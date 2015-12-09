(ns malbum.routes.album
  (:require [compojure.core :refer :all]
            [malbum.views.layout :as layout]
            [malbum.util
             :refer [thumb-prefix image-uri thumb-uri]]
            [malbum.models.db :as db]
            [noir.session :as session]))

(defn fetch-previews []
  (let [previews (db/get-album-previews)
        rows (partition-all 4 previews)
        rows-construct (for [x rows]
                         { :galrow (for [y x]
                                     {:row (assoc y :uname (db/username-by-id (y :user_id)))})})]
    {:galleries rows-construct }))

(defn display-all-albums []
  (let [galleries (fetch-previews)]
    (layout/render "all-albums.html" galleries)))

(defn display-album [uname]
  (layout/render "album.html"
    {:thumb-prefix thumb-prefix
     :page-owner uname
     :pictures (db/images-by-user-name uname) }))

(defroutes album-routes
  (GET "/album/:uname" [uname]
    (display-album uname))
  (GET "/all-albums" []
    (display-all-albums)))
