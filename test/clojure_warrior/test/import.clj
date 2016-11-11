(ns clojure-warrior.test.import
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.import :as import]))

(deftest extract-unit
  (testing "extract unit type correctly"
    (is (= :wall (:type (import/extract-unit :--))))
    (is (= :captive (:type (import/extract-unit :C>))))
    (is (= :archer (:type (import/extract-unit :a>))))
    (is (= :warrior (:type (import/extract-unit :*>)))))

  (testing "extract unit direction correctly"
    (is (= :east (:direction (import/extract-unit :C>))))
    (is (= :west (:direction (import/extract-unit :<C))))))

(deftest extract-board
  (testing "extract-board"
    (let [in [[:__ :C> :a> nil nil :*> nil :<S nil :<w :<C]]
          exp [[{:type :wall}
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
                {:type :wall}]]
          out (import/extract-board in)]
      (is (= (count (first exp))
             (count (first out))))
      (is (= (map :type (first exp))
             (map :type (first out))))
      (is (= (map :direction (first exp))
             (map :direction (first out))))
      (is (= (map :health (first exp))
             (map :health (first out)))))))
