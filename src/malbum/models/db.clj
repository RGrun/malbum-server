(ns malbum.models.db
  (:require [korma.core :as sql]
            [korma.db :refer [defdb transaction]]
            [clj-time.coerce :refer [to-sql-time]])

  (:import [java.io File FileInputStream FileOutputStream]
           ))

(def db
  {:subprotocol "postgresql"
   :subname "//localhost/malbum"
   :user "admin"
   :password "admin"})

(defdb korma-db db)

(sql/defentity comments)
(sql/defentity photos)
(sql/defentity role_rules)
(sql/defentity roles)
(sql/defentity rules)
(sql/defentity settings)
(sql/defentity user_roles)
(sql/defentity users)
(sql/defentity users_allowed_upload)

(defn now [] (new java.util.Date))

(defn create-user [new-user]
  (sql/insert users (sql/values new-user)))


(defn user-by-name
  "User by username."
  [uname]
  (first (sql/select users
               (sql/where {:uname_lower (clojure.string/lower-case uname)})
               (sql/limit 1))))

(defn user-by-id
  "User by user id"
  [userid]
  (first (sql/select users
               (sql/where {:user_id  userid})
               (sql/limit 1))))

(defn username-by-id
  "Helper shortcut method."
  [userid]
  (:uname (first (sql/select users
           (sql/where {:user_id userid})
           (sql/limit 1)))))

(defn id-by-username
  "Helper shortcut method."
  [uname]
  (:user_id (first (sql/select users
                   (sql/where {:uname_lower (clojure.string/lower-case uname)})
                   (sql/limit 1)))))

;; saves image metadata in database
(defn add-image
  "Add image to database."
  [userid path name custom-name desription]
  (transaction
    (if (empty? (sql/select photos
                  (sql/where {:user_id userid :name name})
                  (sql/limit 1)))
      (sql/insert photos (sql/values
                           {:user_id userid
                            :photo_path (str path File/separator name)
                            :thumb_name (str "thumb_" name)
                            :description (if (>= (count desription) 1) desription "No comment.")
                            :upload_date (to-sql-time (now))
                            :modified_date (to-sql-time (now))
                            :deleted false
                            :name name
                            :custom_name (if (>= (count custom-name) 1) custom-name name) }))
      (throw
        (Exception. "You've already uploaded an image with the same name!")))))

;; grabs all images by one user
(defn images-by-user-id [userid]
  (sql/select photos (sql/where {:user_id userid :deleted false })
                     (sql/order :upload_date :desc)))


(defn images-by-user-name [uname]
  (let [userid ((user-by-name (clojure.string/lower-case uname)) :user_id)
        proper-uname (username-by-id userid)
        photos (images-by-user-id userid)]
    (for [photo photos]
      (assoc photo :uname proper-uname))))

;; should only return one image
(defn get-image-by-name [image-name]
  (first
    (sql/select photos
      (sql/where {:name image-name}))))

;; TODO: make function query return last row instead of first
;; grab the first image from each user's gallery to use as a preview
(defn get-album-previews []
  (sql/exec-raw
    ["select * from
      (select *, row_number() over (partition by user_id) as row_number from photos)
      as rows where row_number = 1 and deleted = false" []]
    :results))



;; sets photo to 'deleted = true' state in database
(defn delete-image [uname name]
  (let [userid (id-by-username uname)]
    (sql/update photos
      (sql/set-fields { :deleted true })
      (sql/where { :name name :user_id userid }))))

(defn delete-user [userid]
  (sql/delete users (sql/where {:user_id userid})))


(defn add-comment
  "Add a comment to the database."
  [comment user-id photo-id]
  (when (not (clojure.string/blank? comment))  ;; don't post blank comments
    (transaction
      (sql/insert comments (sql/values { :photo_id (read-string photo-id)
                                         :user_id user-id   ;; user-id of -1 indicates anonymous comment
                                         :comment comment
                                         :date (to-sql-time (now)) })))))

(defn get-comments-for-photo
  "Returns a seq of all comments for a photo."
  [photo-id]
  (sql/select comments
    (sql/where {:photo_id photo-id :deleted false})))