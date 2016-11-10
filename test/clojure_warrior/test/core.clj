(ns clojure-warrior.test.core
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.core :as w]
    [clojure-warrior.play :as play]))

(deftest generate-initial-level-state
  (testing "generate-initial-level-state"
    (let [in {:board [[:__ :C> :a> nil nil :*> nil :<S nil :<w :<C]]}
          out {:board
               [[{:type :wall}
                 {:type :stairs}
                 {:type :captive
                  :direction :east
                  :health 1}
                 {:type :archer
                  :direction :east
                  :health 7}
                 {:type :floor}
                 {:type :floor}
                 {:type :warrior
                  :health 20
                  :direction :east}
                 {:type :floor}
                 {:type :thick-sludge
                  :direction :west
                  :health 24}
                 {:type :floor}
                 {:type :wizard
                  :direction :west
                  :health 3}
                 {:type :captive
                  :direction :west
                  :health 1}
                 {:type :wall}]]}]
      (is (= out
             (w/generate-initial-level-state in))))))


(deftest generate-display
  (testing "generate-display"
    (let [in {:board
              [[{:type :wall}
                {:type :stairs}
                {:type :captive
                 :direction :east
                 :health 1}
                {:type :archer
                 :direction :east
                 :health 7}
                {:type :floor}
                {:type :floor}
                {:type :warrior
                 :health 20
                 :direction :east}
                {:type :floor}
                {:type :thick-sludge
                 :direction :west
                 :health 24}
                {:type :floor}
                {:type :wizard
                 :direction :west
                 :health 3}
                {:type :captive
                 :direction :west
                 :health 1}
                {:type :wall}]]}
          out "-------------\n|>Ca  @ S wC|\n-------------"]
      (is (= out (w/generate-display in))))))
