(ns pairwell.client.main
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))


(def app-state
  (atom {:heading "Pair Well"}))

(defn matching
  []
  [:div
   [:span (@app-state :error)]
   [:h2 "You made it!"]])

(defn welcome
  []
  [:form {:roleName "form"}
   [:input {:placeholder "Enter your name"
            :on-change (fn [e]
                         (let [v (.-value (.-target e))]
                           (if (seq v)
                             (swap! app-state assoc :username v)
                             (swap! app-state dissoc :username))))}]
   [:br]
   [:span (@app-state :error)]
   [:br]
   [:button {:type "button"
             :className "btn btn-primary btn-lg btn-block"
             :on-click (fn [e]
                         (if (@app-state :username)
                           (swap! app-state assoc :page matching)
                           (swap! app-state assoc :error "Please enter your name first.")))}
    "Start"]
   [:br]
   [:input]
   [:br]
   [:select 
    [:option "foo"]
    [:option "bar"]]])

(swap! app-state assoc :page welcome)

(defn widget [{:keys [page heading] :as data} owner]
  (reify
    om/IRender
    (render [this]
      (html [:div {:className "container"}
             [:h1 heading]
             (page)]))))

(om/root widget
         app-state
         {:target (.getElementById js/document "app")})
