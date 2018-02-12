(ns typershark.game
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async]
            [typershark.navigation :as nav]
            [chord.client :refer [ws-ch]]
            [play-cljs.core :as play]))

(goog-define BASE_WEBSOCKET "ws://localhost:8090")

(def STATE (atom {:particles []}))
(def GAME (atom nil))

(defn get-boundaries []
  [(quot (.-innerWidth js/window) 2)
   (quot (.-innerHeight js/window) 2)])

(defn new-particle [[width height]]
  {:x       (* (rand) width)
   :y       (+ height (* (rand) height))
   :speed   (rand)
   :radius  (+ 3 (* 6 (rand)))
   :opacity (int (+ 150 (* 55 (rand))))})

(defn particle->arc [{:keys [x y radius opacity]}]
  [:fill
   {:colors [255 255 255 opacity]}
   [:ellipse {:x x :y y :width radius :height radius}]])

(defn update-particle [{:keys [y speed] :as particle}]
  (if (> -10 y)
    (new-particle (get-boundaries))
    (assoc particle :y (- y speed))))

(def main-screen
  (reify play/Screen

    (on-show [this]
      (let [bounds    (get-boundaries)
            renderer  (play/get-renderer @GAME)
            particles (repeatedly 75 (partial new-particle bounds))]
        (try
          (.blendMode renderer "lighter")
          (catch js/Error e (println "Error " e)))
        (swap! STATE assoc :particles particles)))

    (on-hide [this]
      (println "Hiding"))

    (on-render [this]
      (let [game  @GAME
            state @STATE
            parts (:particles state)]
        (play/render game (mapv particle->arc parts))
        (let [updated-particles (mapv update-particle parts)]
          (swap! STATE assoc :particles updated-particles))))))

(defn init-screen! []
  (let [[width height] (get-boundaries)
        game (play/create-game width height)]
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
