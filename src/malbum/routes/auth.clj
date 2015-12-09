;; user registration workflow
(ns malbum.routes.auth
  (:require [hiccup.form :refer :all]
            [compojure.core :refer :all]
            [malbum.routes.home :refer :all]
            [malbum.views.layout :as layout]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [malbum.models.db :as db]
            [malbum.util :refer [album-path]]
            [noir.util.route :refer [restricted]]
            ;[malbum.routes.upload :refer [delete-image]]
            )

  (:import java.io.File))

;; creates folder in album for each user upon user-creation
(defn create-album-path []
  (let [user-path (File. (album-path))]
    (if-not (.exists user-path) (.mkdirs user-path))
    (str (.getAbsolutePath user-path) File/separator)))


;; checks inputted registration data
(defn check-registration-data
  "Checks the registration form input data and returns a map of errors."
  [uname pwd pwd1 fname lname]
  (let [errors '()]
    (cond->> errors
      (or (empty? uname) (< (count uname) 3)) (cons "Please enter a username of at least three characters.")
      false (cons "A user with that username already exists.")
      (< (count pwd) 5) (cons "Password must be at least five characters long.")
      (not (= pwd pwd1)) (cons "Passwords do not match, try again.")
      (< (count fname) 1) (cons "Please enter a first name.")
      (< (count lname) 1) (cons "Please enter a last name."))))

;; checks inputted login data
(defn check-login-data
  "Validates login data"
  [uname pwd]
  (let [errors '()
        user-map (db/user-by-name uname)]
    (cond->> errors
      (nil? user-map) (cons "There is no registered user with that name.")
      (and (not (nil? user-map)) (not (crypt/compare pwd (:pass user-map)))) (cons "Password is invalid, try again."))))


;; builds registration page
(defn registration-page [& errors]
  (layout/render "registration.html"
    (when errors
      {:errors (first errors)})))

;; login page
(defn login-page [& errors]
  (layout/render "login.html"
    (when errors
      {:errors (first errors)})))



(defn format-error [uname ex]
  (cond
    (and (instance? org.postgresql.util.PSQLException ex)
      (= 0 (.getErrorCode ex)))
    (str "The user with username " uname " already exists!")
    :else
    "An error has occured while processing the request"))

;; logs in new user and redirects them to index

(defn handle-registration [uname pwd pwd1 fname lname]
  (let [errors (check-registration-data uname pwd pwd1 fname lname)] ; validate registration form input
    (if (empty? errors)
      (try
        (db/create-user {:uname uname ;; create database record
                         :uname_lower (clojure.string/lower-case uname) ;; for case-insensitive login
                         :fname fname
                         :lname lname
                         :pass (crypt/encrypt pwd)
                         :api_key "PLACEHOLDER" })
        (session/put! :user (db/user-by-name uname)) ;; add user to session
        (create-album-path)   ;; create directory in album folder for new user
        (resp/redirect "/")     ;; redirect to index
        (catch Exception ex
          (do (println ex) (registration-page {:other (str ex)}))))
      (registration-page errors))))

;; checks that user exists during login

;; user map looks like this:
;; { :api_key "PLACEHOLDER",
;; :pass "$2a$10$M6QZ...",
;; :lname "Smith",
;; :fname "John",
;; :uname_lower "jsmith",
;; :uname "JSmith",
;; :user_id 1 }

(defn handle-login [uname pwd]
  (let [errors (check-login-data uname pwd)]
    (if (empty? errors)
      (let [user (db/user-by-name uname)]
        (session/put! :user user)
        (resp/redirect "/"))
      (login-page errors))))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

;; confirmation regarding account deletion
;(defn delete-account-page []
;  (layout/render "deleteAccount.html" {}))

;; actual delete happens here
;(defn handle-confirm-delete []
;  (let [user (session/get :user)]
;    (doseq [{:keys [name]} (db/images-by-user user)]
;      (delete-image user name))
;    (clojure.java.io/delete-file (album-path))
;    (db/delete-user user))
;  (session/clear!)
;  (resp/redirect "/"))


;; route declarations

(defroutes auth-routes

  (GET "/register" []
    (registration-page)) ;; return registration page
  (POST "/register" [uname pwd pwd1 fname lname]
    (handle-registration uname pwd pwd1 fname lname))  ;; handle user registration with POST'ed data

  (GET "/login" []
    (login-page))
  (POST "/login" [uname pwd]
    (handle-login uname pwd))

  (GET "/logout" []
    (handle-logout))

;  (GET "/delete-account" []
;    (restricted (delete-account-page)))

 ; (POST "/confirm-delete" []
;    (restricted (handle-confirm-delete))) ;; actual deletion happens here
)
