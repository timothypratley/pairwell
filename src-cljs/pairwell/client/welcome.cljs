(ns pairwell.client.welcome
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.bindom :as bindom]))


(defn welcome
  "Returns the landing page."
  [app-state]
  [:form {:roleName "form"}
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
                       (swap! app-state assoc :page :matching))
                   (swap! app-state assoc
                          :error "Please enter your name first.")))}
    "Start"]
   [:br]
   [:input]
   [:br]
   [:select 
    [:option "foo"]
    [:option "bar"]]])
