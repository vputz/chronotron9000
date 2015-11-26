(ns chronotron.component.hyperion-component
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :as async :refer [<! >! <!! >!! chan go go-loop close!]]
            [chronotron.hyperion :as hyperion]
            [clojure.tools.logging :as log]))

(defrecord Hyperion-component [host port priority]
  component/Lifecycle

  (start [component]
    (log/info "starting Hyperion service on host " host ", port " port ", priority " priority)
    (let [c2
          (assoc component
                 :hyperion-service (hyperion/->Hyperion-service host port priority)
                 :in (chan)
                 :out (chan))]
      (go-loop []
        (when-let [cmd (<! (:in c2))]
          (log/info "Hyperion received " cmd)
          (>! (:out c2) (hyperion/send-command (:hyperion-service c2) cmd))
          (recur)))
      c2))

  (stop [component]
    (log/info "stopping Hyperion service")
    (when-let [in (:in component)] (close! in))
    (when-let [out (:out component)] (close! out))
    component
    ))

(defn new-hyperion-component [config]
  (map->Hyperion-component (select-keys config [:host :port :priority])))

(defn queue-command [hyperion-component command-data]
  (>!! (:in hyperion-component) command-data)
  (<!! (:out hyperion-component)))
