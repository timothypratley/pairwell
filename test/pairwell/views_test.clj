(ns pairwell.views-test
  (:require [pairwell.views :refer :all]
            [clojure.test :refer :all]))


(def users
  {"john" {:page :about
           :activities ["Anything"]
           :confirmed "jane"}
   "jane" {:contact-me "jane@example.com"
           :page :matching
           :activities ["Project Euler"
                        "Improving Pair Well"]
           :confirmed "tim"}
   "tim" {:contact-me "tim@example.com"
          :page :matching
          :activities ["Improving Pair Well"]
          :confirmed "jane"}
   "Testbot" {:activities ["Test activity"]}
   "bad" {:yeargh nil}})
(def uids ["john" "jane" "tim" "bad"])

(deftest test-state->views
  (testing "View constructors"
    (let [activities (all-activities users)
          my-view (view users uids activities "tim")]
      (is (= {"Anything" #{"john"}
              "Project Euler" #{"jane"}
              "Improving Pair Well" #{"tim" "jane"}
              "Test activity" #{"Testbot"}}
             activities))
      (is (= {:available #{"john" "bad"}
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
             (dissoc john :people)))
      (is (= {:paired {"Improving Pair Well" #{"tim" "jane"}}
              :contact {"tim" "tim@example.com"}
              :available {"Anything" #{"john"}
                          "Test activity" #{"Testbot"}}
              :joined {"Project Euler" #{"jane"}}}
             (dissoc jane :people)))
      (is (= {:paired {"Improving Pair Well" #{"jane" "tim"}}
              :contact {"jane" "jane@example.com"}
              :available {"Anything" #{"john"}
                          "Project Euler" #{"jane"}
                          "Test activity" #{"Testbot"}}}
             (dissoc tim :people))))))
