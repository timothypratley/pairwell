(ns pairwell.client.communication
  (:require-macros
   [cljs.core.match.macros :refer [match]])
  (:require
   [cljs.core.match]
   #_[clj-diff.core :as diff]
   [taoensso.encore :as encore :refer [logf]]
   [taoensso.sente :as sente]))


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

         [:chsk/recv [:pairwell/patch p]]
         (do
           (logf "PATCH: %s" p)
           #_(logf "M: %s" (apply hash-map (diff/patch {} p)))
           #_(swap! model
                  (fn [m p]
                    (apply hash-map (diff/patch m p)))
                  p)
           #_(swap! model diff/patch p))

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
