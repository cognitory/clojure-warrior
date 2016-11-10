(ns clojure-warrior.core
  (:gen-class)
  (:require
    [clojure.string :as string]
    [clojure-warrior.units :as units]))

(defn extract-unit
  "Given space notation from level description,
  returns object type and direction"
  [s]
  (if-not s
    {:type :floor}
    (let [chars (set (seq (name s)))
          type (cond
                 (contains? chars \-) :wall
                 (contains? chars \_) :stairs
                 (contains? chars \C) :captive
                 (contains? chars \a) :archer
                 (contains? chars \*) :warrior
                 (contains? chars \s) :sludge
                 (contains? chars \S) :thick-sludge
                 (contains? chars \w) :wizard)
          direction (cond
                      (contains? chars \>) :east
                      (contains? chars \<) :west)
          health (:max-health (units/reference type))]
      (as-> {} m
        (if type (assoc m :type type) m)
        (if direction (assoc m :direction direction) m)
        (if health (assoc m :health health) m)))))

(defn generate-initial-level-state
  [level-description]
  {:board
   (->> level-description
        :board
        (map (fn [row]
               (concat [:-] row [:-])))
        (map (fn [row]
               (map (fn [space]
                      (extract-unit space))
                    row))))})

(defn generate-display [state]
  (let [width (count (first (state :board)))
        line (string/join "" (repeat width "-"))]
    (string/join "\n"
      (concat [line]
              (->> state
                   :board
                   (map (fn [row]
                          (->> row
                               (map (fn [space]
                                      (:display-char (units/reference (space :type)))))
                               (string/join "")))))
              [line]))))
