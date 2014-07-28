(ns pairwell.views-test
  (:require [pairwell.views :refer :all]
            [clojure.test :refer :all]))


(deftest test-state->views
  (testing "All views are correctly calculated"
    (let [states {"john" {:page :about
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
                  "bad" {:yeargh nil}}
          uids ["john" "jane" "tim" "bad"]
          views (states->views states uids)
          {:strs [john jane tim]} views]
      (is (= {"Anything" #{"john"}
              "Project Euler" #{"jane"}
              "Improving Pair Well" #{"tim" "jane"}
              "Test activity" #{"Testbot"}}
             (all-activities states)))
      ;TODO
      #_(is (= {:me people}
             (view :people)))
      (is (= (set (keys views)) #{"john" "jane" "tim"}))
      (is (= {:available [{:activity "Proejct Euler"
                           :people #{"jane"}}
                          {:activity "Test activity"
                           :people #{"Testbot"}}]
              :joined [{:activity "Anything"}]}
             john))
      (is (= {:matched {:activity "Improving Pair Well"
                        :username "tim"
                        :contact-me "tim@example.com"}
              :available [{:activity "Anything"
                           :people #{"john"}}
                          {:activity "Test activity"
                           :people #{"Testbot"}}]
              :joined [{:activity "Project Euler"}
                       {:activity "Improving Pair Well"
                        :people #{"tim"}}]}
             jane))
      (is (= {:matched {:activity "Improving Pair Well"
                        :username "jane"
                        :contact-me "jane@example.com"}
              :available [{:activity "Anything"
                           :people #{"john"}}
                          {:activity "Project Euler"
                           :people #{"jane"}}
                          {:activity "Test activity"
                           :people #{"Testbot"}}]
              :joined [{:activity "Improving Pair Well"
                        :matched [#{"jane"}]}]}
             tim)))))
