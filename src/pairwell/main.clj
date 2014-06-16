(ns pairwell.main
  (:require [pairwell.handler :refer [handler]]
            [org.httpkit.server :refer [run-server]]))


(defn new-system
  []
  {})

(defn start
  [system]
  {:pre [(not (system :stop))]}
  (assoc system :stop
         (run-server handler {:port 8080})))

(defn stop
  [{:keys [stop] :as system}]
  {:pre [stop]}
  (stop)
  (dissoc system :stop))

(defn -main
  [& args]
  (start (new-system)))
