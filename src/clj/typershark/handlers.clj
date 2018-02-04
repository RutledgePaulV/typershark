(ns typershark.handlers
  (:require [org.httpkit.server :as server]))

(defonce CLIENTS (atom #{}))

(defmulti handle-event :kind)

(defmethod handle-event :default [event]
  (println "Received event" event)
  {:ack true})

(defn connect! [request]
  (let [params nil]
    (server/with-channel request channel
      (server/on-close channel
        (fn [status]
          (swap! CLIENTS disj channel)))
      (server/on-receive channel
        (fn [data]
          (swap! CLIENTS conj channel)
          (when-some [response (handle-event data)]
            (server/send! channel response)))))))