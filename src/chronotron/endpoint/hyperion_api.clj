(ns chronotron.endpoint.hyperion-api
  (:require [compojure.core :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ring.util.response :as r]
            [chronotron.component.hyperion-component :as hyperion]))

;; https://blog.interlinked.org/programming/clojure_rest.html

(def welcome-page
  (io/resource "chronotron/endpoint/hyperion_api/hyperion.html"))

;;curl  192.168.1.230:3000/hyperion -H "Content-Type: application/json" -d '{ "command" : "color", "color" : [255,0,0], "priority" : 450 }'

(defn api-command [hyperion params]
  (log/info "Api: " params)
  (hyperion/queue-command hyperion params)
  )

(defn hyperion-api-endpoint [config]
  (routes
   (GET "/" [] welcome-page)
   (context "/hyperion" []
            (let [hc (:hyperion config)]
              (defroutes hyperion-routes
                (POST "/command" {params :params} (api-command hc params))
                (POST "/color" {params :params}
                      (hyperion/set-color hc (get params "color")))
                (POST "/effect" {params :params}
                      (hyperion/effect hc (get params "name")))
                (POST "/clear" {params :params} (hyperion/clear hc))
                (GET "/clear" [] (hyperion/clear hc))
                (POST "/clearall" {params :params} (hyperion/clearall hc))
                (GET "/clearall" [] (hyperion/clearall hc))
                (GET "/serverinfo" [] (->
                                       (r/response (hyperion/server-info (:hyperion config)))
                                       (r/header "Content-type" "application/json")
                                       ))
                (GET "/" [] (str "Post JSON command to /hyperion/command")))))
   )
  )
