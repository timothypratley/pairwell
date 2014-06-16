(ns pairwell.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.reload :refer [wrap-reload]]))


(defroutes pairwell-routes
  (GET "/" req (redirect "index.html"))
  (resources "/")
  (not-found "Not found"))

(def handler
  (if :dev
    (wrap-reload (site #'pairwell-routes))
    (site pairwell-routes)))
