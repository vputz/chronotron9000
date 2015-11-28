(ns ^:figwheel-always chronotron.hyperion
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(println "Edits to this text should show up in developer console.")

(defn serverinfo [] (prn "bob") (:response  (GET "/hyperion/serverinfo" {:response-format :json})))

(defonce app-state (atom {:text "Placeholder"}))

(defn set-app-effects [serverinfo]
  (let [effects (:effects (:info serverinfo))]
    (prn effects)
    (swap! app-state assoc :effects (:effects (:info serverinfo)))))
; Get the list of effects from the server and populate the app state
(GET "http://localhost:3000/hyperion/serverinfo" {:handler (fn [response] (set-app-effects response))
                                                  :response-format :json
                                                  :keywords? true})

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
        (dom/ul {} (for [effect (:effects data)]
                     (dom/li {} (:name  effect))  )))))
  app-state
  {:target (. js/document (getElementById "test"))})



(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  (swap! app-state assoc :text "tim")
  (prn "reload")
)

