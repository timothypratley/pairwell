(ns pairwell.client.welcome
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.bindom :as bindom]
            [taoensso.encore :refer [logf]]))


(defn welcome
  "Returns the landing page."
  [app-state]
  [:form {:role "form"}
   [:input {:placeholder "Enter your name"
            :on-change (bindom/setter app-state [:username])}]
   [:br]
   [:span (@app-state :error)]
   [:span (str (@app-state :open?))]
   [:br]
   [:button.btn.btn-primary.btn-lg.btn-block
    {:type "button"
     :on-click (fn [e]
                 (if (@app-state :username)
                   (do (comm/login (:username @app-state))
                       (swap! app-state assoc :page :matching)
                       (comm/chsk-send! [:pairwell/hello]))
                   (swap! app-state assoc
                          :error "Please enter your name first.")))}
    "Start"]])
