(ns pairwell.client.welcome
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.bindom :as bindom]
            [taoensso.encore :refer [logf]]))


(defn welcome
  "Returns the landing page."
  [app-state]
  [:form
   {:role "form"
    :on-submit (bindom/form
                (fn [{:keys [username contact]}]
                  (if-let [error (cond
                                  (empty? username)
                                  "Please enter your name."
                                  (empty? contact)
                                  "Please enter contact preference.")]
                    (swap! app-state assoc :error error)
                    (do (comm/login username)
                        (swap! app-state assoc
                               :page :matching
                               :username username
                               :contact contact)
                        (comm/chsk-send! [:pairwell/hello])))))}
   [:div.form-group
    [:label.control-label {:for "username"} "Display name"]
    [:div.input-group.input-group-lg
     [:span.input-group-addon.glyphicon.glyphicon-user]
     [:input.form-control {:name "username"
                           :placeholder "Enter your name"}]]
    [:p.help-block
     "Your display name is publicly displayed when creating pair cards, or expressing interst in a card."]]
   [:div.form-group
    [:label.control-label {:for "contact"} "Contact instructions"]
    [:div.input-group.input-group-lg
     [:span.input-group-addon.glyphicon.glyphicon-facetime-video]
     [:input.form-control {:name "contact"
                           :placeholder "Contact me by"}]]
    [:p.help-block
     "Video or voice channel that you will be actively listening on. "
     "Example: https://zoom.us/j/xxxyyyzzz or Google Hangout text@example.com. "
     "Pair Well does not track or store your contact information, "
     "but will reveal it to people you confirm."]]
   (when-let [e (@app-state :error)]
     [:p [:span.glyphicon.glyphicon-exclamation-sign] " " e])
   [:button.btn.btn-primary.btn-lg.btn-block {:type "submit"} "Start"]])
