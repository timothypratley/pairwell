(ns pairwell.handler
  (:require [pairwell.services :as services]
            [compojure.core :refer :all]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [ring.middleware.reload :refer [wrap-reload]]))


(defroutes pairwell-routes
  (GET "/" req (redirect "index.html"))
  (GET  "/chsk" req (services/ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (services/ring-ajax-post req))
  (POST "/login" req (services/login! req))
  (resources "/")
  (not-found "Not found"))

(def handler
  (-> #'pairwell-routes
      wrap-reload
      #_(wrap-anti-forgery
       {:read-token (fn [req] (-> req :params :csrf-token))})
      site))
