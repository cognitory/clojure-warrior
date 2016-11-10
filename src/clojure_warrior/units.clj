(ns clojure-warrior.units)

(def reference
  {:archer
   {:type :archer
    :define-char \a
    :display-char \a
    :abilities #{:shoot :look}
    :shoot-power 3
    :max-health 7

    :play (fn []
            ; look in each direction, shoot if player
            )}

   :captive
   {:type :captive
    :max-health 1
    :define-char \C
    :display-char \C}

   :sludge
   {:type :sludge
    :define-char \s
    :display-char \s
    :abilities #{:attack :feel}
    :attack-power 3
    :max-health 12
    :play (fn []
            ; feel in each direction, shoot if player
            )}

   :thick-sludge
   {:type :thick-sludge
    :define-char \S
    :display-char \S
    :abilities #{:attack :feel}
    :attack-power 3
    :max-health 24
    :play (fn []
            ; feel in each direction, shoot if player
            )}

   :wizard
   {:type :wizard
    :define-char \w
    :display-char \w
    :abilities #{:shoot :look}
    :shoot-power 11
    :max-health 3
    :play (fn []
            ; look in each direction, shoot if player
            )}

   :warrior
   {:type :warrior
    :define-char \*
    :display-char \@
    :max-health 20
    :attack-power 5
    :shoot-power 3}

   :wall
   {:type :wall
    :define-char \-
    :display-char \|}

   :stairs
   {:type :stairs
    :define-char \_
    :display-char \>}

   :floor
   {:type :floor
    :define-char nil
    :display-char " "}})

(def define-char->type
  (reduce
    (fn [memo u]
      (assoc memo (u :define-char) (u :type)))
    {}
    (vals reference)))
