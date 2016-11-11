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
                            {:type :floor}]]
                   :messages []}
            action [:walk :forward]
            expected-state {:board [[{:type :floor}
                                     {:type :warrior
                                      :direction :east}]]
                            :messages ["You walk forward"]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can walk backward when open space"
      (let [state {:board [[{:type :floor}
                            {:type :warrior
                             :direction :east}]]
                   :messages []}
            action [:walk :backward]
            expected-state {:board [[{:type :warrior
                                      :direction :east}
                                     {:type :floor}]]
                            :messages ["You walk backward"]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can walk forward when open space (and facing west)"
      (let [state {:board [[{:type :floor}
                            {:type :warrior
                             :direction :west}]]
                   :messages []}
            action [:walk :forward]
            expected-state {:board [[{:type :warrior
                                      :direction :west}
                                     {:type :floor}]]
                            :messages ["You walk forward"]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "cannot walk forward when not open space"
      (let [state {:board [[{:type :warrior
                             :direction :east}
                            {:type :wall}]]
                   :messages []}
            action [:walk :forward]
            expected-state {:board [[{:type :warrior
                                      :direction :east}
                                     {:type :wall}]]
                            :messages ["You walk forward"
                                       "You bump into a wall"]}]
        (is (= expected-state (play/take-warrior-action state action))))))

  (testing "pivot"
    (testing "turns warrior west->east"
      (let [state {:board [[{:type :warrior
                             :direction :east}]]
                   :messages []}
            action [:pivot]
            expected-state {:board [[{:type :warrior
                                      :direction :west}]]
                            :messages ["You pivot"
                                       "You are now facing west"]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "turns warrior east->west"
      (let [state {:board [[{:type :warrior
                             :direction :west}]]
                   :messages []}
            action [:pivot]
            expected-state {:board [[{:type :warrior
                                      :direction :east}]]
                            :messages ["You pivot"
                                       "You are now facing east"]}]
        (is (= expected-state (play/take-warrior-action state action))))))

  (testing "rest"
    (testing "get back 10% of max health"
      (let [state {:board [[{:type :warrior
                             :max-health 20.0
                             :health 5.0}]]
                   :messages []}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :max-health 20.0
                                      :health 7.0}]]
                            :messages ["You rest"
                                       "You receive 2.0 health from resting, up to 7.0 health"]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "does not get more than max-health"
      (let [state {:board [[{:type :warrior
                             :max-health 20.0
                             :health 19.0}]]
                   :messages []}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :max-health 20.0
                                      :health 20.0}]]
                            :messages ["You rest"
                                       "You receive 1.0 health from resting, up to 20.0 health"]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "does not heal at max-health"
      (let [state {:board [[{:type :warrior
                             :max-health 20.0
                             :health 20.0}]]
                   :messages []}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :max-health 20.0
                                      :health 20.0}]]
                            :messages ["You rest"
                                       "You are already fit as a fiddle"]}]
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
        (is (= expected-state (play/take-warrior-action state action))))))

  (testing "shoot (damages first unit within 3 units ahead)"
    (testing "can shoot forward (range 1)"
      (let [state {:board [[{:type :warrior
                             :shoot-power 3.0
                             :direction :east}
                            {:type :whatever
                             :health 10.0}]]}
            action [:shoot :forward]
            expected-state {:board [[{:type :warrior
                                      :shoot-power 3.0
                                      :direction :east}
                                     {:type :whatever
                                      :health 7.0}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can shoot forward (range 3)"
      (let [state {:board [[{:type :warrior
                             :shoot-power 3.0
                             :direction :east}
                            {:type :floor}
                            {:type :floor}
                            {:type :whatever
                             :health 10.0}]]}
            action [:shoot :forward]
            expected-state {:board [[{:type :warrior
                                      :shoot-power 3.0
                                      :direction :east}
                                     {:type :floor}
                                     {:type :floor}
                                     {:type :whatever
                                      :health 7.0}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "when nothing is in range, no effect"
      (let [state {:board [[{:type :warrior
                             :shoot-power 3.0
                             :direction :east}
                            {:type :floor}
                            {:type :floor}
                            {:type :floor}]]}
            action [:shoot :forward]
            expected-state {:board [[{:type :warrior
                                      :shoot-power 3.0
                                      :direction :east}
                                     {:type :floor}
                                     {:type :floor}
                                     {:type :floor}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "can shoot backward"
      (let [state {:board [[{:type :warrior
                             :shoot-power 3.0
                             :direction :west}
                            {:type :whatever
                             :health 10.0}]]}
            action [:shoot :backward]
            expected-state {:board [[{:type :warrior
                                      :shoot-power 3.0
                                      :direction :west}
                                     {:type :whatever
                                      :health 7.0}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "shooting object without health has no effect"
      (let [state {:board [[{:type :warrior
                             :shoot-power 5.0
                             :direction :west}
                            {:type :whatever}]]}
            action [:shoot :forward]
            expected-state {:board [[{:type :warrior
                                      :shoot-power 5.0
                                      :direction :west}
                                     {:type :whatever}]]}]
        (is (= expected-state (play/take-warrior-action state action))))))

  (testing "rescue"
    (testing "receives 20 points if captive; captive is removed"
      (testing "can rescue forward"
        (let [state {:board [[{:type :warrior
                               :points 0.0
                               :direction :east}
                              {:type :captive}]]
                     :messages []}
              action [:rescue :forward]
              expected-state {:board [[{:type :warrior
                                        :points 20.0
                                        :direction :east}
                                       {:type :floor}]]
                              :messages ["You rescue forward"
                                         "You unbind and rescue a captive"
                                         "You earn 20 points"]}]
          (is (= expected-state (play/take-warrior-action state action)))))

      (testing "can rescue backward"
        (let [state {:board [[{:type :warrior
                               :points 0.0
                               :direction :west}
                              {:type :captive}]]
                     :messages []}
              action [:rescue :backward]
              expected-state {:board [[{:type :warrior
                                        :points 20.0
                                        :direction :west}
                                       {:type :floor}]]
                              :messages ["You rescue backward"
                                         "You unbind and rescue a captive"
                                         "You earn 20 points"]}]
          (is (= expected-state (play/take-warrior-action state action))))))

    (testing "if not a captive, no effect"
      (let [state {:board [[{:type :warrior
                             :points 0.0
                             :direction :east}
                            {:type :whatever}]]
                   :messages []}
            action [:rescue :forward]
            expected-state {:board [[{:type :warrior
                                      :points 0.0
                                      :direction :east}
                                     {:type :whatever}]]
                            :messages ["You rescue forward"
                                       "There is no captive to rescue"]}]
        (is (= expected-state (play/take-warrior-action state action)))))))
