(ns pairwell.client.main
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.welcome :refer [welcome]]
            [pairwell.client.matching :refer [matching]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))


(def app-state (atom {:page :welcome
                      :model {}}))

(def page-fns {:welcome welcome
               :matching matching})

(add-watch comm/chsk-state :a-state-change
           (fn [k r old-val new-val]
             (.log js/console "changed" (:open? new-val))
             (swap! app-state assoc :open? (:open? new-val))))

(add-watch comm/model :a-model-change
           (fn [k r old-val new-val]
             (.log js/console "changed model" new-val)
             (swap! app-state assoc :model new-val)))

(add-watch app-state :a-state-change
           (fn [k r old-val new-val]
             (comm/send-app-state (dissoc new-val :model))))


(defn widget [{:keys [page] :as data} owner]
  (reify
    om/IRender
    (render [this]
      (html [:div {:className "container"}
             [:h1 "Pair Well"]
             ((page-fns page) app-state)]))))

(om/root widget
         app-state
         {:target (.getElementById js/document "app")})
