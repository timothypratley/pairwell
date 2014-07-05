(ns pairwell.main
  (:require [pairwell.handler :refer [handler]]
            [org.httpkit.server :refer [run-server]]))


(defn new-system []
  {:port 8080})

(defn start [system]
  {:pre [(not (system :stop))]}
  (let [server (run-server #'handler system)]
    (println "Server started.")
    (assoc system :stop server)))

(defn stop [{:keys [stop] :as system}]
  {:pre [stop]}
  (stop)
  (println "Server stopped.")
  (dissoc system :stop))

(def system (new-system))

(defn -main [& args]
  (alter-var-root #'system start))
