(defproject pairwell "0.1.0-SNAPSHOT"
  :description "Pair Well is an online pair programming nexus"
  :url "http://pairwell.heroku.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :uberjar-name "pairwell-standalone.jar"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.0"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [compojure "1.1.8"]
                 [bidi "1.10.4"]
                 [liberator "0.12.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/core.match "0.2.1"]
                 [org.clojure/clojurescript "0.0-2277"]
                 #_[clj-diff "1.0.0-SNAPSHOT"]
                 [com.taoensso/sente "0.15.1"]
                 [com.taoensso/encore "1.7.0"]
                 [com.facebook/react "0.11.1"]
                 [om "0.6.5"]
                 [sablono "0.2.18"]]
  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.3"]]}}
  :cljsbuild {:builds {:dev {:compiler {:output-to "resources/public/js/pairwell-dev.js"
                                        :output-dir "resources/public/js/out"
                                        :optimizations :none
                                        :source-map true
                                        :warnings true}}
                       :release {:compiler {:output-to "resources/public/js/pairwell.js"
                                            :optimizations :advanced
                                            :pretty-print false
                                            :preamble ["ga.js"
                                                       "react/react.js"
                                                       "jquery.js"
                                                       "bootstrap.js"
                                                       "howler.js"]
                                            :externs ["ga.js"
                                                      "react/externs/react.js"
                                                      "jquery.js"
                                                      "bootstrap.js"
                                                      "howler.js"]
                                            :warnings true
                                            :closure-warnings {:externs-validation :off
                                                               :non-standard-jsdoc :off}}}}})
