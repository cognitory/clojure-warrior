(ns clojure-warrior.test.core
  (:require
    [clojure.test :refer [deftest testing is]]
    [clojure-warrior.display :as display]))

(deftest generate-display
  (testing "generate-display"
    (let [in {:board
              [[{:type :wall
                 :display-char \|}
                {:type :captive
                 :display-char \C}
                {:type :archer
                 :display-char \a}
                {:type :floor
                 :display-char " "}
                {:type :wall
                 :display-char \|}]]}
          out "-----\n|Ca |\n-----"]
      (is (= out (display/generate-display in))))))
