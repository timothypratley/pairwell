(ns pairwell.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [pairwell.services :as services]
            [ring.middleware.anti-forgery :as anti-forgery]
            [ring.middleware.reload :as reload]
            [ring.util.response :as response]))


(defroutes pairwell-routes
  (GET "/" req (response/resource-response "public/index.html"))
  (GET  "/chsk" req (services/ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (services/ring-ajax-post req))
  (POST "/login" req (services/login! req))
  (route/resources "/")
  (route/not-found "Not found"))

(def handler
  (-> #'pairwell-routes
      reload/wrap-reload
      #_(anti-forgery/wrap-anti-forgery
         {:read-token (fn [req] (-> req :params :csrf-token))})
      handler/site))
