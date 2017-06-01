(ns clojure-warrior.unit
  (:require
    [clojure-warrior.state :refer [get-warrior
                                   unit-at-position
                                   first-unit-in-range
                                   action-target-position
                                   add-message
                                   set-at
                                   assoc-at
                                   update-at]]))

(defmulti take-warrior-action
  "Returns new state after performing warrior action"
  (fn [state action] (first action)))

(defmethod take-warrior-action :walk
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (action-target-position warrior direction)
        target (unit-at-position (state :board) target-position)
        action-text (str "You walk " (name direction))]
    (as-> state $
      (cond
        (= :floor (:type target))
        (-> $
            (add-message action-text)
            (set-at (warrior :position) {:type :floor})
            (set-at (target :position) (dissoc warrior :position)))
        (= :stairs (:type target))
        (-> $
            (add-message (str action-text " and up the stairs"))
            (set-at (warrior :position) {:type :floor})
            (set-at (target :position) (-> warrior
                                           (assoc :at-stairs true)
                                           (dissoc :position))))
        :else
        (add-message $ (str action-text " and bump into a " (name (target :type))))))))

(defmethod take-warrior-action :pivot
  [state _]
  (let [warrior (get-warrior (state :board))
        new-direction (case (warrior :direction)
                        :east :west
                        :west :east)]
    (-> state
        (add-message (str "You pivot and are now facing " (name new-direction)))
        (assoc-at (warrior :position) :direction new-direction))))

(defmethod take-warrior-action :rest
  [state _]
  (let [warrior (get-warrior (state :board))
        max-health (:max-health warrior)
        health (:health warrior)
        new-health (min max-health (+ health (* max-health 0.1)))
        health-delta (- new-health health)
        action-text "You rest"]
    (as-> state $
        (if (> health-delta 0)
          (add-message $ (str action-text " and receive " health-delta " health from resting, up to " new-health " health"))
          (add-message $ (str action-text " but are already fit as a fiddle")))
        (assoc-at $ (warrior :position) :health new-health))))

(defmethod take-warrior-action :attack
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target-position (action-target-position warrior direction)
        target (unit-at-position (state :board) target-position)
        power-multiplier (case direction
                           :forward 1.0
                           :backward 0.5)
        attack-power (* (warrior :attack-power) power-multiplier)
        action-text (str "You attack " (name direction))]
    (as-> state $
      (if (and target (target :health))
        (let [damage (min attack-power (target :health))
              target-new-health (max 0 (- (target :health) damage))]
          (-> $
              (add-message (str action-text " and a " (name (:type target)) " takes " damage " damage, "
                                (if (< 0 target-new-health)
                                  (str "and has " target-new-health " health left")
                                  (str "and dies"))))
              (assoc-at target-position :health target-new-health)))
        (add-message $ (str action-text " but you hit nothing"))))))

(defmethod take-warrior-action :shoot
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target (first-unit-in-range (state :board) warrior direction 2)
        attack-power (warrior :shoot-power)
        action-text (str "You shoot " (name direction))]
    (as-> state $
      (if (and target (target :health))
        (let [damage (min attack-power (target :health))
              target-new-health (max 0 (- (target :health) damage))]
          (-> $
              (add-message (str action-text " and a " (name (:type target)) " takes " damage " damage, "
                                (if (< 0 target-new-health)
                                  (str "and has " target-new-health " health left")
                                  (str "and dies"))))
              (assoc-at (target :position) :health target-new-health)))
        (-> $
            (add-message (str action-text " but you hit nothing")))))))

(defmethod take-warrior-action :rescue
  [state [_ direction]]
  (let [warrior (get-warrior (state :board))
        target (first-unit-in-range (state :board) warrior direction 1)
        action-text (str "You reach " (name direction))]
    (as-> state $
      (if (and target (= :captive (:type target)))
        (-> $
            (add-message (str action-text " and unbind a captive. You earn 20 points."))
            (set-at (target :position) {:type :floor})
            (update-at (warrior :position) :points (fn [points]
                                                     (+ points 20))))
        (add-message $ (str action-text " but there is no captive to rescue"))))))
