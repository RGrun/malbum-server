(ns malbum.routes.api
  (:require [malbum.routes.upload :as upload]
            [compojure.core :refer [defroutes GET POST]]
            [malbum.models.db :as db]
            [malbum.util :refer [albums album-path album-path-api thumb-prefix thumb-uri thumb-size]]
            [noir.response :as resp]
            [noir.io :refer [upload-file resource-path]]
            [noir.util.crypt :as crypt]
            [malbum.routes.auth :as auth]
            [clojure.java.io :as io]
            )

  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))

;; this namespace handles uploads from mobile clients


(defn save-thumbnail-api [user {:keys [filename]}]
  "Thumbnail API method"
  (let [path (str (album-path-api user) File/separator)]
    (ImageIO/write
      (upload/scale-image (io/input-stream (str path filename)))
      "jpeg"
      (File. (str path thumb-prefix filename)))))


(defn handle-api-upload
  "Handles image uploads from mobile clients."
  [api-key {:keys [filename] :as file} name descrip]
  (println "Photo upload commencing...")
  (println (str "Key: " api-key))
  (println (str "File: " file))
  (println (str "Name: " name))
  (println (str "Desc: " descrip))
  (let [user (db/user-from-key api-key)
        uname (user :uname)
        file (update-in file [:filename] #(clojure.string/replace % #" " "_"))]
    (if (empty? filename)
      (str "File upload filed. Your user: " uname ". Map : " file)
      (try

        ;; save the file and create thumbnail
        (upload-file (album-path-api uname) file)
        (save-thumbnail-api uname file)
        (db/add-image (user :user_id)
          (album-path-api uname)
          (clojure.string/replace filename #" " "_") ;; replace spaces with underscores
          name
          descrip)

        ;; return confirmation message
        (resp/json {:status "ok"})
        (catch Exception ex
          (resp/json {:status "failure" :error (.getMessage ex)} ))))

  ))

;; returns user data map containing api key
(defn handle-api-login
  "Handle logins from mobile clients."
  [uname pwd]
  (let [errors (auth/check-login-data uname pwd)]
    (if (empty? errors)
      (let [user (db/user-by-name uname)
            user-clean (dissoc user :pass)]
        (resp/json {:status "ok" :data user-clean}))
      (resp/json {:status "failure" :error errors}))))


(defn api-get-images
  "Returns a list of thumbnails for a user."
  [uname]
  (let [thumbs (db/api-recent-thumbnails-for-user uname)]

    (if-not (empty? thumbs)
      (resp/json {:status "ok" :thumbs thumbs})
      (resp/json {:status "failure"}))))

(defn api-albums
  "Returns JSON with info about most recent photo for each user."
  [key]
  (println key)
  (if-let [user (db/user-from-key key)] ;; valid api keys only
    (let [thumb-seq (db/get-album-previews)
          thumbs-unames (for [x thumb-seq] (assoc x :uname (db/username-by-id (:user_id x))))]
      (resp/json {:status "ok" :thumbs thumbs-unames}))
    (resp/json {:status "failure"})))

(defn photos-for-user
  "Get all images relating to one user."
  [key uname]
  (if-let [user (db/user-from-key key)] ;; valid api keys only
    (let [photos (db/images-by-user-name uname)]
      (resp/json {:status "ok" :photos photos}))
    (resp/json {:status "failure"})))

(defn get-photo-information
  "Returns information about a single photo."
  [key photo-id]
  (if-let [user (db/user-from-key key)] ;; valid api keys only
    (let [photo (db/photo-from-id photo-id)
          comments (db/get-comments-for-photo (read-string photo-id))
          comments-usernames (for [x comments]
                               (assoc x :uname (db/username-by-id (:user_id x))))]
      (resp/json {:status "ok" :photo photo :comments comments-usernames}))
    (resp/json {:status "failure"})))

(defn new-comment
  "Accept and save a new comment from mobile clients."
  [key photo-id comment]
  (if-let [user (db/user-from-key key)] ;; valid api keys only
    (try
      (do
        (db/add-comment comment (:user_id user) (read-string photo-id))
        (resp/json {:status "ok"}))

      (catch Exception ex
        (resp/json {:status "failure"})))
    (resp/json {:status "failure"})))

(def formatter (java.text.SimpleDateFormat. "hh:mm a 'on' EEE, MMM d, ''yy"))

(defn get-latest-images
  "Returns the latest n photos."
  [key start end]
  (if-let [user (db/user-from-key key)] ;; valid api keys only
    (let [photo-seq (db/latest-images start end)
          photo-seq-clean (for [x photo-seq]
                            (if (nil? (:custom_name x))
                              (assoc x :custom_name (:name x))
                              x))
          count (count photo-seq)]
      (resp/json {:status "ok" :photos photo-seq-clean :count (str count)}))
    (resp/json {:status "failure"})))

;; routes for handler.clj
(defroutes api-routes

  ;; api call to send local file to server with curl:
  ;; curl -F "user=apiuser" -F "file=@/home/richard/img/pikachu_kite.jpg" localhost:3000/api/upload

  (POST "/api/login" [uname pwd] (handle-api-login uname pwd))


  (GET "/api/albums" [key] (api-albums key)) ;; get first album image for each user

  (GET "/api/photos-for-user/:uname" [key uname] (photos-for-user key uname)) ;; get images relating to one user

  (GET "/api/photo-information" [key photo_id] (get-photo-information key photo_id))

  (GET "/api/latest-images" [key start end] (get-latest-images key start end))

  (POST "/api/new-comment" [key photo_id comment] (new-comment key photo_id comment)) ;; new comment mechanism

  (POST "/api/upload" [file key cname cdescrip] (handle-api-upload key file cname cdescrip))
  (POST "/api/seefile" [key file] (str key " | " file)) ;; for debugging the uploaded file map
  )
