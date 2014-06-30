(ns pairwell.services
  (:require [taoensso.sente :as sente]
            [clojure.core.match :refer [match]]
            [clojure.core.async :as async :refer [go go-loop <! >! put! chan]]))


;TODO: this can be evaluated on code reload :(
(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! {})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))

(defn login! [ring-request]
  (let [{:keys [session params]} ring-request
        {:keys [user-id]} params]
    (println "Login request: " params)
    {:status 200 :session (assoc session :uid user-id)}))

(def user-cards (ref {"John" [{:topic "Test topic"
                               :until "5pm"
                               :interest #{"Tim"}}]
                      "Jane" [{:topic "Other test topic"
                               :until "7pm"}]}))

(defn available
  "All cards except mine."
  [uid]
  (mapcat (fn [[owner cards]]
            (map #(assoc % :with owner) cards))
          (dissoc @user-cards uid)))

(defn interest
  [uid]
  (filter #((:interest % #{}) uid)
          (available uid)))

(defn view
  "The model visible to me."
  [uid]
  (println "Sending view for UID" uid)
  {:my-cards (@user-cards uid)
   :interest (interest uid)
   :available (available uid)})

(defn update
  "Make updates in response to my app-state changes"
  [uid app-state]
  (when (not= (@user-cards uid) (:new-card app-state))
    (dosync
     (alter user-cards assoc
            uid (:new-card app-state)))
    (chsk-send! uid [:pairwell/model (view uid)])))

(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid (:uid session)
        [id data :as ev] event]

    (println "Event:" ev)
    (match [id data]
           [:pairwell/hello x]
           (chsk-send! uid [:pairwell/model (view uid)])

           [:pairwell/app-state app-state]
           (update uid app-state)

           :else
           (do (println "Unmatched event:" ev)
               (when-not (:dummy-reply-fn? (meta ?reply-fn))
                 (?reply-fn {:umatched-event-as-echoed-from-from-server ev}))))))

(defonce chsk-router
  (sente/start-chsk-router-loop! event-msg-handler ch-chsk))
