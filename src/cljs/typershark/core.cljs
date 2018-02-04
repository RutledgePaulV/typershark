(ns typershark.core
  (:require [reagent.core :as r]
            [ajax.core :as a]))


(defn game-item [game]
  [:a {:href (str "/games?id=" (:id game))} (get game :id)])

(defn main-component []
  [:div [:h3 "I am a component!"]])

(defn mount! [games]
  (r/render [main-component]
    (.-body js/document)))

(a/GET "/games" {:handler mount!})