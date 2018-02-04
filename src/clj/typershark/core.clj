(ns typershark.core
  (:require [typershark.pages :as pages]
            [typershark.handlers :as handlers]
            [compojure.core :refer :all]
            [compojure.route :refer :all]
            [ring.middleware.defaults :as defaults]
            [org.httpkit.server :as server]
            [ring.util.response :as response]))


(defroutes application

  (GET "/" request
    (pages/index-page))

  (GET "/ws" request
    (handlers/connect! request))

  (resources "/")

  (response/not-found "Not found!"))

(defn default-mw [handler]
  (defaults/wrap-defaults handler defaults/site-defaults))

(alter-var-root #'application default-mw)

(defn -main [& args]
  (let [result (server/run-server #'application)]
    (println "Server started up!")
    @(promise)))