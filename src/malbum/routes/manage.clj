(ns malbum.routes.manage
  (:require [compojure.core :refer :all]
            [malbum.views.layout :as layout]
            [malbum.models.db :as db]
            [noir.util.route :refer [restricted]]
            [noir.session :as session]))


(defn account-page []
  (layout/render "account.html"
  {}))

(defn manage-page []
  (layout/render "manage.html"
    {:photos (db/images-by-user-name ((session/get :user ) :uname))}))


(defroutes manage-routes
  (GET "/account" []
    (restricted (account-page)))
  (GET "/manage" []
    (restricted (manage-page))))