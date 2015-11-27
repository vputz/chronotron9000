(ns ^:figwheel-always chronotron.hyperion
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in developer console.")

(defonce app-state (atom {:text "Hello wurld!"}))

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
        (dom/h1 nil (:text data)))))
  app-state
  {:target (. js/document (getElementById "test"))})



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

