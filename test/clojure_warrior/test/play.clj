(ns clojure-warrior.test.play
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.play :as play]))

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
        (is (= expected-state (play/take-warrior-action state action)))))

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
        (is (= expected-state (play/take-warrior-action state action)))))

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
        (is (= expected-state (play/take-warrior-action state action)))))

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
        (is (= expected-state (play/take-warrior-action state action))))))

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
                                         "You hit a whatever, dealing 3.0 damage"]}]
          (is (= expected-state (play/take-warrior-action state action)))))

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
                                         "You hit a whatever, dealing 3.0 damage"]}]
          (is (= expected-state (play/take-warrior-action state action)))))

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
          (is (= expected-state (play/take-warrior-action state action)))))

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
                                         "You hit a whatever, dealing 3.0 damage"]}]
          (is (= expected-state (play/take-warrior-action state action)))))

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
          (is (= expected-state (play/take-warrior-action state action)))))))

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

(deftest play-turn
  (testing "play-turn"
    (let [init-state {:board [[{:type :warrior
                                :health 10.0
                                :direction :east}
                               {:type :floor}]]
                      :messages []
                      :tick 0}
          users-code (fn [state]
                       [:walk :forward])]

      (is (= {:board [[{:type :floor}
                       {:type :warrior
                        :health 10.0
                        :direction :east}]]
              :messages ["You walk forward"]
              :tick 1}
             (play/play-turn init-state users-code))))))

(deftest start-level
  (testing "start-level"
    (let [level {:id 1
                 :board [[:*> nil nil :__]]}
          user-code (fn [state]
                      [:walk :forward])]
      (is (= [["You enter room 1"]
              ["You enter room 1"
               "You walk forward"]
              ["You enter room 1"
               "You walk forward"
               "You walk forward"]
              ["You enter room 1"
               "You walk forward"
               "You walk forward"
               "You walk forward"
               "You walk up the stairs"]]
             (map :messages (play/start-level level user-code))))))

  (testing "player death"
    (let [level {:id 1
                 :board [[:*> :<w]]}
          user-code (fn [state]
                      [:walk :forward])]
      (is (= ["You enter room 1"
              "You walk forward"
              "You bump into a wizard"
              "A wizard shoots you"
              "You lose 11.0 health, down to 9.0"
              "You walk forward"
              "You bump into a wizard"
              "A wizard shoots you"
              "You lose 9.0 health, down to 0.0"
              "You are dead. Game over."]
             (:messages (last (play/start-level level user-code))))))))

(deftest play-levels
  (testing "play-levels"
    (testing "win game"
      (let [levels [{:id 1
                     :board [[:*> :__]]}
                    {:id 2
                     :board [[:*> :__]]}]
            user-code (fn [state]
                        [:walk :forward])]
        (is (= ["You enter the tower"
                "You enter room 1"
                "You walk forward"
                "You walk up the stairs"
                "You enter room 2"
                "You walk forward"
                "You walk up the stairs"
                "You have reached the top of the tower"]
               (last (map :messages (play/play-levels levels user-code)))))))

    (testing "lose game, due to death"
      (let [levels [{:id 1
                     :board [[:*> :-- :<a :__]]}
                    {:id 2
                     :board [[:*> :-- :<w :__]]}]
            user-code (fn [state]
                        [:attack :forward])]
        (is (= true
               (:game-over? (last (play/play-levels levels user-code)))))))))

(deftest get-public-unit
  (let [private-unit {:type :archer
                      :enemy? true
                      :ranged? true
                      :define-char \a
                      :display-char \a
                      :abilities #{:shoot :look}
                      :shoot-power 3.0
                      :max-health 7.0
                      :health 10.0
                      :direction :east}]
    (= #{:type :enemy? :ranged? :health :direction}
       (set (keys (play/get-public-unit private-unit))))))

(deftest get-public-state
  (testing "get-public-state"
    (let [state {:board [[{:type :warrior
                           :foo :bar
                           :health 10.0}]]}
          public-state [[{:type :warrior
                          :health 10.0}]]]
      (= public-state
         (play/get-public-state state)))))
