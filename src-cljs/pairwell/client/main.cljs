(ns pairwell.client.main
  (:require [pairwell.client.bindom :as bindom]
            [pairwell.client.communication :as comm]
            [pairwell.client.about :refer [about]]
            [pairwell.client.matching :refer [matching]]
            [pairwell.client.welcome :refer [welcome]]
            [pairwell.client.audio :as audio]
            [clojure.string :as string]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]))


(def app-state
  (atom {:page :welcome
         :model {}}))

(def page-fns
  {:welcome welcome
   :matching matching
   :about about})

;; TODO: should open explicitly instead of by require
(swap! app-state assoc :open? (:open? @comm/chsk-state))

(defn chsk-state-change-watch
  "Monitors the sente channel socket status."
  [k r a b]
  (swap! app-state assoc :open? (:open? b)))

(add-watch comm/chsk-state :csc chsk-state-change-watch)

(defn model-change-watch
  "Monitors the server sent model to rerender."
  [k r a b]
  (swap! app-state assoc :model b))

(add-watch comm/model :mc model-change-watch)

(defn maybe-login
  [{:keys [username interest cards page]}]
  (when (and (not= :welcome page)
             (empty? username)
             (or interest cards))
    (swap! app-state assoc :page :welcome)))

(defn state-change-watch
  "Monitors app state change to triger communication and audio."
  [k r a b]
  (maybe-login b)
  (audio/transitions a b)
  (let [a (dissoc a :model)
        b (dissoc b :model)]
    (when (not= a b)
      (comm/send-app-state b))))

(add-watch app-state :sc state-change-watch)

(defn link
  [to]
  [:li [:a {:href "#"
            :on-click (fn a-link-click [e]
                        (swap! app-state assoc :page to))}
        (string/capitalize (name to))]])

(defn widget
  "Renders the application from app-state."
  [{:keys [page username] :as data} owner]
  (reify
    om/IRender
    (render [this]
      (html
       [:div {:className "container"}
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
            [:img {:src "img/pairwell.png"}]]]
          [:div.collapse.navbar-collapse {:id "navbar-collapse"}
           [:ul.nav.navbar-nav.navbar-right
            (when-not (:open? @app-state)
              [:li
               [:p.navbar-text
                [:span.glyphicon.glyphicon-exclamation-sign]
                " Disconnected from server."]])
            (when (not= page :welcome)
              (link :welcome))
            (when (and username (not= page :matching))
              (link :matching))
            (when (not= page :about)
              (link :about))
            (when username
              [:li.dropdown
               [:a.dropdown-toggle {:href "#"
                                    :data-toggle "dropdown"}
                [:span.glyphicon.glyphicon-user]
                (str " " username)
                [:span.caret]]
               [:ul.dropdown-menu {:role "menu"}
                [:li
                 [:form.form-inline
                  {:role "form"
                   :on-submit (bindom/form
                               (fn [{:keys [contact]}]
                                 (swap! app-state assoc :contact contact)))}
                  [:div.form-group
                   [:div.input-group
                    [:span.input-group-addon [:span.glyphicon.glyphicon-facetime-video]]
                    [:input.form-control {:name "contact"
                                          :placeholder "Contact me by"}]]]
                  [:button.btn.btn-default.pull-right {:type "submit"}
                   [:span.glyphicon.glyphicon-ok]]]]
                [:li [:a {:href "#"
                          :on-click #(swap! app-state assoc
                                            :page :welcome
                                            :username nil)}
                      "logout"]]]])]]]]
        ((page-fns page) app-state)]))))

(om/root widget
         app-state
         {:target (.getElementById js/document "app")})
