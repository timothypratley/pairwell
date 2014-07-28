(ns pairwell.client.communication
  (:require-macros
   [cljs.core.match.macros :refer [match]]
   [cljs.core.async.macros :as asyncm :refer [go go-loop]])
  (:require
   [cljs.core.match]
   [cljs.core.async :as async :refer [<! >! put! chan]]
   [taoensso.encore :as encore :refer [logf]]
   [taoensso.sente :as sente :refer [cb-success?]]))


(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" {:type :auto})]
  (def chsk chsk)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def chsk-state state))

(def model (atom {}))

(defn- event-handler [[id data :as ev] _]
  (logf "Event: %s" ev)
  (match [id data]
         [:chsk/recv [:pairwell/model m]]
         (reset! model m) 

         [:chsk/state {:first-open? true}]
         (logf "Channel socket successfully established!")

         [:chsk/state new-state]
         (logf "Chsk state change: %s" new-state)

         [:chsk/recv payload]
         (logf "Push event from server: %s" payload)

         :else
         (logf "Unmatched event: %s" ev)))

(defonce chsk-router
  (sente/start-chsk-router-loop! event-handler ch-chsk))

(defn send-app-state [m]
  (chsk-send! [:pairwell/app-state m]))

(defn login [user-id]
  (logf "Logging in with user-id %s" user-id)
  (encore/ajax-lite "/login" {:method :post
                              :params {:user-id (str user-id)
                                       :csrf-token (:csrf-token @chsk-state)}}
                    (fn [ajax-resp]
                      (logf "Ajax login response: %s" ajax-resp)))
  (sente/chsk-reconnect! chsk))
