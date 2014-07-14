(ns pairwell.client.matching
  (:require [clojure.string :as string]
            [pairwell.client.bindom :as bindom]
            [taoensso.encore :refer [logf]]))


(defn render-value [app-state v]
  (if (string? v)
    v
    (for [x v]
      [:button.btn.btn-default
       {:type "button"
        :on-click (fn [e]
                    (swap! app-state assoc :confirmed x))} x])))

(defn render-card [app-state card action]
  [:div.btn.btn-default.btn-block
   action
   [:dl.dl-horizontal
    (interleave
     (for [k (keys card)]
       [:dt (string/capitalize (name k))])
     (for [v (vals card)]
       [:dd {:style {:text-align "left"}}
        (render-value app-state v)]))]])

(defn new-card-form [app-state]
  [:form.btn.btn-default.btn-block
   {:role "form"
    :on-submit (bindom/form
                (fn [m]
                  (let [topic (:topic m)
                        props (dissoc m :topic)]
                    (swap! app-state assoc-in [:cards topic] props))))}
   [:button.close
    {:type "button"
     :on-click (fn [e]
                 (swap! app-state dissoc :creating-new-card)
                 false)}
    [:span.glyphicon.glyphicon-remove]]
   [:div.form-group
    [:input.form-control {:name "topic"
                          :placeholder "Enter topic"}]]
   [:div.form-group
    [:input {:name "until"
             :type "time"}]]
   [:button.btn.btn-primary {:type "submit"}
    "Publish"
    [:span.glyphicon.glyphicon-ok]]])

(defn join-or-leave [app-state card]
  (let [interest (select-keys card [:with :topic])]
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
  [:div
   [:span (@app-state :error)]
   [:div.col-md-6
    [:h3 "My cards"]
    (for [card (get-in @app-state [:model :cards])]
      (render-card app-state card
                   [:button.close
                    {:on-click (fn [e]
                                 (swap! app-state update-in [:cards]
                                        dissoc (:topic card)))}
                    [:span.glyphicon.glyphicon-remove]]))
    (if (:creating-new-card @app-state)
      (new-card-form app-state)
      [:button.btn.btn-primary
       {:on-click (fn [e]
                    (swap! app-state assoc :creating-new-card true))}
       [:span.glyphicon.glyphicon-plus]])]
   [:div.col-md-6
    [:h3 "Available"]
    (for [card (get-in @app-state [:model :available])]
      (render-card app-state card (join-or-leave app-state card)))]])
