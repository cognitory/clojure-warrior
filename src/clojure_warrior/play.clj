(ns clojure-warrior.play)

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

(defmulti take-warrior-action
  "Returns new state after performing warrior action"
  (fn [state action] (first action)))

(defmethod take-warrior-action :walk
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (case [(warrior :direction) direction]
                          [:east :forward] (update-in (warrior :position) [0] inc)
                          [:west :backward] (update-in (warrior :position) [0] inc)
                          [:west :forward] (update-in (warrior :position) [0] dec)
                          [:east :backward] (update-in (warrior :position) [0] dec))
        can-walk? (= :floor (:type (unit-at-position (state :board) target-position)))]

    (update state :board
              (fn [board]
                (if can-walk?
                  (-> board
                      (assoc-in (reverse (warrior :position)) {:type :floor})
                      (assoc-in (reverse target-position) (dissoc warrior :position)))
                  board)))))


; consider not storing units on the board
; but in a list, with each unit having a position
