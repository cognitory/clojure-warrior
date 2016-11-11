(ns clojure-warrior.test.import
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.import :as import]))

(deftest generate-initial-level-state
  (testing "generate-initial-level-state"
    (let [in {:board [[:__ :C> :a> nil nil :*> nil :<S nil :<w :<C]]}
          out {:board
               [[{:type :wall}
                 {:type :stairs}
                 {:type :captive
                  :direction :east
                  :health 1.0}
                 {:type :archer
                  :direction :east
                  :health 7.0}
                 {:type :floor}
                 {:type :floor}
                 {:type :warrior
                  :health 20.0
                  :direction :east}
                 {:type :floor}
                 {:type :thick-sludge
                  :direction :west
                  :health 24.0}
                 {:type :floor}
                 {:type :wizard
                  :direction :west
                  :health 3.0}
                 {:type :captive
                  :direction :west
                  :health 1.0}
                 {:type :wall}]]}]
      (is (= out
             (import/generate-initial-level-state in))))))
