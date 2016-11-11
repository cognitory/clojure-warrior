(ns clojure-warrior.test.state
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.state :as state]))

(deftest get-warrior
  (testing "get-warrior"
    (is (= {:type :warrior
            :position [2 0]}
           (state/get-warrior [[{} {} {:type :warrior}]])))))

(deftest get-stairs
  (testing "get-stairs"
    (is (= {:type :stairs
            :position [2 0]}
           (state/get-stairs [[{} {} {:type :stairs}]])))))

(deftest unit-at-position
  (testing "unit-at-position"
    (is (= {:type :warrior
            :position [0 0]}
           (state/unit-at-position [[{:type :warrior}]] [0 0])))))

