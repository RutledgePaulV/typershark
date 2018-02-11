(ns typershark.sketches
  (:require [play-cljs.core :as play]
            [play-cljs.options :as options]))



(defmethod play/draw-sketch! :gradient [game ^js/p5 renderer content parent-opts]
  (let [[_ opts & children] content
        {:keys [colors] :as opts} (options/update-opts opts parent-opts options/basic-defaults)]
    (.push renderer)
    ; DO THINGS
    (play/draw-sketch! game renderer children opts)
    (.pop renderer)))