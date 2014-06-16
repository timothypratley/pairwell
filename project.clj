(defproject pairwell "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main pairwell.main
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]
                 [bidi "1.10.2"]
                 [liberator "0.11.0"]
                 [http-kit "2.1.18"]]
  :profiles {:dev {:plugins [[lein-cljsbuild "0.3.0"]]
                   :dependencies [[org.clojure/clojurescript "0.0-2234"]
                                  [om "0.6.4"]
                                  [sablono "0.2.17"]]}}
  :cljsbuild {:builds [{:id "dev"
                        :compiler {:output-to "resources/public/js/pairwell.js"
                                   :output-dir "resources/public/js/out"
                                   :optimizations :none
                                   :source-map true
                                   :warnings true}}]})
