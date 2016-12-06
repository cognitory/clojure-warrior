(ns clojure-warrior.units
  (:require
    [clojure-warrior.helpers :refer [look-generic feel-generic]]))

(defn melee-unit-logic [state self]
  (cond
    (= :warrior (:type (feel-generic state self :forward)))
    [:attack :forward]
    (= :warrior (:type (feel-generic state self :backward)))
    [:attack :backward]
    :else
    [:rest]))

(defn ranged-unit-logic [state self]
  (cond
    (= :warrior (:type (look-generic state self :forward 2)))
    [:shoot :forward]
    (= :warrior (:type (look-generic state self :backward 2)))
    [:shoot :backward]
    :else
    [:rest]))

(def reference
  {:captive
   {:type :captive
    :captive? true
    :max-health 1.0
    :define-char \C
    :display-char \C}

   :archer
   {:type :archer
    :enemy? true
    :ranged? true
    :define-char \a
    :display-char \a
    :shoot-power 3.0
    :max-health 7.0
    :logic ranged-unit-logic}

   :sludge
   {:type :sludge
    :enemy? true
    :melee? true
    :define-char \s
    :display-char \s
    :attack-power 3.0
    :max-health 12.0
    :logic melee-unit-logic}

   :thick-sludge
   {:type :thick-sludge
    :enemy? true
    :melee? true
    :define-char \S
    :display-char \S
    :attack-power 3.0
    :max-health 24.0
    :logic melee-unit-logic}

   :wizard
   {:type :wizard
    :enemy? true
    :ranged? true
    :define-char \w
    :display-char \w
    :shoot-power 11.0
    :max-health 3.0
    :logic ranged-unit-logic}

   :warrior
   {:type :warrior
    :ranged? true
    :melee? true
    :define-char \*
    :display-char \@
    :max-health 20.0
    :attack-power 5.0
    :shoot-power 3.0}

   :wall
   {:type :wall
    :environment? true
    :define-char \-
    :display-char \|}

   :stairs
   {:type :stairs
    :stairs? true
    :environment? true
    :define-char \_
    :display-char \>}

   :floor
   {:type :floor
    :empty? true
    :environment? true
    :define-char nil
    :display-char " "}})

(def define-char->type
  (reduce
    (fn [memo u]
      (assoc memo (u :define-char) (u :type)))
    {}
    (vals reference)))
