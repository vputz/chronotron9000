(ns chronotron.system
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            [ring.middleware.json :refer [wrap-json-params wrap-json-response]]
            [environ.core :refer [env]]
            [chronotron.component.hyperion-component :as hyperion :refer [new-hyperion-component]]
            [chronotron.endpoint.hyperion-api :refer [hyperion-api-endpoint]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-webjars]
                      [wrap-json-params]
                      [wrap-json-response]
                      [wrap-defaults :defaults]]
         :not-found  (io/resource "chronotron/errors/404.html")
         :defaults   (meta-merge site-defaults {:static {:resources "chronotron/public"}
                                                :security {:anti-forgery false}})}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :hyperion (new-hyperion-component (:hyperion config))
         :hyperion-api (endpoint-component hyperion-api-endpoint))
        (component/system-using
         {:http [:app]
          :app  [:hyperion-api]
          :hyperion []
          :hyperion-api [:hyperion :hyperion]}))))
