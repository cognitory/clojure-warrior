(ns clojure-warrior.units
  (:require
    [clojure-warrior.helpers :refer [look feel]]))

(defn melee-unit-logic [state]
  (cond
    (= :warrior (:type (feel state :forward)))
    [:attack :forward]
    (= :warrior (:type (feel state :backward)))
    [:attack :backward]))

(defn ranged-unit-logic [state]
  (cond
    (= :warrior (:type (look state :forward)))
    [:shoot :forward]
    (= :warrior (:type (look state :backward)))
    [:shoot :backward]))

(def reference
  {:captive
   {:type :captive
    :max-health 1.0
    :define-char \C
    :display-char \C}

   :archer
   {:type :archer
    :enemy? true
    :define-char \a
    :display-char \a
    :shoot-power 3.0
    :max-health 7.0
    :logic ranged-unit-logic}

   :sludge
   {:type :sludge
    :enemy? true
    :define-char \s
    :display-char \s
    :attack-power 3.0
    :max-health 12.0
    :logic melee-unit-logic}

   :thick-sludge
   {:type :thick-sludge
    :enemy? true
    :define-char \S
    :display-char \S
    :attack-power 3.0
    :max-health 24.0
    :logic melee-unit-logic}

   :wizard
   {:type :wizard
    :enemy? true
    :define-char \w
    :display-char \w
    :shoot-power 11.0
    :max-health 3.0
    :logic ranged-unit-logic}

   :warrior
   {:type :warrior
    :define-char \*
    :display-char \@
    :max-health 20.0
    :attack-power 5.0
    :shoot-power 3.0}

   :wall
   {:type :wall
    :define-char \-
    :display-char \|}

   :stairs
   {:type :stairs
    :define-char \_
    :display-char \>}

   :floor
   {:type :floor
    :define-char nil
    :display-char " "}})

(def define-char->type
  (reduce
    (fn [memo u]
      (assoc memo (u :define-char) (u :type)))
    {}
    (vals reference)))
