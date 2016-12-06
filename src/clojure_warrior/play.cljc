(ns clojure-warrior.play
  (:require
    [clojure-warrior.import :as import]
    [clojure-warrior.helpers :as helpers]
    [clojure-warrior.unit :refer [take-warrior-action]]
    [clojure-warrior.state :refer [get-warrior
                                   add-message
                                   assoc-at]]))

(defn map-units [f board]
  (mapv (fn [row]
          (mapv f row)) board))

(defn get-public-unit [unit]
  (select-keys unit [:type :health :direction
                     :enemy? :melee? :ranged?
                     :captive? :empty? :stairs?]))

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
  (if (< 40 (state :tick))
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
        (add-message (str "A " (name (enemy :type)) " shoots you"
                          " and you lose " health-delta " health, down to " new-health))
        (assoc-at (:position warrior) :health new-health))))

(defmethod take-enemy-action :attack
  [state enemy _]
  (let [warrior (get-warrior (state :board))
        strength (enemy :attack-power)
        new-health (max 0.0 (- (:health warrior) strength))
        health-delta (- (:health warrior) new-health)]
    (-> state
        (add-message (str "A " (name (enemy :type)) " attacks you"
                          " and you lose " health-delta " health, down to " new-health))
        (assoc-at (:position warrior) :health new-health))))

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
        post-warrior-state (-> init-state
                               increment-tick
                               (take-warrior-action warrior-action))
        post-env-state (-> post-warrior-state
                           remove-dead-units)
        post-npc-state (-> post-env-state
                           take-npc-actions)
        post-env2-state (-> post-npc-state
                            check-warrior-dead
                            check-warrior-stalled)]
    (remove nil?
            [post-warrior-state
             (when (not= post-env-state post-warrior-state)
               post-env-state)
             (when (not= post-npc-state post-env-state)
               post-npc-state)
             (when (not= post-env2-state post-npc-state)
               post-env2-state)])))

(defn play-level [history users-code]
  (if (or
        (:game-over? (last history))
        (warrior-at-stairs? (last history)))
    history
    (play-level (concat history
                      (play-turn (last history) users-code)) users-code)))

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
    (if (:game-over? (last history))
      (vec history)
      (update-in (vec history) [(dec (count history)) :messages]
                 conj "You have reached the top of the tower"))))
