(ns pairwell.client.about)

(defn about
  [app-state]
  [:div
   [:div.col-md-6
    [:h2 "What?"]
    [:p "Meet up with people online who are interested in pair programming on a project right now."]
    [:h2 "Why?"]
    [:p "Pairing is fun and productive, coordinating schedules is hard."]
    [:h2 "How?"]
    [:p "Once you find someone to pair with, use Zoom or Google Hangouts to talk, and VNC or TMUX to type."]]
   [:div.col-md-6
    [:img.img-responsive.img-rounded
     {:src "img/muppetspairprogramming.jpg"
      :style {:width "100%"}
      :alt "muppets pair programming"}]]])
