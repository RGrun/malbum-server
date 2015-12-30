(ns malbum.util
  (:require [noir.session :as session]
            [hiccup.util :refer [url-encode]]
            [malbum.models.db :as db])
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


;; random string generation

(def random (java.util.Random.))

;define characters list to use to generate string
(def char-list
  (map char (concat (range 48 58) (range 66 92) (range 97 123))))

;generates 1 random character
(defn random-char []
  (nth char-list (.nextInt random (count char-list))))

; generates random string of length characters
(defn random-string [length]
  (apply str (take length (repeatedly random-char))))