(ns clojure-warrior.test.core
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.core :as core]))

(deftest generate-display
  (testing "generate-display"
    (let [in {:board
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
                {:type :wall}]]}
          out "-------------\n|>Ca  @ S wC|\n-------------"]
      (is (= out (core/generate-display in))))))
