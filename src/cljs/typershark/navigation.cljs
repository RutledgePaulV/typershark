(ns typershark.navigation
  (:require [bide.core :as b]
            [clojure.string :as string]
            [clojure.string :as string]
            [goog.events :as events])
  (:import goog.history.Html5History
           goog.Uri))

(def router
  (b/router
    [["/" :typershark/menu]
     ["/games/:id" :typershark/games]
     ["/404" :typershark/not-found]]))

(def on-leave
  (atom nil))

(defn on-leave! [f]
  (reset! on-leave f))

(defmulti
  on-navigate
  (fn [name _ _]
    (when-some [old (first (reset-vals! on-leave nil))]
      (old))
    name))

(defn get-history []
  (let [history (Html5History.)]
    (events/listen
      js/document "click"
      (fn [e]
        (let [path (.getPath (.parse Uri (.-href (.-target e))))]
          (when-not (string/blank? path)
            (.preventDefault e)
            (. history (setToken path (.-title (.-target e))))))))
    history))

(defn setup-navigation! []
  (b/start! router
    {:default      :typershark/not-found
     :on-navigate  on-navigate
     :html5?       true
     :html5history get-history}))

(defn navigate!
  ([destination] (navigate! destination {}))
  ([destination params] (b/navigate! router destination params)))
