(ns clojure-warrior.core
  #?(:clj (:gen-class))
  (:require
    [clojure-warrior.play :as play]
    [clojure-warrior.helpers :as helpers]))

(def feel helpers/feel)
(def look helpers/look)
(def listen helpers/listen)
(def inspect helpers/inspect)

(def stairs helpers/stairs)
(def warrior helpers/warrior)

(def say helpers/say)

(def distance-to helpers/distance-to)


(defn enter-the-tower!
  [user-code]
  (let [levels [{:id 1
                 :board [[:*> :-- :<a :__]]}
                {:id 2
                 :board [[:*> :__]]}]]
    (play/play-levels levels user-code)))


(def play-levels! play/play-levels)
