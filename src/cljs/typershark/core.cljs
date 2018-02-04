(ns typershark.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [chord.client :refer [ws-ch]]
            [play-cljs.core :as play]))

(enable-console-print!)

(defonce state (atom {}))

(defn new-connection []
  (ws-ch "ws://localhost:8090/ws"))

(defn start-event-loop [connection]
  (go
    (let [{:keys [ws-channel error]} (async/<! connection)]
      (async/>! ws-channel "Hello from the client.")
      (loop [event (async/<! ws-channel)]
        (if event
          (do
            (println "Received data from server" event)
            (recur (async/<! ws-channel)))
          (println "Channel was closed."))))))


(defonce game (play/create-game 500 500 {:debug? false}))

(defn title-text []
  [[:fill {:color "lightblue"} [:rect {:x 0 :y 0 :width 500 :height 500}]]
   [:fill {:color "black"} [:text {:value "Hello, world!" :x 100 :y 100 :size 16 :font "Georgia"}]]])

(def title-screen
  (reify play/Screen
    (on-show [this])
    (on-hide [this])
    (on-render [this]
      (play/render game
        (title-text)))))


(start-event-loop
  (new-connection))

(doto game
  (play/start)
  (play/set-screen title-screen))