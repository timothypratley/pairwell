(ns pairwell.services
  (:require [taoensso.sente :as sente]
            [clojure.core.match :refer [match]]
            [clojure.core.async :as async :refer [go go-loop <! >! put! chan]]))


(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! {})]
  (def ring-ajax-post ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk ch-recv)
  (def chsk-send! send-fn)
  (def connected-uids connected-uids))

(defn login! [{:keys [session {:keys [user-id] :as params}] :as ring-request}]
  (println "Login request: " params)
  {:status 200 :session (assoc session :uid user-id)})

(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid (:uid session)
        [id data :as ev] event]

    (println "Event:" ev)
    (match [id data]
    :else
    (do (println "Unmatched event:" ev)
        (when-not (:dummy-reply-fn? (meta ?reply-fn))
          (?reply-fn {:umatched-event-as-echoed-from-from-server ev}))))))

(defonce chsk-router
  (sente/start-chsk-router-loop! event-msg-handler ch-chsk))

(defonce broadcaster
  (go-loop []
    (<! (async/timeout 10000))
    (println (format "Broadcasting server>user: %s" @connected-uids))
    (doseq [uid (:any @connected-uids)]
      (chsk-send! uid
                  [:pairwell/model {:my-cards [{:topic "anything"
                                                :until "9pm"}]
                                    :invitations {:sent [{:to "spammer1"}]
                                                  :received [{:from "spammer2"}]}}]))
    (recur)))
