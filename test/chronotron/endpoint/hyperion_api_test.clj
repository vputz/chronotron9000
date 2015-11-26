(ns chronotron.endpoint.hyperion-api-test
  (:require [com.stuartsierra.component :as component]
            [clojure.test :refer :all]
            [kerodon.core :refer :all]
            [kerodon.test :refer :all]
            [chronotron.endpoint.hyperion-api :as hyperion-api]))

(def handler
  (hyperion-api/hyperion-api-endpoint {}))

(deftest smoke-test
  (testing "index page exists"
    (-> (session handler)
        (visit "/")
        (has (status? 200) "page exists")
        )))
