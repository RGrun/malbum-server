(ns malbum.models.db
  (:require [korma.core :as sql]
            [korma.db :refer [defdb transaction]]
            [noir.util.crypt :as crypt]
            [clj-time.coerce :refer [to-sql-time]])

  (:import [java.io File FileInputStream FileOutputStream]
           ))

(def db
  {:subprotocol "postgresql"
   :subname "//localhost/malbum"
   :user "malbum"
   :password "malbum"})

(defdb korma-db db)

(sql/defentity comments)
(sql/defentity photos)
(sql/defentity admins)
(sql/defentity settings)
(sql/defentity users)

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
  (if-let [username (:uname (first (sql/select users
                                     (sql/where {:user_id userid})
                                     (sql/limit 1))))]
    username
    "Anonymous"))

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

;; grab the latest image from each user's gallery to use as a preview
(defn get-album-previews []
  (sql/exec-raw
    ["with all_data as (
        select
          *, max (upload_date) over (partition by user_id) as max_date
        from photos
        where deleted = false
      )
      select *
      from all_data
      where
        upload_date = max_date" []]
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
  (println comment "\n" user-id "\n" photo-id)
  (if (not (clojure.string/blank? comment))  ;; don't post blank comments

    (transaction
      (sql/insert comments (sql/values { :photo_id photo-id
                                         :user_id user-id   ;; user-id of -1 indicates anonymous comment
                                         :comment comment
                                         :date (to-sql-time (now)) })))
    (throw
      (Exception. "There was an error uploading the comment."))))

(defn get-comments-for-photo
  "Returns a seq of all comments for a photo."
  [photo-id]
  (sql/select comments
    (sql/where {:photo_id photo-id :deleted false})
    (sql/order :date :desc)))


(defn latest-images
  "Returns the last n images added to the server."
  ([end]
    (latest-images 0 end))
  ([start end]
    (for [photo (sql/select photos
                  (sql/where {:deleted false})
                  (sql/order :upload_date :desc)
                  (sql/offset start)
                  (sql/limit end))]
      (assoc photo :uname (username-by-id (photo :user_id)))))) ;; add usernames to returned result

(defn list-users
  "Returns a seq of all users"
  []
  (for [user (sql/select users)]
    (assoc user :uname (username-by-id (user :user_id)))))

(defn user-from-key [key]
  (try
    (let [user (first (sql/select users
                        (sql/where {:api_key key})))]
      (assoc user :uname (username-by-id (user :user_id))))
    (catch Exception ex
      nil)))


(defn is-admin?
  "Checks to see if a given user is an admin."
  [userid]
  (let [id-seq  (for [rec (sql/select admins)]
                  (let [id (rec :user_id)]
                    id))]
    (not (nil? (some #(= userid %) id-seq)))))

(defn api-password-valid?
  "Checks to see if the password is valid for a mobile user."
  [uname pwd]
  (let [user (user-by-name uname)]
    (crypt/compare pwd (user :pass))))

(defn api-recent-thumbnails-for-user
  "Fetches recent thumbnails for a particular user."
  [uname]
  (let [thumbs (sql/select photos
                 (sql/fields [:thumb_name])
                 (sql/where {:user_id (id-by-username uname)}))]
    (if-not (empty? thumbs)
      thumbs
      '())))

(defn photo-from-id
  "Returns a photo's information based on it's id."
  [photo-id]
  (let [photo (first (sql/select photos
                (sql/where {:photo_id (read-string photo-id)})))]
    (if-not (empty? photo)
      (assoc photo :uname (username-by-id (:user_id photo)))
      nil)))


;; the following help determine if various global site settings are set

(defn site-name
  "Returns the name of the site."
  []
  ((first (sql/select settings)) :site_name))

(defn is-super-user?
  "Checks to see if the current user is the site's super user."
  [userid]
  (let [suid ((first (sql/select settings)) :super_user_id)]
    (= userid suid)))

(defn uploads-allowed?
  "Checks to see if uploads are allowed to the site at all."
  []
  ((first (sql/select settings)) :allow_uploads))

(defn site-public?
  "Checks to see if the site is public."
  []
  ((first (sql/select settings)) :site_public))

(defn anon-comments?
  "Checks to see if anonymous comments are allowed."
  []
  ((first (sql/select settings)) :anon_comments))