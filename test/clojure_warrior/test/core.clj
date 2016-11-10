(ns clojure-warrior.test.core
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.core :as w]))

(deftest generate-initial-level-state
  (testing "..."

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
                 nil
                 nil
                 {:type :warrior
                  :health 20
                  :direction :east}
                 nil
                 {:type :thick-sludge
                  :direction :west
                  :health 24}
                 nil
                 {:type :wizard
                  :direction :west
                  :health 3}
                 {:type :captive
                  :direction :west
                  :health 1}
                 {:type :wall}]]}]
      (is (= out
             (w/generate-initial-level-state in))))))
