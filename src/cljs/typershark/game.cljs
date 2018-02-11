(ns typershark.game
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [typershark.navigation :as nav]
            [chord.client :refer [ws-ch]]
            [play-cljs.core :as play]))

(defmulti handle-event :kind)

(defmethod handle-event :state-change [event]
  (println "Received state change event!"))

(defmethod handle-event :default [event]
  (println "Received unknown event" event))

(defn start-event-loop [url]
  (let [connection (ws-ch url) close (async/chan)]
    (go (let [{:keys [ws-channel error]} (async/<! connection)]
          (if error
            (nav/navigate! :typershark/menu {})
            (do (async/>! ws-channel {:kind :init})
                (loop [[event port] (async/alts! [ws-channel close])]
                  (if (= port close)
                    (async/close! ws-channel)
                    (if event
                      (do (handle-event (:message event))
                          (recur (async/alts! [ws-channel close])))
                      (println "Connection to server was closed."))))))))
    (fn [] (async/put! close {}))))

(defn attach! [game]
  (let [url (str "ws://localhost:8090/ws?game=" game)]
    (nav/on-leave! (start-event-loop url))))
