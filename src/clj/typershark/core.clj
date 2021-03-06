(ns typershark.core
  (:require [typershark.pages :as pages]
            [typershark.handlers :as handlers]
            [compojure.core :refer :all]
            [compojure.route :refer :all]
            [ring.middleware.defaults :as defaults]
            [org.httpkit.server :as server]
            [ring.middleware.json :as rj]
            [typershark.middleware :as mw]
            [ring.middleware.reload :as reload]))


(defn default-mw [handler]
  (defaults/wrap-defaults handler
    (-> defaults/site-defaults
        (assoc-in [:security :ssl-redirect] false)
        (assoc-in [:security :anti-forgery] false))))

(defn json-mw [handler]
  (-> handler (rj/wrap-json-body) (rj/wrap-json-params) (rj/wrap-json-response)))


(defroutes unauthenticated

  (resources "/static")

  (GET "/login" request
    (pages/login-page))

  (GET "/healthz" request
    {:body    "{\"healthy\": true}"
     :headers {"content-type" "application/json"}}))


(defroutes authenticated

  (GET "/ws" [game :as request]
    (handlers/connect! request game))

  (->
    (routes
      (GET "/api/games" request
        {:body (handlers/get-games)})

      (POST "/api/games" request
        {:body (handlers/new-game!)}))
    (json-mw))

  (GET "/**" request
    (pages/index-page)))


(defroutes application

  unauthenticated

  (->
    authenticated
    (mw/wrap-authentication)))

(alter-var-root #'application reload/wrap-reload)
(alter-var-root #'application default-mw)

(defn -main [& args]
  (let [result (server/run-server #'application)]
    (println "Server started up!")
    @(promise)))