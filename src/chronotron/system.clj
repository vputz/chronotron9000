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
            [environ.core :refer [env]]
            [chronotron.component.hyperion-component :as hyperion :refer [new-hyperion-component]]
            [chronotron.endpoint.example :refer [example-endpoint]]))

(def env-config
  {:hyperion (some-> env :hyperion)})

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-webjars]
                      [wrap-defaults :defaults]]
         :not-found  (io/resource "chronotron/errors/404.html")
         :defaults   (meta-merge site-defaults {:static {:resources "chronotron/public"}})}})

(defn new-system [config]
  (let [config (meta-merge env-config base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :hyperion (new-hyperion-component (:hyperion config))
         :example (endpoint-component example-endpoint))
        (component/system-using
         {:http [:app]
          :app  [:example]
          :hyperion []
          :example []}))))
