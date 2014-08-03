(ns pairwell.client.about)


(defn about
  [app-state]
  [:div.jumbotron
   [:div.row
    [:div.col-md-6
     [:p "Pair Well matches people who are interested in collaborating right now."]
     [:dl.dl-horizontal
      [:dt "What?"]
      [:dd
       "You register your interests and are matched with suitable collaborators. "
       "When you confirm someone to pair with, your contact details are mutually shared. "
       "You can then start a direct video feed and screenshare to work, study, play games, "
       "or whatever you both were interested in pairing on."]
      [:dt "Why?"]
      [:dd
       "Pairing is fun and productive. Coordinating schedules is hard. "
       "Login and pair with people who are ready right now with a shared interest."]
      [:dt "How?"]
      [:dd
       "Once you find someone to pair with, use Zoom or Google Hangouts to talk. "
       "Use ScreenHero, VNC or TMUX to allow each other to type."]]
     [:p
      "Pair Well does not track or store your contact information, "
      "but will reveal it to people you confirm."]
     [:p
      "I wrote Pair Well to connect with friends to work on personal projects. "]
     [:p "Please email me feedback "
      [:a {:href "mailto:timothypratley@gmail.com"} "(timothypratley@gmail.com)"]
      " or request features and fixes on "
      [:a {:href "https://github.com/timothypratley/pairwell/issues"} "github."]]]
    [:div.col-md-6
     [:img.img-responsive.img-rounded
      {:src "img/muppetspairprogramming.jpg"
       :style {:width "100%"}
       :alt "muppets pair programming"}]]]])
