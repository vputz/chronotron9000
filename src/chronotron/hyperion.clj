(ns chronotron.hyperion
  (:require [clojure.data.json :as json])
  (:use [clojure.java.io :only [output-stream
                                input-stream
                                writer
                                reader]]))

(defrecord Hyperion-service [address port priority])
                                        ; def port 19444
; fluellen 192.168.1.65
(def hs (->Hyperion-service "192.168.1.65" 19444 400))
                                        ; https://stackoverflow.com/questions/23487849/clojure-tcp-client-using-java-socket
                                        ; https://codethat.wordpress.com/2011/01/05/coffee-time-with-java-part-1-and-a-half/
(def cmd {:command "color" :color [255,0,0] :priority 500})
(def max-message-size 500)

(defn send-command
  ([service command-data]
   (with-open [socket (java.net.Socket. (:address service) (:port service))
               writer (writer socket)
               reader (reader socket)]
     (prn command-data)
     (.setSoTimeout socket 1000)
     (json/write command-data writer)
     (.append writer \newline)
     (.flush writer)
     (.readLine reader)
     ))
  
  ([service command data]
   (send-command service (assoc data :command command))))
    
;(send-command hs {:command "color" :color "red" :priority "9000"})

(defn set-color [hs color]
  (send-command hs "color" {:color color :priority (:priority hs)}))

(defn server-info [hs]
  (json/read-str (send-command hs "serverinfo" {})))

(defn clear [hs]
  (send-command hs "clear" {:priority (:priority hs)}))

(defn clear-all [hs]
  (send-command hs "clearall" {}))

(defn effect [hs effect-name]
  (send-command hs "effect" {:effect {:name effect-name} :priority (:priority hs)}))
