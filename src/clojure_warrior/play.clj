(ns clojure-warrior.play
  (:require
    [clojure-warrior.import :as import]
    [clojure-warrior.helpers :as helpers]
    [clojure-warrior.state :refer [get-units
                                   get-warrior
                                   unit-at-position
                                   first-unit-in-range
                                   action-target-position]]))

(defn add-message [state message]
  (update state :messages conj message))

(defn assoc-at [state position value]
  (assoc-in state [:board (last position) (first position)] value))

(defn update-at [state position k fn]
  (update-in state [:board (last position) (first position) k] fn))

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
      (if (and target (target :health))
        (let [damage (min attack-power (target :health))
              target-new-health (max 0 (- (target :health) damage))]
          (-> $
              (add-message (str "A " (name (:type target)) " takes " damage " damage, "
                                (if (< 0 target-new-health)
                                  (str "and has " target-new-health " health left")
                                  (str "and dies"))))
              (update-at target-position :health
                         (fn [health]
                           target-new-health))))
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

(defn map-units [f board]
  (mapv (fn [row]
          (mapv f row)) board))

(defn get-public-unit [unit]
  (select-keys unit [:type :enemy? :health :direction :melee? :ranged?]))

(defn get-public-state [state]
  (->> state
       :board
       (map-units get-public-unit)))

(defn remove-dead-units [state]
  (update state :board
          (fn [board]
            (map-units (fn [unit]
                         (if (and
                               (contains? unit :health)
                               (>= 0 (unit :health)))
                           {:type :floor}
                           unit))
                       board))))

(defn warrior-at-stairs? [state]
  (->> (state :board)
       flatten
       (map :at-stairs)
       (some true?)))

(defn increment-tick [state]
  (update state :tick inc))

(defn check-warrior-dead [state]
  (if (= 0.0 (:health (get-warrior (state :board))))
    (-> state
        (assoc :game-over? true)
        (add-message "You are dead. Game over."))
    state))

(defn check-warrior-stalled [state]
  (if (< 10 (state :tick))
    (-> state
        (assoc :game-over? true)
        (add-message "You have taken too long. Game over."))
    state))

(defmulti take-enemy-action
  (fn [state enemy action]
    (first action)))

(defmethod take-enemy-action :shoot
  [state enemy _]
  (let [warrior (get-warrior (state :board))
        strength (enemy :shoot-power)
        new-health (max 0.0 (- (:health warrior) strength))
        health-delta (- (:health warrior) new-health)]
    (-> state
        (add-message (str "A " (name (enemy :type)) " shoots you"))
        (add-message (str "You lose " health-delta " health, down to " new-health))
        (update-at (:position warrior) :health (fn [_] new-health)))))

(defmethod take-enemy-action :attack
  [state enemy _]
  (let [warrior (get-warrior (state :board))
        strength (enemy :attack-power)
        new-health (max 0.0 (- (:health warrior) strength))
        health-delta (- (:health warrior) new-health)]
    (-> state
        (add-message (str "A " (name (enemy :type)) " attacks you"))
        (add-message (str "You lose " health-delta " health, down to " new-health))
        (update-at (:position warrior) :health (fn [_] new-health)))))

(defn take-npc-actions [state]
  (let [enemies (helpers/listen state)]
    (reduce
      (fn [state enemy]
        (if (enemy :logic)
          (take-enemy-action state enemy ((enemy :logic) state))
          state))
      state
      enemies)))

(defn play-turn [init-state users-code]
  (let [warrior-action (users-code (get-public-state init-state))
        ; TODO validate warrior-action
        ]
    (-> init-state
        increment-tick
        (take-warrior-action warrior-action)
        remove-dead-units
        take-npc-actions
        check-warrior-dead
        check-warrior-stalled)))

(defn play-level [history users-code]
  (if (or
        (:game-over? (last history))
        (warrior-at-stairs? (last history)))
    history
    (play-level (conj history (play-turn (last history) users-code)) users-code)))

(defn start-level [level-definition users-code]
  (let [init-state [(import/generate-initial-level-state level-definition)]]
    (play-level init-state users-code)))

(defn play-levels [level-definitions users-code]
  (let [history (reduce
                  (fn [memo ld]
                    (if (:game-over? (last memo))
                      memo
                      (concat memo
                              (play-level
                                [{:messages (conj (:messages (last memo))
                                                  (str "You enter room " (ld :id)))
                                  :board (import/extract-board (ld :board))
                                  :tick 0}]
                                users-code))))
                  [{:messages ["You enter the tower"]}] level-definitions)]
    (update-in (vec history) [(dec (count history)) :messages]
                 conj "You have reached the top of the tower")))
