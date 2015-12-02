(defproject malbum "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [postgresql/postgresql "9.1-901.jdbc4"] ;; postgres driver
                 [org.clojure/java.jdbc "0.2.3"] ;; database connections
                 [lib-noir "0.7.6"]             ;; sessions, cookies, etc
                 [com.taoensso/timbre "2.6.1"]  ;; logging
                 [com.postspectacular/rotor"0.1.0"] ;; logging
                 [prone "0.8.2"] ;; WAAAAY better error reporting
                 [selmer "0.5.4"] ;; HTML templating using template files
                 [clj-time "0.11.0"]
                 ]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler malbum.handler/app
         :init malbum.handler/init
         :destroy malbum.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}})
