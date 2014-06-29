(ns pairwell.client.matching
  (:require [pairwell.client.bindom :as bindom]))


(defn render-card [card]
  [:button.btn.btn-default.btn-block
   (into [:dl.dl-horizontal]
         (apply concat (for [[k v] card]
                         [[:dt (name k)]
                          [:dd {:style {:text-align "left"}} v]])))])

(defn new-card-form [app-state]
  [:div
   [:textarea {:on-change (bindom/setter app-state [:matching :new-card :text])}]
   [:ul
    (for [tag (get-in @app-state [:matching :tags])]
      [:li.label.label-defaul tag])]
   [:button.btn.btn-primary
    {:on-click (fn [e])}
    "Publish" [:span.glyphicon.glyphicon-ok]]
   [:button.btn
    {:on-click (fn [e]
                 (swap! app-state bindom/dissoc-in [:matching :new-card]))}
    [:span.glyphicon.glyphicon-remove]]])

(defn matching
  "Page for searching for a matching pair"
  [app-state]
  [:div
   [:span (@app-state :error)]
   [:div.col-md-6
    [:h3 "Available"]
    (for [card (get-in @app-state [:model :available])]
      (render-card card))]
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
    
    [:hr]
    [:h3 "My cards"]
    (for [card (get-in @app-state [:model :my-cards])]
      (render-card card))
    (if (get-in @app-state [:matching :new-card])
      (new-card-form app-state)
      [:button.btn {:on-click (fn [e]
                                (swap! app-state assoc-in
                                       [:matching :new-card] {}))}
       "+"])
    [:hr]
    [:h3 "Invitations received"]
    (for [invite (get-in @app-state [:model :invitations :received])]
      (render-card invite))
    [:hr]
    [:h3 "Invitations sent"]
    (for [invite (get-in @app-state [:model :invitations :sent])]
      (render-card invite))
    [:hr]
    [:span (str (@app-state :model))]]])
