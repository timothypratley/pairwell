(ns pairwell.client.main
  (:require [pairwell.client.communication :as comm]
            [pairwell.client.about :refer [about]]
            [pairwell.client.matching :refer [matching]]
            [pairwell.client.welcome :refer [welcome]]
            [pairwell.client.audio :as audio]
            [clojure.string :as string]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))


(def app-state (atom {:page :matching
                      :model {}}))

(def page-fns {:welcome welcome
               :matching matching
               :about about})

(defn maybe-login [{:keys [username interest cards page]}]
  (when (and (not= :welcome page)
             (empty? username)
             (or interest cards))
    (swap! app-state assoc :page :welcome)))

;; TODO: should open explicitly instead of by require
(swap! app-state assoc :open? (:open? @comm/chsk-state))
(add-watch comm/chsk-state :a-chsk-state-change
           (fn a-chsk-state-change-watch [k r old-val new-val]
             (swap! app-state assoc :open? (:open? new-val))))

(add-watch comm/model :a-model-change
           (fn a-model-change-watch [k r old-val new-val]
             (swap! app-state assoc :model new-val)))

(add-watch app-state :a-state-change
           (fn a-state-change-watch [k r old-val new-val]
             (maybe-login new-val)
             (audio/transitions old-val new-val)
             (let [old-val (dissoc old-val :model)
                   new-val (dissoc new-val :model)]
               (when (not= old-val new-val)
                 (comm/send-app-state new-val)))))

(defn link [to]
  [:li [:a {:href "#"
            :on-click (fn [e]
                        (swap! app-state assoc
                               :page to))} (string/capitalize (name to))]])

(defn widget [{:keys [page] :as data} owner]
  (reify
    om/IRender
    (render [this]
      (html [:div {:className "container"}
             [:nav.navbar.navbar-default {:role "navigation"}
              [:div.container-fluid
               [:div.navbar-header
                [:button.navbar-toggle {:type "button"
                                        :data-toggle "collapse"
                                        :data-target "navbar-collapse"}
                 [:span.sr-only "Toggle navigation"]
                 [:span.icon-bar]
                 [:span.icon-bar]
                 [:span.icon-bar]]
                [:a.navbar-brand {:href "#"}
                 "Pair Well"]]
               [:div.collapse.navbar-collapse {:id "navbar-collapse"}
                [:ul.nav.navbar-nav
                 (link :welcome)
                 (link :matching)
                 (link :about)]
                [:ul.nav.navbar-nav.pull-right
                 (when-let [username (:username @app-state)]
                   [:button.btn.btn-default
                    [:span.glyphicon.glyphicon-user]
                    " " username])
                 (if (:open? @app-state)
                   [:span.glyphicon.glyphicon-link
                    {:title "Connected to server."}]
                   [:span.glyphicon.glyphicon-exclamation-sign
                    {:title "Disconnected from server."}])]]]]
             ((page-fns page) app-state)]))))

(om/root widget
         app-state
         {:target (.getElementById js/document "app")})
