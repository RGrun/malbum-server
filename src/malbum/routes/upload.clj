;; this NS handles file uploads
(ns malbum.routes.upload
  (:require [compojure.core :refer [defroutes GET POST]]
            [malbum.views.layout :as layout]
            [noir.io :refer [upload-file resource-path]]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]
            [clojure.java.io :as io]
            [ring.util.response :refer [file-response]]
            [malbum.models.db :as db]
            [malbum.util :refer [albums album-path album-path-api thumb-prefix thumb-uri thumb-size]]
            [noir.util.route :refer [restricted]]
            [malbum.models.db :as db])

  (:import [java.io File FileInputStream FileOutputStream]
           [java.awt.image AffineTransformOp BufferedImage]
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           javax.imageio.ImageIO))

;; for scaling uploaded images
(defn scale [img ratio width height]
  (let [scale (AffineTransform/getScaleInstance (double ratio) (double ratio))
        transform-op (AffineTransformOp. scale AffineTransformOp/TYPE_BILINEAR)]

    (.filter transform-op img (BufferedImage. width height (.getType img)))))

;; uses java interop to scale the image
(defn scale-image [file]
  (let [img (ImageIO/read file)
        img-width (.getWidth img)
        img-height (.getHeight img)
        ratio (/ thumb-size img-height)]
    (scale img ratio (int (* img-width ratio)) thumb-size)))

;; saves thumbnail to local filesystem
(defn save-thumbnail [{:keys [filename]}]
  (let [path (str (album-path) File/separator)]
    (ImageIO/write
      (scale-image (io/input-stream (str path filename)))
      (if (.endsWith (clojure.string/lower-case filename) "jpeg") "jpeg" "png")
      (File. (str path thumb-prefix filename)))))

;; build image upload form
(defn upload-page [params]
  (println params)
  (layout/render "upload.html" params))

(defn handle-upload [custom-name {:keys [filename] :as file} description]
  (let [ custom-name (if (>= (count custom-name) 1) custom-name filename)
         filename-escaped (clojure.string/replace filename #" " "_")]
    (println custom-name ", " description)
    (upload-page
      (if (empty? filename)
        {:errors "Please select a file to upload."}
        (if (.endsWith (clojure.string/lower-case filename) "gif")
          {:errors "GIF uploads are not supported."}
          (try
            (println (str (album-path) File/separator filename-escaped))
            (upload-file (album-path) (assoc file :filename filename-escaped))
            (save-thumbnail (assoc file :filename filename-escaped))
            (db/add-image ((session/get :user) :user_id) (album-path) filename-escaped custom-name description)
            {:image (thumb-uri ((session/get :user) :uname) filename-escaped)}
            (catch Exception ex
              ;(error ex "an error has occured while uploading" name)
              {:errors (str "error uploading file " (.getMessage ex))})))))))

;; grab file from local filesystem
(defn serve-file [uname file-name]
  (file-response (str albums File/separator uname File/separator file-name)))

;; delete images in database
(defn delete-image [uname pname]
  (try
    (db/delete-image uname pname)
    (io/delete-file (str (album-path) File/separator pname))
    (io/delete-file (str (album-path) File/separator thumb-prefix pname))
    "ok"
    (catch Exception ex
      ;(error ex "Error occurred while deleting: " name)
      (.getMessage ex))))


;; plural version of delete-images
(defn delete-images [names]
  (let [uname ((session/get :user) :uname)]
    (resp/json
      (for [pname names] {:name pname :status (delete-image uname pname)})))) ;; return JSON responses


;; routes for handler.clj
(defroutes upload-routes
  (GET "/img/:uname/:file-name" [uname file-name]
    (serve-file uname file-name))
  (GET "/upload" [info] (restricted (upload-page {:info info})))
  (POST "/upload" [custom_name file description] (restricted (handle-upload custom_name file description)))
  (POST "/delete" [names] (restricted (delete-images names)))

  ;; api call to send local file to server with curl:
  ;; curl -F "user=apiuser" -F "file=@/home/richard/img/pikachu_kite.jpg" localhost:3000/api/upload

  ;(POST "/api/upload" [user file] (handle-api-upload user file)) ;; EXPERIMENTAL api call (works!)
  ;(POST "/api/seefile" [file] (str file)) ;; for debugging the uploaded file map
  )