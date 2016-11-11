(ns clojure-warrior.core
  (:gen-class)
  (:require
    [clojure.string :as string]
    [clojure-warrior.units :as units]))

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
