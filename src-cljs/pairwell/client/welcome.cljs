(ns pairwell.client.welcome
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.bindom :as bindom]
            [taoensso.encore :refer [logf]]))


(defn welcome
  "Returns the landing page."
  [app-state]
  [:form {:role "form"
          :on-submit (bindom/form
                      (fn [{:keys [username contact]}]
                        (cond
                         (nil? username)
                         (swap! app-state assoc
                                :error "Please enter your name first.")

                         (nil? contact)
                         (swap! app-state assoc
                                :error "Please enter contact preference.")

                         :else
                         (do (comm/login username)
                             (swap! app-state assoc
                                    :page :matching
                                    :contact contact)
                             (comm/chsk-send! [:pairwell/hello])))))}
   [:input {:name "username"
            :placeholder "Enter your name"}]
   [:input {:name "contact"
            :placeholder "Contact me by"}]
   [:br]
   [:span (@app-state :error)]
   [:span (str (@app-state :open?))]
   [:br]
   [:button.btn.btn-primary.btn-lg.btn-block {:type "submit"} "Start"]])
