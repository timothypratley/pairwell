(ns pairwell.views-test
  (:require [pairwell.views :refer :all]
            [clojure.test :refer :all]))


(def uids ["john" "jane" "tim" "bad"])
(def users
  {"john" {:page :about
           :activities #{"Anything"}
           :confirmed "jane"}
   "jane" {:contact "jane@example.com"
           :page :matching
           :activities #{"Project Euler"
                         "Improving Pair Well"}
           :confirmed "tim"}
   "tim" {:contact "tim@example.com"
          :page :matching
          :activities #{"Improving Pair Well"}
          :confirmed "jane"}
   "Testbot" {:activities #{"Test activity"}}
   "bad" {:yeargh nil}})

(deftest test-states->views
  (testing "View constructors"
    (let [activity->people (participation users)
          my-view (view users activity->people "tim")]
      (is (= {"Anything" #{"john"}
              "Project Euler" #{"jane"}
              "Improving Pair Well" #{"tim" "jane"}
              "Test activity" #{"Testbot"}}
             activity->people))
      (is (= {:available #{"john" "bad" "Testbot"}
              :my-pair #{"jane"}
              :me #{"tim"}}
             (:people my-view)))))

  (testing "All views"
    (let [views (states->views users uids)
          {:strs [john jane tim]} views]
      (is (= (set (keys views)) #{"john" "jane" "tim" "bad"}))
      (is (= {:available {"Project Euler" #{"jane"}
                          "Test activity" #{"Testbot"}
                          "Improving Pair Well" #{"tim" "jane"}}
              :joined {"Anything" #{"john"}}}
             (:activities john)))
      (is (not (:contact john)))
      (is (= {:paired {"Improving Pair Well" #{"tim" "jane"}}
              :available {"Anything" #{"john"}
                          "Test activity" #{"Testbot"}}
              :joined {"Project Euler" #{"jane"}}}
             (:activities jane)))
      (is (= (:contact jane) {"tim" "tim@example.com"}))
      (is (= {:paired {"Improving Pair Well" #{"jane" "tim"}}
              :available {"Anything" #{"john"}
                          "Project Euler" #{"jane"}
                          "Test activity" #{"Testbot"}}}
             (:activities tim)))
      (is (= (:contact tim) {"jane" "jane@example.com"})))))
