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
                        (assoc cmt :uname ((db/user-by-id user-id) :uname)) ;; assoc username of comment poster
                        (assoc cmt :uname "Anonymous"))))]
    (layout/render "photoview.html"
      { :page-user uname
        :comments comments
        :picture  pic-data })))

;; TODO: format returned date
(defn process-comment [comment photo-id]
  (if-let [poster-sess  (session/get :user)]   ;; checks to see if user posting comment is logged in
    (try
      (db/add-comment comment  (poster-sess :user_id)  (read-string photo-id))
      (resp/json {:poster_name (db/username-by-id (poster-sess :user_id))
                  :status "ok"
                  :time (db/now)})  ;; success result
      (catch Exception ex
        (resp/json {:poster_name (db/username-by-id (poster-sess :user_id)) :status (str ex)})))  ;; failure result
    (try
      (db/add-comment comment (- 1) (read-string photo-id))             ;; if user isn't logged in, it's an anonymous comment
      (resp/json {:poster_name "Anonymous"
                  :status "ok"
                  :time (db/now)})
      (catch Exception ex
        (resp/json {:poster_name "Anonymous" :status ex})))))

(defroutes photoview-routes
  (GET "/:uname/detail/:photoname" [uname photoname]
    (display-photo uname photoname))
  (POST "/add-comment" [new_comment photo_id]
    (process-comment new_comment photo_id)))
