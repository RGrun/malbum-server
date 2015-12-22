(ns malbum.routes.home
  (:require [compojure.core :refer :all]
            [malbum.util :refer [thumb-prefix]]
            [malbum.models.db :as db]
            [malbum.views.layout :as layout]))



(defn fetch-latest
  "Grabs the latest ten images uploaded to the server."
  []
  (let [photos (db/latest-images 10)]  ;; grab ten latest images
    { :pictures photos }))

(defn home []
  (let [photos (fetch-latest)]
   (layout/render "home.html" photos)))


(defroutes home-routes
  (GET "/" [] (home)))
