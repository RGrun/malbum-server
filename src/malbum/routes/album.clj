(ns malbum.routes.album
  (:require [compojure.core :refer :all]
            [malbum.views.layout :as layout]
            [malbum.util
             :refer [thumb-prefix image-uri thumb-uri]]
            [malbum.models.db :as db]
            [noir.session :as session]))

(defn display-album [uname]
  (layout/render "album.html"
    {:thumb-prefix thumb-prefix
     :page-owner uname
     :pictures (db/images-by-user-name uname) }))

(defroutes album-routes
  (GET "/album/:uname" [uname]
    (display-album uname)))
