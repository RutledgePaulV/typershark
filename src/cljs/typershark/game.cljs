(ns typershark.game
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [chord.client :refer [ws-ch]]
            [play-cljs.core :as play]))

(enable-console-print!)

(defonce state (atom {}))


(defmulti handle-event :kind)

(defmethod handle-event :default [event]
  (println "Received unknown event" event))


(defn start-event-loop [connection]
  (go (let [{:keys [ws-channel error]} (async/<! connection)]
        (async/>! ws-channel {:kind :init})
        (loop [event (async/<! ws-channel)]
          (if event
            (do (handle-event event)
                (recur (async/<! ws-channel)))
            (println "Connection to server was closed."))))))

(defn title-text []
  [[:fill {:color "lightblue"} [:rect {:x 0 :y 0 :width 500 :height 500}]]])

(defonce game (play/create-game 500 500 {:debug? false}))


(def title-screen
  (reify play/Screen
    (on-show [this])
    (on-hide [this])
    (on-render [this]
      (play/render game
        (title-text)))))

(defn start! []
  (start-event-loop (ws-ch "ws://localhost:8090/ws")))

(doto game
  (play/start)
  (play/set-screen title-screen))