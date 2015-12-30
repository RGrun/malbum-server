(ns malbum.routes.api
  (:require [malbum.routes.upload :as upload]
            [compojure.core :refer [defroutes GET POST]]
            [malbum.models.db :as db]
            [malbum.util :refer [albums album-path album-path-api thumb-prefix thumb-uri thumb-size]]
            [noir.response :as resp]
            [noir.io :refer [upload-file resource-path]]
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


;; TODO: when app is complete, modify to accept custom file data
(defn handle-api-upload
  "Handles image uploads from mobile clients."
  [api-key {:keys [filename] :as file}]
  (let [user (db/user-from-key api-key)
        uname (user :uname)]
    (if (empty? filename)
      (str "File upload filed. Your user: " uname ". Map : " file)
      (try

        ;; save the file and create thumbnail
        (upload-file (album-path-api uname) file)
        (save-thumbnail-api uname file)
        (db/add-image (user :user_id) (album-path-api uname) filename filename "Api upload") ;; this will need to be modified here

        ;; return confirmation message
        (str "File upload complete")
        (catch Exception ex
          (str "error occurred during upload: " (.getMessage ex)))))

  ))

;; routes for handler.clj
(defroutes api-routes

  ;; api call to send local file to server with curl:
  ;; curl -F "user=apiuser" -F "file=@/home/richard/img/pikachu_kite.jpg" localhost:3000/api/upload

  (POST "/api/upload" [key file] (handle-api-upload key file)) ;; EXPERIMENTAL api call (works!)
  (POST "/api/seefile" [key file] (str key " | " file)) ;; for debugging the uploaded file map
  )
