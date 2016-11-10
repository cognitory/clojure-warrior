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

(deftest get-warrior
  (testing "get-warrior"
    (is (= {:type :warrior
            :position [2 0]}
           (play/get-warrior [[{} {} {:type :warrior}]])))))

(deftest unit-at-position
  (testing "unit-at-position"
    (is (= {:type :warrior
            :position [0 0]}
           (play/unit-at-position [[{:type :warrior}]] [0 0])))))

(deftest take-warrior-action
  (testing "walk"
    (testing "can walk forward when open space"
      (let [state {:board [[{:type :warrior
                             :health 20
                             :direction :east}
                            {:type :floor}]]}
            action [:walk :forward]
            expected-state {:board [[{:type :floor}
                                     {:type :warrior
                                      :health 20
                                      :direction :east}]]}]

        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can walk backward when open space"
      (let [state {:board [[{:type :floor}
                            {:type :warrior
                             :health 20
                             :direction :east}]]}
            action [:walk :backward]
            expected-state {:board [[{:type :warrior
                                      :health 20
                                      :direction :east}
                                     {:type :floor}]]}]

        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can walk forward when open space (and facing west)"
      (let [state {:board [[{:type :floor}
                            {:type :warrior
                             :health 20
                             :direction :west}]]}
            action [:walk :forward]
            expected-state {:board [[{:type :warrior
                                      :health 20
                                      :direction :west}
                                     {:type :floor}]]}]

        (is (= expected-state (play/take-warrior-action state action)))))


    (testing "cannot walk forward when not open space"
      (let [state {:board [[{:type :warrior
                             :health 20
                             :direction :east}
                            {:type :wall}]]}
            action [:walk :forward]
            expected-state {:board [[{:type :warrior
                                      :health 20
                                      :direction :east}
                                     {:type :wall}]]}]

        (is (= expected-state (play/take-warrior-action state action))))

      ; TODO have a message appended
      )))
