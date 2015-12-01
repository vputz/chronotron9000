(ns ^:figwheel-always chronotron.hyperion
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [<! put! chan]]
            [ajax.core :refer [GET POST]])
  (:import [goog.net Jsonp]
           [goog Uri]))

(enable-console-print!)

(println "Edits to this text should show up in developer console.")

(defonce app-state (atom {:text "Placeholder"
                          :host "localhost"
                          :port 3000}))

(def command-chan (chan))

(defn queue-command
  ([cmd]
   (put! command-chan {:command cmd}))
  ([cmd params]
   (put! command-chan {:command cmd :params params})))

(defn set-app-effects [serverinfo]
  (let [effects (:effects (:info serverinfo))]
    (swap! app-state assoc :effects (:effects (:info serverinfo)))))

                                        ; Get the list of effects from the server and populate the app state
;; (GET "http://localhost:3000/hyperion/serverinfo" {:handler (fn [response] (set-app-effects response))
;;                                                   :response-format :json
;;                                                   :keywords? true})

;; (defn jsonp [uri]
;;   (let [out (chan)
;;         req (Jsonp. (Uri. uri))]
;;     (.send req nil (fn [res] (put! out res)))
;;     out
;;     ))

;;(go (prn (<! (jsonp "http://localhost:3000/hyperion/serverinfo"))))

(defn get-chan [url]
  (let [out (chan)]
    (GET url {:handler (fn [response] (put! out response))
              :response-format :json
              :keywords? true})
    out))

(defn app-url [path] (str "http://"
                          (:host @app-state) ":" (:port @app-state)
                          path))


(defn color-view [data owner]
(reify om/IRender
     (render [_]
       (dom/div {}
                (dom/button #js {:id "clear"
                                 :onClick (fn [e]
                                            (queue-command "clear")
                                            (.stopPropagation e))}
                            (str "Clear"))
                (dom/p {})
                (dom/input #js {:type "text" :ref "red" :value (:color-red data)})
                (dom/text {} "Red")
                (dom/p {})
                (dom/input #js {:type "text" :ref "green" :value (:color-green data)})
                (dom/text {} "Green")
                (dom/p {})
                (dom/input #js {:type "text" :ref "blue" :value (:color-green data)})
                (dom/text {} "Blue")
                (dom/p {})
                (dom/button #js {:id "set-color"
                                 :onClick (fn [e]
                                            (queue-command "color" [255 0 0])
                                            (.stopPropagation e))}
                            (str "Set Color"))
                )
       
       ;; (dom/ul {} (for [effect (:effects data)]
       ;;              (dom/li {:react-key (:name effect)} (:name  effect))
       ;;              ))
       )))

(defn handle-change [e owner {:keys [selected-effect]}]
  (om/set-state! owner :selected-effect (.. e -target -value)))

(defn effect-view [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:selected_effect nil})
    om/IRenderState
    (render-state [this state]
      (dom/div {}
               (dom/select #js {:id "effect"
                            :value (:selected_effect state)
                            :onChange #(handle-change % owner state)}
                           (for [effect (:effects data)]
                             (dom/option {:react-key (:name effect)} (:name effect))))
               (dom/button #js {:id "send-effect"
                                :onClick (fn [e]
                                           (queue-command "effect" (:selected-effect state)))
                                :react-key "send-effect"}
                           (str "Send Effect")))
      ;; (dom/ul {} (for [effect (:effects data)]
      ;;              (dom/li {:react-key (:name effect)} (:name  effect))
      ;;              ))
      )))

(om/root
 effect-view
 app-state
 {:target (. js/document (getElementById "effect"))})

(om/root
 color-view
 app-state
 {:target (. js/document (getElementById "color"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  (swap! app-state update-in [:__figwheel_counter] inc)
  (swap! app-state assoc :text "tim")
  (prn "reload")
)
 
(defn init [] 
  (go (set-app-effects (<! (get-chan (app-url "/hyperion/serverinfo"))))))

(defmulti handle-command :command)
(defmethod handle-command "color" [cmd]
  (POST "/hyperion/color" {:format :json
                           :params {:color (:params cmd)}}))
(defmethod handle-command "clear" [cmd]
  (POST "/hyperion/clear"))
(defmethod handle-command "effect" [cmd]
  (POST "/hyperion/effect" {:format :json
                            :params {:name (:params cmd)}}))
(init)
(go-loop []
  (handle-command (<! command-chan))
  (recur))
