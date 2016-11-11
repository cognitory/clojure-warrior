(ns clojure-warrior.play
  (:require
    [clojure-warrior.import :as import]))

(defn add-message [state message]
  (update state :messages conj message))

(defn assoc-at [state position value]
  (assoc-in state [:board (last position) (first position)] value))

(defn update-at [state position k fn]
  (update-in state [:board (last position) (first position) k] fn))

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

(defmulti take-warrior-action
  "Returns new state after performing warrior action"
  (fn [state action] (first action)))

; Move in the given direction
(defmethod take-warrior-action :walk
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (action-target-position warrior direction)
        target (unit-at-position (state :board) target-position)]
    (as-> state $
        (add-message $ (str "You walk " (name direction)))
        (cond
          (= :floor (:type target))
          (-> $
              (assoc-at (warrior :position) {:type :floor})
              (assoc-at (target :position) (dissoc warrior :position)))
          (= :stairs (:type target))
          (-> $
              (add-message "You walk up the stairs")
              (assoc-at (warrior :position) {:type :floor})
              (assoc-at (target :position) (-> warrior
                                               (assoc :at-stairs true)
                                               (dissoc :position))))
          :else
          (add-message $ (str "You bump into a " (name (target :type))))))))

(defmethod take-warrior-action :pivot
  [state _]
  (let [warrior (get-warrior (state :board))
        new-direction (case (warrior :direction)
                        :east :west
                        :west :east)]
    (-> state
        (add-message "You pivot")
        (add-message (str "You are now facing " (name new-direction)))
        (update-at (warrior :position) :direction (fn [_] new-direction)))))

(defmethod take-warrior-action :rest
  [state _]
  (let [warrior (get-warrior (state :board))
        max-health (:max-health warrior)
        health (:health warrior)
        new-health (min max-health (+ health (* max-health 0.1)))
        health-delta (- new-health health)]
    (as-> state $
        (add-message $ "You rest")
        (if (> health-delta 0)
          (add-message $ (str "You receive " health-delta " health from resting, up to " new-health " health"))
          (add-message $ (str "You are already fit as a fiddle")))
        (update-at $ (warrior :position) :health (fn [health] new-health)))))

(defmethod take-warrior-action :attack
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (action-target-position warrior direction)
        target (unit-at-position (state :board) target-position)
        power-multiplier (case direction
                           :forward 1.0
                           :backward 0.5)
        attack-power (* (warrior :attack-power) power-multiplier)]
    (as-> state $
      (add-message $ (str "You attack " (name direction)))
      (if target
        (-> $
            (add-message (str "You hit a " (name (:type target)) ", dealing " attack-power " damage"))
            (update-at target-position :health
                       (fn [health]
                         (max 0 (- health attack-power)))))
        (add-message $ "You hit nothing")))))

(defmethod take-warrior-action :shoot
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target (first-unit-in-range (state :board) warrior direction 3)
        attack-power (warrior :shoot-power)]
    (as-> state $
      (add-message $ (str "You shoot " (name direction)))
      (if (and target (target :health))
        (-> $
            (add-message (str "You hit a " (name (:type target)) ", dealing " attack-power " damage"))
            (update-at (target :position) :health
                       (fn [health]
                         (max 0 (- health attack-power)))))
        (-> $
            (add-message "You hit nothing"))))))

(defmethod take-warrior-action :rescue
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target (first-unit-in-range (state :board) warrior direction 1)]
    (as-> state $
      (add-message $ (str "You rescue " (name direction)))
      (if (and target (= :captive (:type target)))
        (-> $
            (add-message "You unbind and rescue a captive")
            (assoc-at (target :position) {:type :floor})
            (update-at (warrior :position) :points (fn [points]
                                                     (+ points 20)))
            (add-message "You earn 20 points"))
        (add-message $ "There is no captive to rescue")))))


(defn get-public-state [state]
  ; TODO
  )

(defn take-env-actions [state]
  ; TODO
  state)

(defn take-npc-actions [state]
  ; TODO
  state)

(defn play-turn [init-state users-code]
  (let [warrior-action (users-code (get-public-state init-state))
        ; TODO validate warrior-action
        post-user-state (take-warrior-action init-state warrior-action)
        post-env-state (take-env-actions post-user-state)
        post-npc-state (take-npc-actions post-env-state)
        final-state (take-env-actions post-npc-state)]
    final-state))

(defn warrior-at-stairs? [state]
  (->> (state :board)
       flatten
       (map :at-stairs)
       (some true?)))

(defn play-level [history users-code]
  (if (or
        (> (count history) 20)
        (warrior-at-stairs? (last history)))
    history
    (play-level (conj history (play-turn (last history) users-code)) users-code)))

(defn start-level [level-definition users-code]
  (let [init-state [(import/generate-initial-level-state level-definition)]]
    (play-level init-state users-code)))
