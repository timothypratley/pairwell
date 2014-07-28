(ns pairwell.client.welcome
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.bindom :as bindom]
            [taoensso.encore :refer [logf]]))


(defn welcome
  "Returns the landing page."
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
                                  :cards {card-title {}})
                           (comm/chsk-send! [:pairwell/hello])))))}
      [:div.form-group
       [:div.input-group.input-group-lg
        [:span.input-group-addon.glyphicon.glyphicon-user]
        [:input.form-control {:name "username"
                              :placeholder "Enter your name"}]]]
      [:div.form-group
       [:div.input-group.input-group-lg
        [:span.input-group-addon.glyphicon.glyphicon-facetime-video]
        [:input.form-control {:name "contact"
                              :placeholder "Contact me by"}]]]
      [:div.form-group
       [:div.input-group.input-group-lg
        [:span.input-group-addon.glyphicon.glyphicon-pencil]
        [:input.form-control {:name "card-title"
                              :placeholder "I want to"}]]]
      (when-let [e (@app-state :error)]
        [:p [:span.glyphicon.glyphicon-exclamation-sign] " " e])
      [:button.btn.btn-primary.btn-lg.btn-block {:type "submit"} "Start"]]]]
   [:br]
   [:br]
   [:br]
   #_[:div.row
    [:div.embed-responsive.embed-responsive-16by9
     [:iframe.embed-responsive-item {:src "//www.youtube.com/embed/JKQZpVJQRT0"
                                     :allow-full-screen "allowfullscreen"}]]]])
