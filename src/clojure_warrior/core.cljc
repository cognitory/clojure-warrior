(ns clojure-warrior.core
  #?(:clj (:gen-class))
  (:require
    [clojure-warrior.play :as play]
    [clojure-warrior.helpers :refer [look feel listen
                                     stairs warrior
                                     distance-to inspect]]))

(defn enter-the-tower!
  [warrior-name warrior-code]
  ; TODO
  )
(def play-levels! play/play-levels)
