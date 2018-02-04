(defproject typershark "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [jarohen/chord "0.8.1"]
                 [http-kit "2.2.0"]
                 [ring "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [compojure "1.6.0"]
                 [play-cljs "1.1.0"]
                 [hiccup "1.0.5"]]

  :plugins [[lein-cljsbuild "1.1.7"] [lein-figwheel "0.5.14"]]

  :clean-target ^{:protect false} ["resources/public/js"]

  :source-paths ["src/clj"]

  :cljsbuild
  {:builds
   [{:id           "development"
     :source-paths ["src/cljs"]
     :figwheel     true
     :compiler     {:main       "typershark.core"
                    :asset-path "js/out"
                    :output-to  "resources/public/js/main.js"
                    :output-dir "resources/public/js/out"}}]}

  :main typershark.core)
