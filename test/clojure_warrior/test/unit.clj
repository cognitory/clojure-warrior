(ns clojure-warrior.test.unit
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.unit :as unit]))

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
        (is (= expected-state (unit/take-warrior-action state action)))))

    (testing "can walk onto stairs"
      (let [state {:board [[{:type :warrior
                             :direction :east}
                            {:type :stairs}]]
                   :messages []}
            action [:walk :forward]
            expected-state {:board [[{:type :floor}
                                     {:type :warrior
                                      :at-stairs true
                                      :direction :east}]]
                            :messages ["You walk forward"
                                       "You walk up the stairs"]}]
        (is (= expected-state (unit/take-warrior-action state action)))))

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
        (is (= expected-state (unit/take-warrior-action state action)))))

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
        (is (= expected-state (unit/take-warrior-action state action)))))

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
        (is (= expected-state (unit/take-warrior-action state action))))))

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
        (is (= expected-state (unit/take-warrior-action state action)))))

    (testing "turns warrior east->west"
      (let [state {:board [[{:type :warrior
                             :direction :west}]]
                   :messages []}
            action [:pivot]
            expected-state {:board [[{:type :warrior
                                      :direction :east}]]
                            :messages ["You pivot"
                                       "You are now facing east"]}]
        (is (= expected-state (unit/take-warrior-action state action))))))

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
        (is (= expected-state (unit/take-warrior-action state action)))))

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
        (is (= expected-state (unit/take-warrior-action state action)))))

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
        (is (= expected-state (unit/take-warrior-action state action))))))

  (testing "attack"
    (testing "can attack forward"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :east}
                            {:type :whatever
                             :health 10.0}]]
                   :messages []}
            action [:attack :forward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :east}
                                     {:type :whatever
                                      :health 5.0}]]
                            :messages ["You attack forward"
                                       "A whatever takes 5.0 damage, and has 5.0 health left"]}]
        (is (= expected-state (unit/take-warrior-action state action)))))

    (testing "can kill a unit"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :east}
                            {:type :whatever
                             :health 5.0}]]
                   :messages []}
            action [:attack :forward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :east}
                                     {:type :whatever
                                      :health 0.0}]]
                            :messages ["You attack forward"
                                       "A whatever takes 5.0 damage, and dies"]}]
        (is (= expected-state (unit/take-warrior-action state action)))))

    (testing "can attack backward (at 50% reduced strength)"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :west}
                            {:type :whatever
                             :health 10.0}]]
                   :messages []}
            action [:attack :backward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :west}
                                     {:type :whatever
                                      :health 7.5}]]
                            :messages ["You attack backward"
                                       "A whatever takes 2.5 damage, and has 7.5 health left"]}]
        (is (= expected-state (unit/take-warrior-action state action)))))

    (testing "attacking object without health has no effect"
      (let [state {:board [[{:type :warrior
                             :attack-power 5.0
                             :direction :west}
                            {:type :whatever}]]
                   :messages []}
            action [:attack :forward]
            expected-state {:board [[{:type :warrior
                                      :attack-power 5.0
                                      :direction :west}
                                     {:type :whatever}]]
                            :messages ["You attack forward"
                                       "You hit nothing"]}]
        (is (= expected-state (unit/take-warrior-action state action))))))

  (testing "shoot"
    (testing "damages first unit within 3 units ahead"
      (testing "can shoot forward (range 1)"
        (let [state {:board [[{:type :warrior
                               :shoot-power 3.0
                               :direction :east}
                              {:type :whatever
                               :health 10.0}]]
                     :messages []}
              action [:shoot :forward]
              expected-state {:board [[{:type :warrior
                                        :shoot-power 3.0
                                        :direction :east}
                                       {:type :whatever
                                        :health 7.0}]]
                              :messages ["You shoot forward"
                                         "A whatever takes 3.0 damage, and has 7.0 health left"]}]
          (is (= expected-state (unit/take-warrior-action state action)))))

      (testing "can shoot forward (range 3)"
        (let [state {:board [[{:type :warrior
                               :shoot-power 3.0
                               :direction :east}
                              {:type :floor}
                              {:type :floor}
                              {:type :whatever
                               :health 10.0}]]
                     :messages []}
              action [:shoot :forward]
              expected-state {:board [[{:type :warrior
                                        :shoot-power 3.0
                                        :direction :east}
                                       {:type :floor}
                                       {:type :floor}
                                       {:type :whatever
                                        :health 7.0}]]
                              :messages ["You shoot forward"
                                         "A whatever takes 3.0 damage, and has 7.0 health left"]}]
          (is (= expected-state (unit/take-warrior-action state action)))))

      (testing "when nothing is in range, no effect"
        (let [state {:board [[{:type :warrior
                               :shoot-power 3.0
                               :direction :east}
                              {:type :floor}
                              {:type :floor}
                              {:type :floor}]]
                     :messages []}
              action [:shoot :forward]
              expected-state {:board [[{:type :warrior
                                        :shoot-power 3.0
                                        :direction :east}
                                       {:type :floor}
                                       {:type :floor}
                                       {:type :floor}]]
                              :messages ["You shoot forward"
                                         "You hit nothing"]}]
          (is (= expected-state (unit/take-warrior-action state action)))))

      (testing "can shoot backward"
        (let [state {:board [[{:type :warrior
                               :shoot-power 3.0
                               :direction :west}
                              {:type :whatever
                               :health 10.0}]]
                     :messages []}
              action [:shoot :backward]
              expected-state {:board [[{:type :warrior
                                        :shoot-power 3.0
                                        :direction :west}
                                       {:type :whatever
                                        :health 7.0}]]
                              :messages ["You shoot backward"
                                         "A whatever takes 3.0 damage, and has 7.0 health left"]}]
          (is (= expected-state (unit/take-warrior-action state action)))))

      (testing "shooting object without health has no effect"
        (let [state {:board [[{:type :warrior
                               :shoot-power 5.0
                               :direction :east}
                              {:type :whatever}]]
                     :messages []}
              action [:shoot :forward]
              expected-state {:board [[{:type :warrior
                                        :shoot-power 5.0
                                        :direction :east}
                                       {:type :whatever}]]
                              :messages ["You shoot forward"
                                         "You hit nothing"]}]
          (is (= expected-state (unit/take-warrior-action state action)))))))

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
          (is (= expected-state (unit/take-warrior-action state action)))))

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
          (is (= expected-state (unit/take-warrior-action state action))))))

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
        (is (= expected-state (unit/take-warrior-action state action)))))))
