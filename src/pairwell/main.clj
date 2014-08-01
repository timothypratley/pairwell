(ns pairwell.main
  (:require [pairwell.handler :refer [handler]]
            [org.httpkit.server :refer [run-server]]))


(defonce system nil)

(defn start [port]
  {:pre [(not (:stop system))]}
  (alter-var-root #'system assoc 
                  :port port
                  :stop (run-server #'handler {:port port}))
  (println "Server started on port" port "."))

(defn stop []
  {:pre [(:stop system)]}
  ((:stop system))
  (println "Server stopped.")
  (alter-var-root #'system dissoc :stop))

(defn -main [& [port]]
  (start (if port
           (Integer. port)
           8080)))
