(ns clojure-warrior.helpers
  (:require
    [clojure-warrior.state :as s]))

(defn stairs
  "Returns the stairs"
  [state]
  (s/get-stairs (state :board)))

(defn warrior
  "Returns the warrior"
  [state]
  (s/get-warrior (state :board)))

(defn look
  "Returns the first non-empty space in given direction from the warrior"
  [state direction]
  (s/first-unit-in-range
    (state :board)
    (warrior state)
    direction
    10000))

(defn feel
  "Return the space 1 unit in given direction from the warrior"
  [state direction]
  (s/unit-at-position (state :board)
    (s/action-target-position
      (warrior state)
      direction)))

(defn listen
  "Returns a list of all enemies and captives"
  [state]
  (->> (s/get-units (state :board))
       (remove (fn [u]
                 (contains? #{:warrior :floor :wall :stairs} (:type u))))))

(defn- abs [x]
  (if (< x 0)
    (* -1 x)
    x))

(defn distance-to
  "Returns the number of steps from the warrior to a position"
  [state target-position]
  (let [warrior-position (:position (warrior state))]
    (+ (abs (- (first target-position) (first warrior-position)))
       (abs (- (last target-position) (last warrior-position))))))

(defn inspect
  "Returns the space at the given position"
  [state target-position]
  (s/unit-at-position (state :board) target-position))
