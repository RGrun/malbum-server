(ns malbum.models.schema
  (:require [malbum.models.db :refer :all]
            [clojure.java.jdbc :as sql]))


(defn create-users-table
  "Base users table"
  []
  (sql/with-connection db
    (sql/create-table
      :users
      [:user_id "serial PRIMARY KEY"]
      [:uname "text NOT NULL"]
      [:uname_lower "text NOT NULL"]
      [:fname "text NOT NULL"]
      [:lname "text NOT NULL"]
      [:pass "text NOT NULL"]
      [:api_key "text NOT NULL"])))


(defn create-roles-table
  "Roles describe user roles, eg: 'admin', 'editor' etc..."
  []
  (sql/with-connection db
    (sql/create-table
      :roles
      [:role_id "serial PRIMARY KEY"]
      [:role_name "text NOT NULL"])))

(defn create-rules-table
  "Rules describe things roles can do, like permissions."
  []
  (sql/with-connection db
    (sql/create-table
      :rules
      [:rule_id "serial PRIMARY KEY"]
      [:rule_name "text NOT NULL"])))

(defn create-user-roles-table
  "User to roles mapping"
  []
  (sql/with-connection db
    (sql/create-table
      :user_roles
      [:user_id "int NOT NULL REFERENCES users (user_id) ON DELETE CASCADE"]
      [:role_id "int NOT NULL REFERENCES roles (role_id) ON DELETE CASCADE"])))

(defn create-role-rules-table
  "Roles to rules mapping"
  []
  (sql/with-connection db
    (sql/create-table
      :role_rules
      [:role_id "int NOT NULL REFERENCES roles (role_id) ON DELETE CASCADE"]
      [:rule_id "int NOT NULL REFERENCES rules (rule_id) ON DELETE CASCADE"])))

(defn create-photos-table
  "Photos table holds photo information"
  []
  (sql/with-connection db
    (sql/create-table
      :photos
      [:photo_id "serial PRIMARY KEY"]
      [:user_id "int NOT NULL REFERENCES users (user_id) ON DELETE CASCADE"]
      [:name "text NOT NULL"]
      [:thumb_name "text NOT NULL"]
      [:photo_path "text NOT NULL"]
      [:upload_date "timestamptz NOT NULL"]
      [:modified_date "timestamptz NOT NULL"]
      [:deleted "bool DEFAULT false"])))

(defn create-comments-table
  "Photo comments"
  []
  (sql/with-connection db
    (sql/create-table
      :comments
      [:comment_id "serial PRIMARY KEY"]
      [:photo_id "int NOT NULL REFERENCES photos (photo_id) ON DELETE CASCADE"]
      [:user_id "int NOT NULL"]  ;; id of -1 means anonymous comment
      [:comment "text NOT NULL"]
      [:date "timestamptz NOT NULL"]
      [:deleted "bool DEFAULT false"])))

(defn create-site-settings-table
  "Site-wide settings table"
  []
  (sql/with-connection db
    (sql/create-table
      :settings
      [:site_name "text NOT NULL"]
      ;; id of user who has all site options avaliable always
      [:super_user_id "int NOT NULL REFERENCES users (user_id) ON DELETE CASCADE"]
      [:allow_uploads "bool DEFAULT true"]
      [:site_public "bool DEFAULT false"]
      [:anon_comments "bool DEFAULT false"])))

(defn create-users-allowed-upload-table
  "Users still allowed to upload photos when 'allowuploads' setting is false"
  []
  (sql/with-connection db
    (sql/create-table
      :users_allowed_upload
      [:user_id "int NOT NULL REFERENCES users (user_id) ON DELETE CASCADE"])))

(defn setup-database-tables
  "Bootstrap the database"
  []
  (do
    (create-users-table)
    (create-roles-table)
    (create-rules-table)
    (create-user-roles-table)
    (create-role-rules-table)
    (create-photos-table)
    (create-comments-table)
    (create-site-settings-table)
    (create-users-allowed-upload-table)))