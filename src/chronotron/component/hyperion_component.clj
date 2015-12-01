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
                                        ; enhance the command with the
                                        ; service's priority if it's a
                                        ; command which requires
                                        ; priority AND the command
                                        ; data doesn't include it.
  (let [priority-commands #{"color" "clear" "effect"}
        priority-command (if (and (contains? priority-commands (:command command-data))
                                  (not (contains? command-data :priority)))
                           (assoc command-data :priority (:priority (:hyperion-service hyperion-component)))
                           command-data)]
    (>!! (:in hyperion-component) priority-command)
    (<!! (:out hyperion-component))))

(defn server-info [hyperion-component]
  (queue-command hyperion-component {:command "serverinfo"}))

(defn clear [hyperion-component]
  (queue-command hyperion-component {:command "clear" }))

(defn clearall [hyperion-component]
  (queue-command hyperion-component {:command "clearall"}))

(defn set-color [hyperion-component color]
  (queue-command hyperion-component {:command "color" :color color}))

(defn effect [hyperion-component effect-name]
  (queue-command hyperion-component {:command "effect" :effect {:name effect-name}}))
