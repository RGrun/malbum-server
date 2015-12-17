(ns malbum.models.schema
  (:require [malbum.models.db :refer :all]
            [clojure.java.jdbc :as sql]))

;; TODO: redo this in Korma

;(defn create-users-table
;  "Base users table"
;  []
;  (sql/with-connection db
;    (sql/create-table
;      :users
;      [:user_id "serial PRIMARY KEY"]
;      [:uname "text NOT NULL"]
;      [:uname_lower "text NOT NULL"]
;      [:fname "text NOT NULL"]
;      [:lname "text NOT NULL"]
;      [:pass "text NOT NULL"]
;      [:api_key "text NOT NULL"])))
;
;
;
;(defn create-photos-table
;  "Photos table holds photo information"
;  []
;  (sql/with-connection db
;    (sql/create-table
;      :photos
;      [:photo_id "serial PRIMARY KEY"]
;      [:user_id "int NOT NULL REFERENCES users (user_id) ON DELETE CASCADE"]
;      [:name "text NOT NULL"]
;      [:custom_name "text"]
;      [:description "text DEFAULT 'No comment.'"]
;      [:thumb_name "text NOT NULL"]
;      [:photo_path "text NOT NULL"]
;      [:upload_date "timestamptz NOT NULL"]
;      [:modified_date "timestamptz NOT NULL"]
;      [:deleted "bool DEFAULT false"])))
;
;(defn create-comments-table
;  "Photo comments"
;  []
;  (sql/with-connection db
;    (sql/create-table
;      :comments
;      [:comment_id "serial PRIMARY KEY"]
;      [:photo_id "int NOT NULL REFERENCES photos (photo_id) ON DELETE CASCADE"]
;      [:user_id "int NOT NULL"]  ;; id of -1 means anonymous comment
;      [:comment "text NOT NULL"]
;      [:date "timestamptz NOT NULL"]
;      [:deleted "bool DEFAULT false"])))
;
;(defn create-site-settings-table
;  "Site-wide settings table"
;  []
;  (sql/with-connection db
;    (sql/create-table
;      :settings
;      [:site_name "text NOT NULL"]
;      ;; id of user who has all site options avaliable always
;      [:super_user_id "int NOT NULL REFERENCES users (user_id) ON DELETE CASCADE"]
;      [:allow_uploads "bool DEFAULT true"]
;      [:site_public "bool DEFAULT false"]
;      [:anon_comments "bool DEFAULT false"])))
;
;(defn setup-database-tables
;  "Bootstrap the database"
;  []
;  (do
;    (create-users-table)
;    (create-photos-table)
;    (create-comments-table)
;    (create-site-settings-table)))