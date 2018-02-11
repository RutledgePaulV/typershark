(ns typershark.handlers
  (:require [chord.http-kit :refer [with-channel]]
            [clojure.core.async :as async]
            [ring.util.response :as response]
            [cemerick.friend :as friend]
            [hazard.core :as hazard]))

(def GAMES (atom {}))

(defn broadcast! [msg channels]
  (dorun (pmap #(async/put! % msg) channels)))

(defn get-channels [game]
  (->> (get game :users) (vals) (map :channel)))

(defn front-end-state [n]
  (let [users (get n :users {})]
    {:users (mapv #(dissoc % :channel) (vals users))}))

(defn add-state-notifications [game-atom]
  (add-watch game-atom "state-changes"
    (fn [k r o n] #_(broadcast! {:ping true} (get-channels n)))))

(defn spawn-broadcast-loop [game-atom]
  (let [broadcast (async/chan)
        updated   (swap! game-atom assoc :broadcast broadcast)]
    (async/go-loop [event (async/<! broadcast)]
      (when event
        (doseq [chan (get-channels @updated)]
          (async/>! chan event))
        (recur (async/<! broadcast))))))

(defn game-descriptor [game]
  {:key   (get game :key)
   :users (count (get game :users {}))})

(defn get-games
  ([] (get-games @GAMES))
  ([games] (->> games (vals) (map deref) (mapv game-descriptor))))

(defn unique [f]
  (let [seen (atom #{})]
    (fn [& args]
      (->> (repeatedly #(apply f args))
           (drop-while #(contains? (first (swap-vals! seen conj %)) %))
           (first)))))

(defn rand-name []
  (first (hazard/words 1 {:min 8 :max 8})))

(alter-var-root #'rand-name unique)

(defn new-game! []
  (let [id   (rand-name)
        game (atom {:key id :users {}})]
    (spawn-broadcast-loop game)
    (add-state-notifications game)
    (get-games (swap! GAMES assoc id game))))

(defmulti handle-user-event
  (fn [game event] (keyword (get event :kind))))

(defmethod handle-user-event :init [game event]
  {:kind :state-change :data (front-end-state @game)})

(defmethod handle-user-event :default [game event]
  (println "Received event for game" (get @game :key) event)
  {:ack true})

(defn deregister! [game channel]
  (let [sess (hash channel)]
    (let [result (swap! game update :users dissoc sess)]
      (when (empty? (:users result {}))
        (swap! GAMES dissoc (:key result))))))

(defn register! [game request channel]
  (let [user (friend/current-authentication request) sess (hash channel)]
    (swap! game assoc-in [:users sess] {:channel channel :user user :id sess})))

(defn connect! [request game-id]
  (if-some [game (get @GAMES game-id)]
    (with-channel request channel
      (register! game request channel)
      (async/go-loop []
        (if-some [message (async/<! channel)]
          (when-some [response (handle-user-event game (:message message))]
            (if (fn? response)
              (when-some [resolved (async/<! (async/thread (response)))]
                (async/>! channel resolved))
              (async/>! channel response))
            (recur))
          (deregister! game channel))))
    (response/not-found "No such game!")))