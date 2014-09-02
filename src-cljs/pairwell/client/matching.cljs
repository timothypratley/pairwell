(ns pairwell.client.matching
  (:require [pairwell.client.bindom :as bindom]
            [pairwell.client.util :as util]
            [clojure.string :as string]
            [taoensso.encore :refer [logf]]
            [om.core :as om :include-macros true]))


(def set-conj (fnil conj #{}))

(defn join-or-leave
  [app-state activity]
  (if ((:activities @app-state) activity)
    [:button.btn.btn-warning.pull-right
     {:on-click #(swap! app-state update-in [:activities]
                        disj activity)}
     "Leave"]
    [:button.btn.btn-success.pull-right
     {:on-click #(swap! app-state update-in [:activities]
                        set-conj activity)}
     "Join"]))


(defn render-person
  [participation app-state person relationship contact]
  [:li.btn-group
   [:span.btn
    {:class (relationship {:my-pair "btn-success"
                           :invited "btn-info"
                           :inviting "btn-info"
                           :available "btn-default"
                           :hunted "btn-warning"}
                          "btn-default")}
    person]
   (when (and (not= participation :available)
              (#{:my-pair :invited :inviting :available :hunted} relationship))
     (let [confirmed (= (:confirmed @app-state) person)]
       [:button.btn.btn-default
        {:type "button"
         :on-click (if confirmed
                     #(swap! app-state dissoc :confirmed)
                     #(swap! app-state assoc :confirmed person))}
        [:span.glyphicon
         {:class (if (= (:confirmed @app-state) person)
                   "glyphicon-thumbs-down"
                   "glyphicon-thumbs-up")}]]))
   (when contact
     [:div
      (om/build util/g-hangout
                {:id "g-hangout"
                 :invites [{:id contact
                            :invite_type "email"}]})
      [:a.btn.btn-success
       {:href contact
        :target "_blank"}
       contact]])])

(defn render-activity
  [app-state [activity people] participation]
  [:div.panel.panel-default
   [:div.panel-body
    {:class (participation {:paired "bg-success"
                            :invited "bg-info"
                            :inviting "bg-info"
                            :shared "bg-warning"})}
    (join-or-leave app-state activity)
    [:h4 {:style {:margin-top 0}} activity]
    [:br]
    (let [relationships (get-in @app-state [:model :people])
          contacts (get-in @app-state [:model :contact])]
      (for [relationship [:my-pair :inviting :invited :available :hunted :taken :me]]
        (when-let [s (seq
                      (for [person people
                            :when (contains? (relationship relationships) person)]
                        (render-person participation
                                       app-state
                                       person
                                       relationship
                                       (get contacts person))))]
          [:ul.list-unstyled.list-inline
           s
           [:span.small
            [:span.glyphicon.glyphicon-chevron-left]
            " "
            [:span (name relationship)]]])))]])

(defn new-activity-form
  [app-state]
  [:div.panel.panel-default
   [:div.panel-body
    [:form
     {:role "form"
      :on-submit (bindom/form
                  (fn [{:keys [activity]}]
                    (swap! app-state update-in [:activities] set-conj activity)))}
     [:div.form-group
      [:input.form-control {:name "activity"
                            :placeholder "Enter activity"}]]
     [:button.btn.btn-primary.pull-right {:type "submit"}
      "Publish "
      [:span.glyphicon.glyphicon-ok]]]]])

(defn matching
  "Page for searching for a matching pair"
  [app-state]
  [:div.row
   [:div.col-md-6
    [:h1 "Available activities:"]
    (for [actkv (get-in @app-state [:model :activities :available])]
      (render-activity app-state actkv :available))]
   [:div.col-md-6
    [:h1 "My activities:"]
    (new-activity-form app-state)
    (for [participation [:paired :invited :shared :joined]
          actkv (get-in @app-state [:model :activities participation])]
      (render-activity app-state actkv participation))]])
