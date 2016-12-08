(ns clojure-warrior.levels)

(def levels
  [{:id 1
    :description "You see before yourself a long hallway with stairs at the end. There is nothing in the way."
    :tip "Call warrior.walk! to walk forward in the Player 'play_turn' method."
    :time-bonus 15
    :ace-score 10
    :board [[:*> nil nil nil nil nil nil :__]]
    :abilities #{:walk}}

   {:id 2
    :description "It is too dark to see anything, but you smell sludge nearby."
    :tip "Use warrior.feel.empty? to see if there is anything in front of you, and warrior.attack! to fight it. Remember, you can only do one action (ending in !) per turn."
    :clue "Add an if/else condition using warrior.feel.empty? to decide whether to warrior.attack! or warrior.walk!."
    :time-bonus 20
    :ace-score 26
    :board [[:*> nil nil nil :<s nil nil :__]]
    :abilities #{:walk :attack}}

   {:id 3
    :description "The air feels thicker than before. There must be a horde of sludge."
    :tip "Be careful not to die! Use warrior.health to keep an eye on your health, and warrior.rest! to earn 10% of max health back."
    :clue "When there is no enemy ahead of you call warrior.rest! until health is full before walking forward."
    :time-bonus 35
    :ace-score 71
    :board [[:*> nil :<s nil :<s :<s nil :<s :__]]
    :abilities #{:walk :attack :rest}}

   {:id 4
    :description "You can hear bow strings being stretched."
    :tip "No new abilities this time, but you must be careful not to rest while taking damage. Save a @health instance variable and compare it on each turn to see if you're taking damage."
    :clue "Set @health to your current health at the end of the turn. If this is greater than your current health next turn then you know you're taking damage and shouldn't rest."
    :time-bonus 45
    :ace-score 90
    :board [[:*> nil :<S :<a nil :<S :__]]
    :abilities #{:walk :attack :rest}}

   {:id 5
    :description "You hear cries for help. Captives must need rescuing."
    :tip "Use warrior.feel.captive? to see if there is a captive and warrior.rescue! to rescue him. Don't attack captives."
    :clue "Don't forget to constantly check if you're taking damage. Rest until your health is full if you aren't taking damage."
    :time-bonus 45
    :ace-score 123
    :board [[:*> nil :<C :<a :<a :<S :<C :__]]
    :abilities #{:walk :attack :rest :rescue}}

   {:id 6
    :description  "The wall behind you feels a bit further away in this room. And you hear more cries for help."
    :tip "You can walk backward by passing ':backward' as an argument to walk!. Same goes for feel, rescue! and attack!. Archers have a limited attack distance."
    :clue "Walk backward if you are taking damage from afar and do not have enough health to attack. You may also want to consider walking backward until warrior.feel(:backward).wall?."
    :time-bonus 45
    :ace-score 90
    :board [[:C> nil :*> nil :<S nil :<a :<a :__]]
    :abilities #{:walk :attack :rest :rescue}}

   {:id 7
    :description "You feel a wall right in front of you and an opening behind you."
    :tip "You are not as effective at attacking backward. Use warrior.feel.wall? and warrior.pivot! to turn around."
    :time-bonus 30
    :ace-score 50
    :board [[:__ :a> nil :S> nil :*>]]
    :abilities #{:walk :attack :rest :rescue :pivot}}

   {:id 8
    :description "You hear the mumbling of wizards. Beware of their deadly wands! Good thing you found a bow."
    :tip "Use warrior.look to determine your surroundings, and warrior.shoot! to fire an arrow."
    :clue "Wizards are deadly but low in health. Kill them before they have time to attack."
    :time-bonus 20
    :ace-score 46
    :board [[:*> nil nil :<C :<w :<w :__]]
    :abilities #{:walk :attack :rest :rescue :pivot :shoot}}

   {:id 9
    :description "Time to hone your skills and apply all of the abilities that you have learned."
    :tip "Watch your back."
    :clue "Don't just keep shooting the bow while you are being attacked from behind."
    :time-bonus 40
    :ace-score 100
    :board [[:__ :C> :a> nil nil :*> nil :<S nil :<w :<C]]
    :abilities #{:walk :attack :rest :rescue :pivot :shoot}}])
