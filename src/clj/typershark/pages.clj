(ns typershark.pages
  (:require [hiccup.page :refer :all]
            [hiccup.core :refer :all]
            [ring.util.anti-forgery :as anti-forgery]))


(defn index-page []
  (html5
    [:html
     [:head
      [:link {:sizes "32x32", :href "favicon-32x32.png" :type "image/png" :rel "icon"}]
      [:link {:sizes "16x16" :href "favicon-16x16.png" :type "image/png" :rel "icon"}]
      (include-css "css/styles.css")]
     [:body (include-js "js/main.js")]]))

(defn login-page []
  (html5
    [:html
     [:head
      [:link {:sizes "32x32", :href "favicon-32x32.png" :type "image/png" :rel "icon"}]
      [:link {:sizes "16x16" :href "favicon-16x16.png" :type "image/png" :rel "icon"}]
      (include-css "https://fonts.googleapis.com/css?family=Open+Sans:400,700")
      (include-css "css/login.css")]]
    [:body.align
     [:form.form.login {:action "/login" :method "POST"}
      [:div.form-field
       [:img.icon {:src "/color.png"}]]
      [:div.form-field
       [:label
        [:svg {:class "icon"}
         [:use {"xmlns:xlink" "http://www.w3.org/1999/xlink" "xlink:href" "#user"}]]]
       [:input.form-input {:type "text" :name "username" :autofocus "autofocus" :autocomplete false}]]
      (anti-forgery/anti-forgery-field)]
     [:svg {:xmlns "http://www.w3.org/2000/svg", :class "icons"}
      [:symbol {:id "user", :viewbox "0 0 1792 1792"}
       [:path {:d "M1600 1405q0 120-73 189.5t-194 69.5H459q-121 0-194-69.5T192 1405q0-53 3.5-103.5t14-109T236 1084t43-97.5 62-81 85.5-53.5T538 832q9 0 42 21.5t74.5 48 108 48T896 971t133.5-21.5 108-48 74.5-48 42-21.5q61 0 111.5 20t85.5 53.5 62 81 43 97.5 26.5 108.5 14 109 3.5 103.5zm-320-893q0 159-112.5 271.5T896 896 624.5 783.5 512 512t112.5-271.5T896 128t271.5 112.5T1280 512z"}]]]]))