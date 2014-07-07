(ns pairwell.services
  (:require [taoensso.sente :as sente]
            [clojure.core.match :refer [match]]
            [clojure.core.async :as async :refer [go go-loop <! >! put! chan]]))


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

;; TODO: reverse index interest in a watch
(defonce user-states (ref {"John" {:interest #{{:topic "Other test topic"
                                                :with "Jane"}}
                                   :cards {"Test topic" {:until "5pm"}
                                           "Anything" {:until "6pm"}}
                                   :contact "zoom 1234"}
                           "Jane" {:cards {"Other test topic" {:until "7pm"}}
                                   :contact "google hangout jane@gmail.com"}}))

;; TODO: save user views for diffing
(def user-views (ref {}))

(defn with-interest
  "Makes a card inclusive of interest."
  [uid owner topic props]
  (let [interested (for [[user {:keys [interest]}] @user-states
                        owner-topic interest
                        :when (= owner-topic {:with owner
                                              :topic topic})]
                     user)
        confirmed (get-in @user-states [owner :confirmed])]
    (assoc props
      :topic topic
      :with owner
      :interest interested
      ;; TODO: remove cards that are confirmed with someone else
      :contact (some (fn [user]
                       (when (and (= user confirmed)
                                  (or (= uid owner)
                                      (= uid user)))
                         (str {owner (get-in @user-states [owner :contact])
                               user (get-in @user-states [interested :contact])})))
                     interested))))

(defn cards
  "My cards."
  [uid]
  (for [[topic props] (get-in @user-states [uid :cards])]
    (with-interest uid uid topic props)))

(defn available
  "All cards except mine."
  [uid]
  (let [others (dissoc @user-states uid)]
    (for [[owner {:keys [cards]}] others
          [topic props] cards]
      (with-interest uid owner topic props))))

(defn view
  "The model visible to me."
  [uid]
  (println "Sending view for UID" uid)
  {:cards (cards uid)
   :available (available uid)})

(defn update
  "Make updates in response to my app-state changes."
  [uid app-state]
  (when uid
    (println "Update for" uid)
    (dosync
     (alter user-states assoc uid app-state))
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
