(ns malbum.routes.photoview
  (:require [compojure.core :refer :all]
            [malbum.views.layout :as layout]
            [malbum.models.db :as db]
            [noir.response :as resp]
            [noir.session :as session]))

(def formatter (java.text.SimpleDateFormat. "hh:mm a 'on' EEE, MMM d, ''yy"))

(defn display-photo [uname photoname]
  (let [pic-raw (db/get-image-by-name photoname)
        formatted-date (.format formatter (pic-raw :upload_date))
        pic-data  (assoc pic-raw :upload-date formatted-date)
        comments  (for [cmt  (db/get-comments-for-photo (pic-data :photo_id))]
                    (let [ user-id (cmt :user_id)]
                      (if (not (= (- 1) user-id))
                        (assoc cmt :uname (db/user-by-id user-id)) ;; assoc username of comment poster
                        (assoc cmt :uname "Anonymous"))))]
    (layout/render "photoview.html"
      { :page-user uname
        :comments comments
        :picture  pic-data })))

;; TODO: make use of 'errors' div in photoview page instead of returning str
;; TODO: have function check to see if 'allow anonymous comments' is true in global settings
(defn process-comment [comment photo-id]
  (if-let [poster-uid  ((session/get :user) :user_id)]
    (try
      (db/add-comment comment  poster-uid (int photo-id))
      (catch Exception ex
        (do (println ex) (str ex))))
    (try
      (db/add-comment comment (- 1) photo-id)
      (catch Exception ex
        (do (println ex) (str ex))))))

(defroutes photoview-routes
  (GET "/:uname/detail/:photoname" [uname photoname]
    (display-photo uname photoname))
  (POST "/add-comment" [new_comment photo_id]
    (process-comment new_comment photo_id)))
