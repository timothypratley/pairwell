(ns pairwell.client.about)

(defn about
  [app-state]
  [:div
   [:div.col-md-6
    [:p "Pair Well matches people who are interested in collaborating right now."]
    [:dl.dl-horizontal
     [:dt "What?"]
     [:dd "Meet up with people online who are interested in pair programming on a project right now."]
     [:dt "Why?"]
     [:dd "Pairing is fun and productive, coordinating schedules is hard."]
     [:dt "How?"]
     [:dd "Once you find someone to pair with, use Zoom or Google Hangouts to talk, and VNC or TMUX to type."]]
    [:p
        "I wrote Pair Well to connect with existing and new friends to work on personal projects. "
        "Please email me ("
        [:a {:href "mailto:timothypratley@gmail.com"}
         "timothypratley@gmail.com"]
        ") any feedback you have."]
    [:p "Music by " [:a {:href "http://milkytracker.org/?download"
                         :target "_blank"}
                     "raina."]]]
   [:div.col-md-6
    [:img.img-responsive.img-rounded
     {:src "img/muppetspairprogramming.jpg"
      :style {:width "100%"}
      :alt "muppets pair programming"}]]])
