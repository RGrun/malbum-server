(ns malbum.util
  (:require [noir.session :as session]
            [hiccup.util :refer [url-encode]])
  (:import java.io.File))


(def thumb-prefix "thumb_")


;; old configuration with no environ module
(def albums "albums")

(def thumb-size 150)

(defn album-path []
  (str albums File/separator ((session/get :user) :uname)))

(defn album-path-api [user-name]
  (str albums File/separator user-name))

(defn image-uri [uname file-name]
  (str "/img/" uname File/separator (url-encode file-name)))

(defn thumb-uri [uname file-name]
  (image-uri uname (str thumb-prefix file-name)))