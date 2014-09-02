(ns pairwell.client.util
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :as html :refer-macros [html]]))

(defn g-hangout [{:keys [id] :as data} owner]
  (reify
    om/IRender
    (render [this]
      (html
       [:div {:id id}
        id]))

    om/IDidMount
    (did-mount [_]
      (.render js/gapi.hangout
               id
               (clj->js
                (merge {"render" "createhangout"} data))))))
