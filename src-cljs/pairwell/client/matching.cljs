(ns pairwell.client.matching
  (:require [pairwell.client.bindom :as bindom]))


(defn render-card [card]
  [:button.btn.btn-default.btn-block
   [:dl.dl-horizontal
    ;TODO: forcat?
    (apply concat (for [[k v] card]
                    [[:dt (name k)]
                     [:dd {:style {:text-align "left"}}
                      (if (string? v)
                        v
                        (for [x v]
                          [:span x]))]]))]])

(defn new-card-form [app-state]
  [:form {:role "form"
          :on-submit (bindom/form app-state [:new-card])}
   [:input {:name "topic"}]
   [:input {:type "time"
            :name "until"}]
   #_[:select
    [:option "foo"]
    [:option "bar"]]
   [:button.btn.btn-primary
    {:type "submit"}
    "Publish" [:span.glyphicon.glyphicon-ok]]
   [:button.btn.btn-danger
    {:on-click (fn [e]
                 (swap! app-state dissoc :creating-new-card))}
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
    [:h3 "My cards"]
    (for [card (get-in @app-state [:model :my-cards])]
      (render-card card))
    (if (:creating-new-card @app-state)
      (new-card-form app-state)
      [:button.btn.btn-primary
       {:on-click (fn [e]
                    (swap! app-state assoc :creating-new-card true))}
       [:span.glyphicon.glyphicon-plus]])
    [:h3 "My interests"]
    (for [invite (get-in @app-state [:model :interest])]
      (render-card invite))
    [:hr]
    [:span (str (@app-state :model))]]])
