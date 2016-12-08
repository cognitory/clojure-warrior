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

(def distance-to helpers/distance-to)


(defn enter-the-tower!
  [warrior-name warrior-code]
  ; TODO
  )
(def play-levels! play/play-levels)
