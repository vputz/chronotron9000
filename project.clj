(defproject chronotron "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.145"]
                 [com.stuartsierra/component "0.3.0"]
                 [compojure "1.4.0"]
                 [duct "0.4.5"]
                 [environ "1.0.1"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.3.0"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.0"]
                 [org.slf4j/slf4j-nop "1.7.12"]
                 [org.clojure/data.json "0.2.6"]
                 [org.omcljs/om "0.9.0"]
                 [org.webjars/normalize.css "3.0.2"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.374"]]
  :plugins [[lein-environ "1.0.1"]
            [lein-gen "0.2.2"]
            [lein-cljsbuild "1.1.0"]
            [lein-figwheel "0.5.0-2"]
            [cider/cider-nrepl "0.10.0-SNAPSHOT"]]
  :generators [[duct/generators "0.4.5"]]
  :duct {:ns-prefix chronotron}
  :main ^:skip-aot chronotron.main
  :target-path "target/%s/"
  :resource-paths ["resources" "target/cljsbuild"]
  :prep-tasks [["javac"] ["cljsbuild" "once"] ["compile"]]
  :cljsbuild
  {:builds
   {:hyperion-dev {:jar true
               :figwheel { :on-jsload "om-tut.core/on-js-reload"}
                   :source-paths ["src"]

               :compiler {:output-to "target/cljsbuild/chronotron/public/js/hyperion.js"
                          :optimizations :none}}}}
  :aliases {"gen"   ["generate"]
            "setup" ["do" ["generate" "locals"]]}
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :repl {:resource-paths ^:replace ["resources" "target/figwheel"]
          :prep-tasks     ^:replace [["javac"] ["compile"]]}
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :dependencies [[reloaded.repl "0.2.1"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [eftest "0.1.0"]
                                  [kerodon "0.7.0"]
                                  [duct/figwheel-component "0.3.1"]
                                  [figwheel "0.4.0"]]
                   :env {:port 3000}}
   :project/test  {}})
