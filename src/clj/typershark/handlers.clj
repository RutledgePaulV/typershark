(ns typershark.handlers
  (:require [chord.http-kit :refer [with-channel]]
            [clojure.core.async :as async]
            [clojure.data :as data]))

(defonce CLIENTS (atom #{}))

(defn broadcast!
  ([msg] (broadcast! msg @CLIENTS))
  ([msg channels] (dorun (pmap #(async/put! % msg) channels))))

(add-watch CLIENTS "peer-events"
  (fn [k r o n]
    (let [[added removed] (data/diff n o)]
      (doseq [peer added] (broadcast! {:peer-added true} (disj n peer)))
      (doseq [peer removed] (broadcast! {:peer-removed true} (disj n peer))))))

(defmulti handle-event :kind)

(defmethod handle-event :default [event]
  (println "Received event" event)
  {:ack true})

(defn register! [request channel]
  (swap! CLIENTS conj channel))

(defn deregister! [request channel]
  (swap! CLIENTS disj channel))

(defn connect! [request]
  (with-channel request channel
    (async/go-loop []
      (if-some [message (async/<! channel)]
        (do
          (register! request channel)
          (async/<! (async/thread (handle-event message)))
          (recur))
        (deregister! request channel)))))