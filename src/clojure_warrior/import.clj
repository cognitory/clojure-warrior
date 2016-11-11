(ns clojure-warrior.import
  (:require
    [clojure-warrior.units :as units]))

(defn extract-unit
  "Given space notation from level description,
  returns object type and direction"
  [s]
  (if-not s
    (units/reference :floor)
    (let [chars (set (seq (name s)))
          type (or (units/define-char->type (first chars))
                   (units/define-char->type (last chars)) )
          direction (cond
                      (contains? chars \>) :east
                      (contains? chars \<) :west)
          health (:max-health (units/reference type))]
      (as-> (units/reference type) m
        (if direction (assoc m :direction direction) m)
        (if health (assoc m :health health) m)))))

(defn generate-initial-level-state
  [level-description]
  {:messages ["You enter the tower"]
   :board
   (->> level-description
        :board
        (map (fn [row]
               (concat [:-] row [:-])))
        (map (fn [row]
               (vec (map (fn [space]
                      (extract-unit space))
                    row))))
        vec)})
