(ns clojure-warrior.state)

; getters

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

(defn get-stairs [board]
  (->> board
       get-units
       (filter (fn [u] (= :stairs (u :type))))
       first))

(defn unit-at-position [board position]
  (->> board
       get-units
       (filter (fn [u] (= position (u :position))))
       first))

(defn first-unit-in-range [board unit action-direction action-range]
  (let [net-direction (case [(unit :direction) action-direction]
                        [:east :forward] :east
                        [:east :backward] :west
                        [:west :forward] :west
                        [:west :backward] :east)
        maybe-reverse (case net-direction
                        :east identity
                        :west reverse)]
    (->> board
         get-units
         maybe-reverse
         (drop-while (fn [u]
                       (not= (:type u) :warrior)))
         (drop 1)
         (take action-range)
         (remove (fn [u]
                   (= (:type u) :floor)))
         first)))

(defn action-target-position [warrior action-direction]
  (case [(warrior :direction) action-direction]
    [:east :forward] (update-in (warrior :position) [0] inc)
    [:west :backward] (update-in (warrior :position) [0] inc)
    [:west :forward] (update-in (warrior :position) [0] dec)
    [:east :backward] (update-in (warrior :position) [0] dec)))

; modifiers

(defn add-message [state message]
  (update state :messages conj message))

(defn set-at [state position value]
  (assoc-in state [:board (last position) (first position)] value))

(defn assoc-at [state position k value]
  (assoc-in state [:board (last position) (first position) k] value))

(defn update-at [state position k fn]
  (update-in state [:board (last position) (first position) k] fn))
