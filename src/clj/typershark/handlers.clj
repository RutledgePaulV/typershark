(ns typershark.handlers
  (:require [chord.http-kit :refer [with-channel]]
            [clojure.core.async :as async]
            [ring.util.response :as response]
            [cemerick.friend :as friend])
  (:import (java.util UUID)))

(defonce GAMES (atom {}))

(defn broadcast! [msg channels]
  (dorun (pmap #(async/put! % msg) channels)))

(defn uuid []
  (str (UUID/randomUUID)))

(defn get-channels [game]
  (->> (get game :users) (vals) (map :channel)))

(defn front-end-state [n]
  (let [users (get n :users)]
    {:users users}))

(defn add-state-notifications [game-atom]
  (add-watch game-atom "state-changes"
    (let [counter (atom 0)]
      (fn [k r o n]
        (let [event
              {:kind  :state-change
               :data  (front-end-state n)
               :order (swap! counter inc)}]
          (broadcast! event (get-channels n)))))))

(defn spawn-broadcast-loop [game-atom]
  (let [broadcast (async/chan)
        updated   (swap! game-atom assoc :broadcast broadcast)]
    (async/go-loop [event (async/<! broadcast)]
      (when event
        (doseq [chan (get-channels @updated)]
          (async/>! chan event))
        (recur (async/<! broadcast))))))

(defn game-descriptor [game]
  {:id    (get game :id)
   :users (count (get game :users))})

(defn get-games
  ([] (get-games @GAMES))
  ([games] (->> games (vals) (map deref) (mapv game-descriptor))))

(defn new-game! []
  (let [id   (uuid)
        game (atom {:id id :users {}})]
    (spawn-broadcast-loop game)
    (add-state-notifications game)
    (get-games (swap! GAMES assoc id game))))

(defmulti handle-user-event
  (fn [game event] (keyword (get event :kind))))

(defmethod handle-user-event :init [game event]
  {:kind :state-change :data (front-end-state @game)})

(defmethod handle-user-event :default [game event]
  (println "Received event for game " (get @game :id))
  {:ack true})

(defn deregister! [game channel]
  (let [sess (hash channel)]
    (swap! game update :users dissoc sess)))

(defn register! [game request channel]
  (let [user (friend/current-authentication request) sess (hash channel)]
    (swap! game assoc-in [:users sess] {:channel channel :user user :id sess})))

(defn connect! [request game-id]
  (if-some [game (get @GAMES game-id)]
    (with-channel request channel
      (register! game request channel)
      (async/go-loop []
        (if-some [message (async/<! channel)]
          (when-some [response (handle-user-event game message)]
            (if (fn? response)
              (when-some [resolved (async/<! (async/thread (response)))]
                (async/>! channel resolved))
              (async/>! channel response))
            (recur))
          (deregister! game channel))))
    (response/not-found "No such game!")))