(defproject malbum "0.1.0-SNAPSHOT"
  :description "A private photo-sharing web app."
  :url "https://github.com/RGrun/malbum-server"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [postgresql/postgresql "9.1-901.jdbc4"] ;; postgres driver
                 [korma "0.4.2"]                ;; SQL DSL
                 [lib-noir "0.7.6"]             ;; sessions, cookies, etc
                 [com.taoensso/timbre "2.6.1"]  ;; logging
                 [com.postspectacular/rotor"0.1.0"] ;; logging
                 [prone "0.8.2"] ;; WAAAAY better error reporting
                 [selmer "0.5.4"] ;; HTML templating using template files
                 [clj-time "0.11.0"]
                 [org.clojure/clojurescript "0.0-1806"]
                 [log4j "1.2.15"   ;; logging
                  :exclusions [javax.mail/mail
                               javax.jms/jms
                               com.sun.jdmk/jmxtools
                               com.sun.jmx/jmxri]]
                 ]
  :plugins [[lein-ring "0.8.12"]
            [lein-cljsbuild "0.3.2"]]
  :cljsbuild
  {:builds
   [{:source-paths ["src-cljs"]
     :compiler
     {:pretty-print false
      :output-to "resources/public/js/album-cljs.js"}}]}
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
