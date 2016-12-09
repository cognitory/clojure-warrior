(ns clojure-warrior.helpers
  (:require
    [clojure-warrior.state :as s]))

(defn say [& args]
  ; redefined in .play to log to state messages
  )

(defn stairs
  "Returns the stairs"
  [board]
  (s/get-stairs board))

(defn warrior
  "Returns the warrior"
  [board]
  (s/get-warrior board))

(defn look-generic
  [board unit direction limit]
  (s/first-unit-in-range
    board
    unit
    direction
    limit))

(defn look
  "Returns the first non-empty space in given direction from the warrior"
  [board direction]
  (look-generic board (warrior board) direction 1000))

(defn feel-generic
  [board unit direction]
  (s/unit-at-position
    board
    (s/action-target-position
      unit
      direction)))

(defn feel
  "Return the space 1 unit in given direction from the warrior"
  [board direction]
  (feel-generic board (warrior board) direction))

(defn listen
  "Returns a list of all enemies and captives"
  [board]
  (->> (s/get-units board)
       (remove (fn [u]
                 (contains? #{:warrior :floor :wall :stairs} (:type u))))))

(defn- abs [x]
  (if (< x 0)
    (* -1 x)
    x))

(defn distance-to
  "Returns the number of steps from the warrior to a position"
  [board target-position]
  (let [warrior-position (:position (warrior board))]
    (+ (abs (- (first target-position) (first warrior-position)))
       (abs (- (last target-position) (last warrior-position))))))

(defn inspect
  "Returns the space at the given position"
  [board target-position]
  (s/unit-at-position board target-position))
