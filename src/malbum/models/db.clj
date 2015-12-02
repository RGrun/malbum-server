(ns malbum.models.db
  (:require [clojure.java.jdbc :as sql]
            [clj-time.coerce :refer [to-sql-time]])

  (:import [java.io File FileInputStream FileOutputStream]
           ))

(def db
  {:subprotocol "postgresql"
   :subname "//localhost/malbum"
   :user "admin"
   :password "admin"})

(defn now [] (new java.util.Date))

(defmacro with-db [f & body]
  `(sql/with-connection ~db (~f ~@body)))

(defn create-user [new-user]
  (with-db sql/insert-record :users new-user))

(defn get-user [uname]
  (with-db sql/with-query-results
    res ["select * from users where uname_lower = ?" (clojure.string/lower-case uname)] (first res)))

(defn username-by-id [userid]
  (with-db
    sql/with-query-results
    res
    ["select uname from users where user_id = ?" userid]
    ((first (doall res)) :uname)))

;; saves image metadata in database
(defn add-image [userid path name]
  (with-db
    sql/transaction
    (if (sql/with-query-results
          res
          ["select user_id from photos where user_id = ? and name = ?" userid name]
          (empty? res))
      (sql/insert-record :photos {:user_id userid
                                  :photo_path (str path File/separator name)
                                  :thumb_name (str "thumb_" name)
                                  :upload_date (to-sql-time (now))
                                  :modified_date (to-sql-time (now))
                                  :deleted false
                                  :name name})
      (throw
        (Exception. "You've already uploaded an image with the same name!")))))

;; grabs all images by one user
(defn images-by-user-id [userid]
  (with-db
    sql/with-query-results
    res ["select * from photos WHERE user_id = ? AND deleted = false" userid] (doall res)))

(defn images-by-user-name [uname]
  (let [userid ((get-user (clojure.string/lower-case uname)) :user_id)
        proper-uname (username-by-id userid)
        photos (images-by-user-id userid)]
    (for [photo photos]
      (assoc photo :uname proper-uname))))

;; grab the first image from each user's gallery to use as a preview
(defn get-gallery-previews []
  (with-db
    sql/with-query-results
    res
    ["select * from
    (select user_id, thumb_name, row_number() over (partition by user_id) as row_number from photos)
    as rows where row_number = 1"]
    (doall res)))


(defn delete-image [userid name]
  (with-db
    sql/delete-rows :photos ["user_id=? and name=?" userid name]))

(defn delete-user [userid]
  (with-db sql/delete-rows :users ["user_id=?" userid]))


