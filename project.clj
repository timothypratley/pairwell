(defproject pairwell "0.1.0-SNAPSHOT"
  :description "Pair Well is an online pair programming nexus"
  :url "http://pairwell.heroku.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :uberjar-name "pairwell-standalone.jar"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.1"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [compojure "1.1.8"]
                 [http-kit "2.1.19"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/core.match "0.2.2"]
                 [org.clojure/clojurescript "0.0-2322"]
                 #_[clj-diff "1.0.0-SNAPSHOT"]
                 [com.taoensso/sente "1.0.0" :exclusions [org.clojure/clojure]]
                 [com.taoensso/encore "1.8.0" :exclusions [org.clojure/clojure]]
                 [com.facebook/react "0.11.1"]
                 [om "0.7.1"]
                 [sablono "0.2.22"]]
  :hooks [leiningen.cljsbuild]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.3"]]}}
  :cljsbuild {:builds {:dev {:compiler {:output-to "resources/public/js/pairwell_dev.js"
                                        :output-dir "resources/public/js/out"
                                        :optimizations :none
                                        :source-map true
                                        :warnings true}}
                       :release {:compiler {:output-to "resources/public/js/pairwell.js"
                                            :optimizations :advanced
                                            :pretty-print false
                                            :preamble ["ga.js"
                                                       "platform.js"
                                                       "react/react.js"
                                                       "jquery.js"
                                                       "bootstrap.js"
                                                       "howler.js"]
                                            :externs ["ga.js"
                                                      "platform.js"
                                                      "react/externs/react.js"
                                                      "jquery.js"
                                                      "bootstrap.js"
                                                      "howler.js"]
                                            :warnings true
                                            :closure-warnings {:externs-validation :off
                                                               :non-standard-jsdoc :off}}}}})
