(ns clojure-warrior.units)

(def reference
  {:archer
   {:type :archer
    :in-char :a
    :display-char "a"
    :abilities #{:shoot :look}
    :shoot-power 3
    :max-health 7

    :play (fn []
            ; look in each direction, shoot if player
            )}

   :captive
   {:type :captive
    :max-health 1
    :display-char "C"}

   :sludge
   {:type :sludge
    :display-char "s"
    :abilities #{:attack :feel}
    :attack-power 3
    :max-health 12
    :play (fn []
            ; feel in each direction, shoot if player
            )}

   :thick-sludge
   {:type :thick-sludge
    :display-char "S"
    :abilities #{:attack :feel}
    :attack-power 3
    :max-health 24
    :play (fn []
            ; feel in each direction, shoot if player
            )}

   :wizard
   {:type :wizard
    :display-char "w"
    :abilities #{:shoot :look}
    :shoot-power 11
    :max-health 3
    :play (fn []
            ; look in each direction, shoot if player
            )}

   :warrior
   {:type :warrior
    :display-char "@"
    :max-health 20
    :attack-power 5
    :shoot-power 3}})
