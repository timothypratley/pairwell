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

(def user-cards (ref {"John" {"Test topic" {:until "5pm"
                                            :interest #{"Tim" "Tammy"}}
                              "Anything" {:until "6pm"}}
                      "Jane" {"Other test topic" {:until "7pm"}}}))

(defn available
  "All cards except mine."
  [uid]
  (let [others (dissoc @user-cards uid)]
    (mapcat (fn [[owner cards]]
              (map (fn [[topic props]]
                     (assoc props
                       :with owner
                       :topic topic))
                     cards))
            others)))

(defn interest
  [uid]
  (filter #(contains? (:interest %) uid)
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
  (println uid)
  (dosync
   ; TODO: dissoc interest, and don't overwrite interest
   (doseq [card (:interest app-state)]
     (alter user-cards update-in [(:with card) (:topic card) :interest]
            (fnil conj #{}) uid))
   (alter user-cards assoc
          uid (:my-cards app-state)))
  (chsk-send! uid [:pairwell/model (view uid)]))

(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (try 
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
                   (?reply-fn {:umatched-event-as-echoed-from-from-server ev})))))
    (catch Exception e
      (println e))))

(defonce chsk-router
  (sente/start-chsk-router-loop! event-msg-handler ch-chsk))
