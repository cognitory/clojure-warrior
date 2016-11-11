(ns clojure-warrior.display
  (:require
    [clojure.string :as string]))

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
                                      (:display-char space)))
                               (string/join "")))))
              [line]))))
