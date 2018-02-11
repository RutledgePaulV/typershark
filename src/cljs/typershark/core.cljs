(ns typershark.core
  (:require [reagent.core :as r]
            [ajax.core :as a]
            [typershark.navigation :as nav]
            [typershark.game :as game]))

(def *state* (r/atom {:games []}))

(defn GET [uri cb]
  (a/GET uri {:response-format :json :keywords? true :handler cb}))

(defn POST [uri cb]
  (a/POST uri {:response-format :json :keywords? true :handler cb}))

(defn game-item [game]
  (let [description (str (get game :key) " (" (get game :users 0) " users)")]
    [:li [:a {:href (str "/games/" (:key game))} description]]))

(defn create-game! [cb]
  (POST "/api/games" (fn [games] (swap! *state* assoc :games games) (cb (last games)))))

(defn get-games [cb]
  (GET "/api/games" (fn [games] (swap! *state* merge {:games games}) (cb))))

(defn goto-game [game]
  (nav/navigate! :typershark/games {:id (:key game)}))

(defn new-game []
  [:a {:style    {:color "black"}
       :on-click (partial create-game! goto-game)}
   "New Game"])

(defn game-list []
  [:ul
   (for [game (:games @*state*)] [game-item game])
   [:li [new-game]]])

(defn overlay []
  [:div.overlay.open [:nav.overlay-menu [game-list]]])

(defn game [id]
  [:div (str "Game " id)])

(defn loading []
  [:div.loading "loading..."])

(defn four-o-four []
  [:div "Not found."])

(defn get-root []
  (.getElementById js/document "application"))

(defn on-load [fun]
  (r/render [loading] (get-root) fun))

(defmethod nav/on-navigate :typershark/menu [_ _ _]
  (on-load #(get-games (fn [] (r/render [overlay] (get-root))))))

(defmethod nav/on-navigate :typershark/games [_ {:keys [id]} _]
  (on-load #(r/render [game id] (get-root) (partial game/attach! id))))

(defmethod nav/on-navigate :typershark/not-found [event params query]
  (on-load #(r/render [four-o-four] (get-root))))

(defmethod nav/on-navigate :typershark/default [event params query]
  (nav/navigate! :typershark/not-found))


(enable-console-print!)
(nav/setup-navigation!)

