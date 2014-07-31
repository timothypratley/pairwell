(ns pairwell.client.matching
  (:require [pairwell.client.bindom :as bindom]
            [clojure.string :as string]
            [taoensso.encore :refer [logf]]))


(def set-conj (fnil conj #{}))

(defn join-or-leave [app-state activity]
  (if (contains? (:activities @app-state) activity)
    [:button.btn.btn-warning.pull-right
     {:on-click #(swap! app-state update-in [:activities]
                        disj activity)}
     "Leave"]
    [:button.btn.btn-success.pull-right
     {:on-click #(swap! app-state update-in [:activities]
                        set-conj activity)}
     "Join"]))

(defn render-value [app-state v]
  (if (string? v)
    v
    (for [x v]
      [:button.btn.btn-default
       {:type "button"
        :on-click #(swap! app-state assoc :confirmed x)}
       (str x " ")
       [:span.glyphicon.glyphicon-thumbs-up]])))

(defn render-activity [app-state [activity people] participation]
  [:div.panel.panel-default
   [:div.panel-body
    {:class (case participation
              :paired "bg-success"
              :invited "bg-info"
              :shared "bg-warning")}
    (join-or-leave app-state activity)
    [:dl.dl-horizontal
     (interleave
      (for [k (keys activity)]
        [:dt (string/capitalize (name k))])
      (for [v (vals activity)]
        [:dd {:style {:text-align "left"}}
         (render-value app-state v)]))]]])

(defn new-activity-form [app-state]
  [:div.panel.panel-default
   [:div.panel-body
    [:form
     {:role "form"
      :on-submit (bindom/form
                  (fn [{:keys [activity]}]
                    (swap! app-state update-in [:activities] set-conj activity)))}
     [:button.close
      {:type "button"
       :on-click #(swap! app-state dissoc :creating-new-activity)}
      [:span.glyphicon.glyphicon-remove]]
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
    (for [actkv (get-in @app-state [:model :available])]
      (render-activity app-state actkv :available))]
   [:div.col-md-6
    [:h1 "My activities:" (when-not (:creating-new-activity @app-state)
                            [:button.btn.btn-primary.pull-right
                             {:on-click #(swap! app-state assoc :creating-new-activity true)}
                             [:span.glyphicon.glyphicon-plus] " Publish new activity"])]
    (when (:creating-new-activity @app-state)
      (new-activity-form app-state))
    (for [participation [:paired :invited :shared :joined]
          actkv (get-in @app-state [:model participation])]
      (render-activity app-state actkv participation))]])
