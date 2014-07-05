(ns pairwell.client.matching
  (:require [pairwell.client.bindom :as bindom]))


(defn render-value [v]
  (if (string? v)
    v
    (for [x v]
      [:button.btn.btn-default x])))

(defn render-card [card action]
  [:div.btn.btn-default.btn-block
   action
   [:dl.dl-horizontal
    (interleave
     (for [k (keys card)]
       [:dt (name k)])
     (for [v (vals card)]
       [:dd {:style {:text-align "left"}} (render-value v)]))]])

(defn new-card-form [app-state]
  [:form {:role "form"
          :on-submit (bindom/form app-state [:my-cards])}
   [:button.close
    {:on-click (fn [e]
                 (swap! app-state dissoc :creating-new-card)
                 false)}
    [:span.glyphicon.glyphicon-remove]]
   [:div.form-group
    [:input.form-control {:name "topic"
                          :placeholder "Enter topic"}]]
   [:div.form-group
    [:input {:type "time"
             :name "until"}]]
   [:button.btn.btn-primary {:type "submit"}
    "Publish"
    [:span.glyphicon.glyphicon-ok]]])

(defn matching
  "Page for searching for a matching pair"
  [app-state]
  [:div
   [:span (@app-state :error)]
   [:div.col-md-6
    [:h3 "Available"]
    (for [card (get-in @app-state [:model :available])]
      (render-card card [:button.btn.btn-success.pull-right
                         {:on-click (fn [e]
                                      (swap! app-state
                                             update-in [:interest]
                                             (fnil conj #{})
                                             card))}
                         "Join"]))]
   [:div.col-md-6
    [:div.row
     [:div.col-md-8
      [:div.progress
       [:div.progress-bar.progress-bar-striped.active
        {:role "progressbar"
         :aria-valuenow "100"
         :aria-valuemin "0"
         :aria-valuemax "100"
         :style {:width "100%"}} "Matching"]]]
     [:div.col-md-4
      [:button.btn.btn-danger.btn-block
       {:type "button"
        :on-click (fn [e]
                    (swap! app-state assoc :page :welcome))}
       "Stop"]]]
    [:h3 "My cards"]
    (for [card (get-in @app-state [:model :my-cards])]
      (render-card card
                   [:button.close
                    {:on-click (fn [e]
                                 (swap! app-state
                                        update-in [:my-cards]
                                        disj card))}
                    [:span.glyphicon.glyphicon-remove]]))
    (if (:creating-new-card @app-state)
      (new-card-form app-state)
      [:button.btn.btn-primary
       {:on-click (fn [e]
                    (swap! app-state assoc :creating-new-card true))}
       [:span.glyphicon.glyphicon-plus]])
    [:h3 "My interests"]
    (for [interest (get-in @app-state [:model :interest])]
      (render-card interest
                   [:button.close
                    {:on-click (fn [e]
                                 (swap! app-state
                                        update-in [:interest]
                                        disj interest))}
                    [:span.glyphicon.glyphicon-remove]]))
    [:hr]
    [:span (str (@app-state :model))]]])
