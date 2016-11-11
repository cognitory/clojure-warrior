(ns clojure-warrior.test.play
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.play :as play]))

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
                             :direction :east}
                            {:type :floor}]]}
            action [:walk :forward]
            expected-state {:board [[{:type :floor}
                                     {:type :warrior
                                      :direction :east}]]}]

        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can walk backward when open space"
      (let [state {:board [[{:type :floor}
                            {:type :warrior
                             :direction :east}]]}
            action [:walk :backward]
            expected-state {:board [[{:type :warrior
                                      :direction :east}
                                     {:type :floor}]]}]

        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can walk forward when open space (and facing west)"
      (let [state {:board [[{:type :floor}
                            {:type :warrior
                             :direction :west}]]}
            action [:walk :forward]
            expected-state {:board [[{:type :warrior
                                      :direction :west}
                                     {:type :floor}]]}]

        (is (= expected-state (play/take-warrior-action state action)))))


    (testing "cannot walk forward when not open space"
      (let [state {:board [[{:type :warrior
                             :direction :east}
                            {:type :wall}]]}
            action [:walk :forward]
            expected-state {:board [[{:type :warrior
                                      :direction :east}
                                     {:type :wall}]]}]

        (is (= expected-state (play/take-warrior-action state action))))

      ; TODO have a message appended
      ))

  (testing "pivot"
    (testing "turns warrior west->east"
      (let [state {:board [[{:type :warrior
                             :direction :east}]]}
            action [:pivot]
            expected-state {:board [[{:type :warrior
                                      :direction :west}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "turns warrior east->west"
      (let [state {:board [[{:type :warrior
                             :direction :west}]]}
            action [:pivot]
            expected-state {:board [[{:type :warrior
                                      :direction :east}]]}]
        (is (= expected-state (play/take-warrior-action state action))))))

  (testing "rest"
    (testing "get back 10% of max health"
      (let [state {:board [[{:type :warrior
                             :health 5.0}]]}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :health 7.0}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "does not get more than max-health"
      (let [state {:board [[{:type :warrior
                             :health 19.0}]]}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :health 20.0}]]}]
        (is (= expected-state (play/take-warrior-action state action))))))

  (testing "attack"
    (testing "can attack forward"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :east}
                            {:type :whatever
                             :health 10.0}]]}
            action [:attack :forward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :east}
                                     {:type :whatever
                                      :health 5.0}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can attack backward (at 50% reduced strength)"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :west}
                            {:type :whatever
                             :health 10.0}]]}
            action [:attack :backward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :west}
                                     {:type :whatever
                                      :health 7.5}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "attacking object without health has no effect"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :west}
                            {:type :whatever}]]}
            action [:attack :forward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :west}
                                     {:type :whatever}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))))
