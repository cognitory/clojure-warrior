(ns clojure-warrior.test.helpers
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.helpers :as helpers]))

(def sample-state
  {:messages []
   :board [[{:type :wall}
            {:type :warrior
             :direction :east}
            {:type :floor}
            {:type :slug}
            {:type :archer}
            {:type :stairs}
            {:type :wall}]]})

(deftest stairs
  (testing "stairs"
    (is (= {:type :stairs
            :position [5 0]}
           (helpers/stairs sample-state)))))

(deftest warrior
  (testing "warrior"
    (is (= {:type :warrior
            :direction :east
            :position [1 0]}
           (helpers/warrior sample-state)))))

(deftest look
  (testing "look"
    (is (= {:type :slug
            :position [3 0]}
           (helpers/look sample-state :forward)))))

(deftest feel
  (testing "feel"
    (is (= {:type :floor
            :position [2 0]}
           (helpers/feel sample-state :forward)))))

(deftest listen
  (testing "listen"
    (is (= [{:type :slug
             :position [3 0]}
            {:type :archer
             :position [4 0]}]
           (helpers/listen sample-state)))))

(deftest distance-to
  (testing "distance-to"
    (is (= 1 (helpers/distance-to sample-state [0 0])))
    (is (= 6 (helpers/distance-to sample-state [4 3])))))

(deftest inspect
  (testing "inspect"
    (is (= {:type :archer
            :position [4 0]}
           (helpers/inspect sample-state [4 0])))))
