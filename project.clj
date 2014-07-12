(defproject pairwell "0.1.0-SNAPSHOT"
  :description "Pair Well is an online pair programming nexus"
  :url "http://pairwell.heroku.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main pairwell.main
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.0"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [compojure "1.1.8"]
                 [bidi "1.10.4"]
                 [liberator "0.11.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/core.match "0.2.1"]
                 [com.taoensso/sente "0.14.1"]]
  :profiles {:dev {:plugins [[lein-cljsbuild "0.3.0"]
                             [com.taoensso/encore "1.6.0"]]
                   :dependencies [[org.clojure/clojurescript "0.0-2234"]
                                  [om "0.6.4"]
                                  [sablono "0.2.17"]]}}
  :cljsbuild {:builds [{:id "dev"
                        :compiler {:output-to "resources/public/js/pairwell.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true
                                   :warnings true}}]})
