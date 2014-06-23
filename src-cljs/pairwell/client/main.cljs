(ns pairwell.client.main
  (:require [pairwell.client.communication :as comm]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))


(def app-state
  (atom {:heading "Pair Well"
         :model {}}))

(add-watch comm/chsk-state :a-state-change
           (fn [k r old-val new-val]
             (.log js/console "changed" (:open? new-val))
             (swap! app-state assoc :open? (:open? new-val))))

(add-watch comm/model :a-model-change
           (fn [k r old-val new-val]
             (.log js/console "changed model" new-val)
             (swap! app-state assoc :model new-val)))

(defn matching
  []
  [:div
   [:span (@app-state :error)]
   [:h2 "You made it!"]
   [:button.btn "+"]
   [:textarea {:on-changeo (fn [e]
                             (let [v (.-value (.-target e))]
                               (comm/send)))}]
   [:hr]
   (for [card (get-in @app-state [:model :my-cards])]
     (for [[k v] card]
       [:dl.btn [:dt (name k)] [:dd v]]))
   [:hr]
   (for [invite (get-in @app-state [:model :invitations :received])]
     (for [[k v] card]
       [:dl.btn [:dt (name k)] [:dd v]]))
   [:hr]
   (for [invite (get-in @app-state [:model :invitations :sent])]
     (for [[k v] card]
       [:dl.btn [:dt (name k)] [:dd v]]))
   [:hr]
   [:span (str (@app-state :model))]])

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
   [:span (str (@app-state :open?))]
   [:br]
   [:button {:type "button"
             :class-name "btn btn-primary btn-lg btn-block"
             :on-click (fn [e]
                         (comm/send)
                         (if (@app-state :username)
                           (do (comm/login (:username @app-state))
                               (swap! app-state assoc :page matching))
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
