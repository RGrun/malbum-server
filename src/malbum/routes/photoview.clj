(ns malbum.routes.photoview
  (:require [compojure.core :refer :all]
            [malbum.views.layout :as layout]
            [malbum.models.db :as db]
            [noir.session :as session]))

(def formatter (java.text.SimpleDateFormat. "hh:mm a 'on' EEE, MMM d, ''yy"))

(defn display-photo [uname photoname]
  (let [pic-raw (db/get-image-by-name photoname)
        formatted-date (.format formatter (pic-raw :upload_date))
        pic-data  (assoc pic-raw :upload-date formatted-date)]
    (layout/render "photoview.html"
      { :page-user uname
        :picture  pic-data})))

(defroutes photoview-routes
  (GET "/:uname/detail/:photoname" [uname photoname]
    (display-photo uname photoname)))
