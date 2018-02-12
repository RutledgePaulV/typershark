(defproject typershark "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [jarohen/chord "0.8.1"]
                 [http-kit "2.3.0-alpha5"]
                 [ring "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-defaults "0.3.1"]
                 [com.cemerick/friend "0.2.3"]
                 [reagent "0.7.0"]
                 [cljs-ajax "0.7.3"]
                 [hazard "0.3.0"]
                 [funcool/bide "1.6.0"]
                 [compojure "1.6.0"]
                 [play-cljs "1.1.0"]
                 [hiccup "1.0.5"]
                 [digest "1.4.6"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.14"]]

  :clean-targets ^{:protect false} ["resources/public/js"]

  :jvm-opts ["-XX:+AlwaysPreTouch" "-Xmx2000m" "-Xms2000m" "--add-modules" "java.xml.bind"]

  :source-paths ["src/clj"]

  :cljsbuild
  {:builds
   [{:id           "development"
     :source-paths ["src/cljs"]
     :figwheel     true
     :compiler     {:main       "typershark.core"
                    :asset-path "/static/js/out"
                    :preloads   [devtools.preload]
                    :output-to  "resources/public/js/main.js"
                    :output-dir "resources/public/js/out"}}

    {:id           "production"
     :source-paths ["src/cljs"]
     :compiler     {:optimizations   :advanced
                    :main            "typershark.core"
                    :closure-defines {typershark.game/BASE_WEBSOCKET
                                      "wss://illuminepixels.io"}
                    :output-to       "resources/public/js/main.js"}}]}

  :main typershark.core)
