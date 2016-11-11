(ns clojure-warrior.play
  (:require
    [clojure-warrior.units :as units]))

(defn get-units
  "Returns list of units, with their positions"
  [board]
  (->> board
       (map-indexed
         (fn [y row]
           (map-indexed (fn [x unit]
                          (assoc unit :position [x y])) row)))
       flatten))

(defn get-warrior [board]
  (->> board
       get-units
       (filter (fn [u] (= :warrior (u :type))))
       first))

(defn unit-at-position [board position]
  (->> board
       get-units
       (filter (fn [u] (= position (u :position))))
       first))

(defn action-target-position [warrior action-direction]
  (case [(warrior :direction) action-direction]
    [:east :forward] (update-in (warrior :position) [0] inc)
    [:west :backward] (update-in (warrior :position) [0] inc)
    [:west :forward] (update-in (warrior :position) [0] dec)
    [:east :backward] (update-in (warrior :position) [0] dec)))

(defmulti take-warrior-action
  "Returns new state after performing warrior action"
  (fn [state action] (first action)))

(defmethod take-warrior-action :walk
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (action-target-position warrior direction)
        can-walk? (= :floor (:type (unit-at-position (state :board) target-position)))]

    (update state :board
              (fn [board]
                (if can-walk?
                  (-> board
                      (assoc-in (reverse (warrior :position)) {:type :floor})
                      (assoc-in (reverse target-position) (dissoc warrior :position)))
                  board)))))

(defmethod take-warrior-action :pivot
  [state _]
  (let [warrior (get-warrior (state :board))
        p (reverse (warrior :position))]
    (update-in state [:board (first p) (last p) :direction]
               (fn [d]
                 (case d
                   :east :west
                   :west :east)))))

(defmethod take-warrior-action :rest
  [state _]
  (let [warrior (get-warrior (state :board))
        p (reverse (warrior :position))
        max-health (:max-health (units/reference :warrior))]
    (update-in state [:board (first p) (last p) :health]
               (fn [health]
                 (min max-health (+ health (* max-health 0.1)))))))

(defmethod take-warrior-action :attack
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (action-target-position warrior direction)
        p (reverse target-position)
        can-attack? (unit-at-position (state :board) target-position)
        power-multiplier (case direction
                           :forward 1.0
                           :backward 0.5)]
    (if can-attack?
      (update-in state [:board (first p) (last p) :health]
                 (fn [health]
                   (- health (* (warrior :attack-power) power-multiplier))))
      state)))
