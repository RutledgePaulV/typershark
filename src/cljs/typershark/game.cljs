(ns typershark.game
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [typershark.navigation :as nav]
            [chord.client :refer [ws-ch]]
            [play-cljs.core :as play]))

(goog-define BASE_WEBSOCKET "ws://localhost:8090")

(def STATE (atom {}))
(def GAME (atom nil))

(def main-screen
  (reify play/Screen

    (on-show [this]
      (println "Showing"))

    (on-hide [this]
      (println "Hiding"))

    (on-render [this]
      (play/render @GAME
        []))))

(defn init-screen! []
  (let [width  (quot (.-innerWidth js/window) 2)
        height (quot (.-innerHeight js/window) 2)
        game   (play/create-game width height)]
    (reset! GAME game)
    (doto game
      (play/start)
      (play/set-screen main-screen))))

(defmulti
  handle-event
  (fn [channel event]
    (keyword (:kind event))))

(defmethod handle-event :state-change [channel event]
  (swap! STATE merge (:data event))
  (when-not @GAME
    (init-screen!)))

(defmethod handle-event :default [channel event]
  (println "Received unknown event" event))

(defn start-event-loop [url]
  (let [connection (ws-ch url) close (async/chan)]
    (go (let [{:keys [ws-channel error]} (async/<! connection)]
          (if error
            (nav/navigate! :typershark/menu {})
            (do (async/>! ws-channel {:kind :init})
                (loop [[event port] (async/alts! [ws-channel close])]
                  (if (identical? port close)
                    (async/close! ws-channel)
                    (if event
                      (do (handle-event ws-channel (:message event))
                          (recur (async/alts! [ws-channel close])))
                      (println "Connection to server was closed."))))))))
    (fn [] (async/put! close {}))))

(defn attach! [game]
  (let [url   (str BASE_WEBSOCKET "/ws?game=" game)
        close (start-event-loop url)]
    (nav/on-leave!
      (fn []
        (reset! STATE {})
        (reset! GAME nil)
        (close)))))
