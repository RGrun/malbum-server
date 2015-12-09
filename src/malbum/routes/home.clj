(ns malbum.routes.home
  (:require [compojure.core :refer :all]
            [malbum.util :refer [thumb-prefix]]
            [malbum.models.db :as db]
            [malbum.views.layout :as layout]))

;; TODO: turn the home page into a "latest images descending" page

(defn fetch-previews []
  (let [previews (db/get-album-previews)
        rows (partition-all 4 previews)
        rows-construct (for [x rows]
                         { :galrow (for [y x]
                                     {:row (assoc y :uname (db/username-by-id (y :user_id)))})})]
   {:galleries rows-construct }
    ))

(defn home []
  (let [galleries (fetch-previews)]
  (layout/render "home.html" galleries)))


(defroutes home-routes
  (GET "/" [] (home)))
