(ns pairwell.services
  (:require [pairwell.views :as views]
            [taoensso.sente :as sente]
            [clojure.data :as data]
            [clojure.core.match :refer [match]]))


;TODO: this can be evaluated on code reload :(
(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! {})]
  (defonce ring-ajax-post ajax-post-fn)
  (defonce ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (defonce ch-chsk ch-recv)
  (defonce chsk-send! send-fn)
  (defonce connected-uids connected-uids))

(defn login! [ring-request]
  (let [{:keys [session params]} ring-request
        {:keys [user-id]} params]
    (println "Login request: " params)
    {:status 200 :session (assoc session :uid user-id)}))

(defonce user-states (ref {"Testbot" {:activities ["Test activity"]}}))
(defonce user-views (ref {}))

(defn update
  "Makes view updates in response to a user's app-state changes."
  [uid app-state]
  (dosync
   (if app-state
     (alter user-states assoc uid app-state)
     (alter user-states dissoc uid))
   (ref-set user-views (views/states->views @user-states (:any @connected-uids)))))

(defn user-views-watch
  [k r a b]
  (let [[only-in-a only-in-b] (data/diff a b)]
    (doseq [uid (set (concat (keys only-in-a) (keys only-in-b)))
            :when ((:any @connected-uids) uid)]
      (chsk-send! uid [:pairwell/model (b uid)]))))
(add-watch user-views :uvw user-views-watch)


(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid (:uid session)
        [id data :as ev] event]

    (println "Event:" ev)
    (match [id data]
           [:chsk/ws-ping _]
           :ignore

           [:pairwell/app-state app-state]
           (if uid
             (update uid app-state)
             (println "Received app-state but no uid."))

           [:chsk/uidport-close _]
           (update uid nil)

           :else
           (do (println "Unmatched event:" ev)
               (when-not (:dummy-reply-fn? (meta ?reply-fn))
                 (?reply-fn {:umatched-event-as-echoed-from-from-server ev}))))))

(defonce chsk-router
  (sente/start-chsk-router-loop! event-msg-handler ch-chsk))
