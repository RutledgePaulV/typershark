(ns typershark.pages
  (:require [hiccup.page :refer :all]
            [hiccup.core :refer :all]))



(defn index-page []
  (html5
    [:html
     [:head (include-css "css/styles.css")]
     [:body (include-js "js/main.js")]]))