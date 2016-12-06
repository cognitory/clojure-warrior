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

(defn look-generic
  [state unit direction limit]
  (s/first-unit-in-range
    (state :board)
    unit
    direction
    limit))

(defn look
  "Returns the first non-empty space in given direction from the warrior"
  [state direction]
  (look-generic state (warrior state) direction 1000))

(defn feel-generic
  [state unit direction]
  (s/unit-at-position
    (state :board)
    (s/action-target-position
      unit
      direction)))

(defn feel
  "Return the space 1 unit in given direction from the warrior"
  [state direction]
  (feel-generic state (warrior state) direction))

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
