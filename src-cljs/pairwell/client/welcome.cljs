(ns pairwell.client.welcome
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.bindom :as bindom]
            [taoensso.encore :refer [logf]]))


(defn welcome
  "Landing page introduction and login."
  [app-state]
  [:div.jumbotron
   [:div.row
    [:div.col-md-7
     [:h1 "Welcome to" [:br] "Pair Well."]
     [:p
      "Collaborate with friends and people with shared interests. "
      "Tinker, learn, work, or play games together online. "
      "Pair up now!"]]
    [:div.col-md-5
     [:form
      {:role "form"
       :on-submit (bindom/form
                   (fn [{:keys [username contact card-title]}]
                     (if-let [error (cond
                                     (empty? username)
                                     "Please enter your name."
                                     (empty? contact)
                                     "Please enter contact preference."
                                     (empty? card-title)
                                     "Please enter what you want to do.")]
                       (swap! app-state assoc :error error)
                       (do (comm/login username)
                           (swap! app-state assoc
                                  :page :matching
                                  :username username
                                  :contact contact
                                  :activities #{card-title})
                           (comm/chsk-send! [:pairwell/hello])))))}
      [:div.form-group
       [:div.input-group.input-group-lg
        [:span.input-group-addon [:span.glyphicon.glyphicon-user]]
        [:input.form-control {:name "username"
                              :placeholder "My name"}]]]
      [:div.form-group
       [:div.input-group.input-group-lg
        [:span.input-group-addon [:span.glyphicon.glyphicon-facetime-video]]
        [:input.form-control {:name "contact"
                              :placeholder "Contact me by"}]]]
      [:div.form-group
       [:div.input-group.input-group-lg
        [:span.input-group-addon [:span.glyphicon.glyphicon-pencil]]
        [:input.form-control {:name "card-title"
                              :placeholder "I want to"}]]]
      (when-let [e (@app-state :error)]
        [:p [:span.glyphicon.glyphicon-exclamation-sign] " " e])
      [:button.btn.btn-primary.btn-lg.btn-block {:type "submit"} "Start"]]]]])
