(ns pairwell.client.matching
  (:require [pairwell.client.bindom :as bindom]
            [clojure.string :as string]
            [taoensso.encore :refer [logf]]))


(defn render-value [app-state v]
  (if (string? v)
    v
    (for [x v]
      [:button.btn.btn-default
       {:type "button"
        :on-click (fn [e]
                    (swap! app-state assoc :confirmed x))}
       (str x " ")
       [:span.glyphicon.glyphicon-thumbs-up]])))

(defn render-card [app-state card action]
  [:div.panel.panel-default
   [:div.panel-body
    {:class (cond (:contact card) "bg-success"
                  (seq (:interest card)) "bg-warning")}
    action
    [:dl.dl-horizontal
     (interleave
      (for [k (keys card)]
        [:dt (string/capitalize (name k))])
      (for [v (vals card)]
        [:dd {:style {:text-align "left"}}
         (render-value app-state v)]))]]])

(defn new-card-form [app-state]
  [:div.panel.panel-default
   [:div.panel-body.bg-info
    [:form
     {:role "form"
      :on-submit (bindom/form
                  (fn [m]
                    (let [activity (:activity m)
                          props (dissoc m :activity)]
                      (swap! app-state assoc-in [:cards activity] props))))}
     [:button.close
      {:type "button"
       :on-click (fn [e]
                   (swap! app-state dissoc :creating-new-card)
                   false)}
      [:span.glyphicon.glyphicon-remove]]
     [:div.form-group
      [:input.form-control {:name "activity"
                            :placeholder "Enter activity"}]]
     [:button.btn.btn-primary.pull-right {:type "submit"}
      "Publish "
      [:span.glyphicon.glyphicon-ok]]]]])

(defn join-or-leave [app-state card]
  (let [interest (:activity card)]
    (if (contains? (:interest @app-state) interest)
      [:button.btn.btn-warning.pull-right
       {:on-click (fn [e]
                    (swap! app-state update-in [:interest]
                           disj interest))}
       "Leave"]
      [:button.btn.btn-success.pull-right
       {:on-click (fn [e]
                    (swap! app-state update-in [:interest]
                           (fnil conj #{}) interest))}
       "Join"])))

(defn matching
  "Page for searching for a matching pair"
  [app-state]
  [:div.row
   [:div.col-md-6
    [:h1 "Available activities:"]
    (for [card (get-in @app-state [:model :available])]
      (render-card app-state card (join-or-leave app-state card)))]
   [:div.col-md-6
    [:h1 "My activities:" (when-not (:creating-new-card @app-state)
                            [:button.btn.btn-primary.pull-right
                             {:on-click (fn [e]
                                          (swap! app-state assoc :creating-new-card true))}
                             [:span.glyphicon.glyphicon-plus] " Publish new activity"])]
    (when (:creating-new-card @app-state)
      (new-card-form app-state))
    (for [card (get-in @app-state [:model :cards])]
      (render-card app-state card
                   [:button.close
                    {:on-click (fn [e]
                                 (swap! app-state update-in [:cards]
                                        dissoc (:activity card)))}
                    [:span.glyphicon.glyphicon-remove]]))
    (for [card (get-in @app-state [:model :joined])]
      (render-card app-state card (join-or-leave app-state card)))]])
