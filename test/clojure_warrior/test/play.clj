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
                             :health 5}]]}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :health 7}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))

    (testing "does not get more than max-health"
      (let [state {:board [[{:type :warrior
                             :health 19}]]}
            action [:rest]
            expected-state {:board [[{:type :warrior
                                      :health 20}]]}]
        (is (= expected-state (play/take-warrior-action state action)))))))