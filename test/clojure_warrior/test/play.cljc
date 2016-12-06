(ns clojure-warrior.test.play
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.play :as play]))

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

      (is (= [{:board [[{:type :floor}
                         {:type :warrior
                          :health 10.0
                          :direction :east}]]
                :messages ["You walk forward"]
                :tick 1}]
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
               "You walk forward and up the stairs"]]
             (map :messages (play/start-level level user-code))))))

  (testing "player death"
    (let [level {:id 1
                 :board [[:*> :<w]]}
          user-code (fn [state]
                      [:walk :forward])]
      (is (= ["You enter room 1"
              "You walk forward and bump into a wizard"
              "A wizard shoots you and you lose 11.0 health, down to 9.0"
              "You walk forward and bump into a wizard"
              "A wizard shoots you and you lose 9.0 health, down to 0.0"
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
                "You walk forward and up the stairs"
                "You enter room 2"
                "You walk forward and up the stairs"
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
               (:game-over? (last (play/play-levels levels user-code)))))

        (is (= "You are dead. Game over."
              (last (:messages (last (play/play-levels levels user-code))))))))))

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
