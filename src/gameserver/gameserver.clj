(ns gameserver.gameserver
  (:use [clojure.java.io :only (reader writer)]
    [clojure.contrib.server-socket :only (create-server connection-count close-server)]
    [clojure.tools.logging :only (info)]))

(def *oponent*)
(def *player*)

(def players (atom {}))
(def tools #{:rock :paper :scissors})
(def rules {:rock :scissors :scissors :paper :paper :rock})

(defn- get-unique-player-name [name]
  (if (@players name)
    (do
      (println "That name is already in use, try again: ") (flush)
      (recur (read-line)))
    name))

(defn find-oponent [player]
  (first (filter #(not= % player) (keys @players))))

(defn find-result [player-move oponent-move]
  (if (= player-move oponent-move)
    :tie
    (if (= (rules player-move) oponent-move)
      :winner
      :looser)))

(defn wait-until-oponent [action]
  (while (not (get-in @players [*oponent* action]))))

(declare make-selection)

(defn clear-and-restart []
  (wait-until-oponent :reported)
  (swap! players update-in [*oponent*] assoc :reported false :selected nil :result nil))

(defn evaluate []
  (wait-until-oponent :selected)
  (let [player-move (get-in @players [*player* :selected])
        oponent-move (get-in @players [*oponent* :selected])
        result (find-result player-move oponent-move)]
    (swap! players update-in [*player*] assoc :result result :reported true)
    (println [result oponent-move]) (flush)
    (if (= result :winner)
      (swap! players update-in [*player* :score] inc)))
  (clear-and-restart))

(defn validate-input [valid-inputs input]
  (if (contains? valid-inputs (keyword input))
    (keyword input)
    (do (println ":invalid-input")
      (flush)
      (recur valid-inputs (read-line)))))

(defn make-selection [rounds-left]
  (while (get-in @players [*player* :selected]))
  (println ":select") (flush)
  (let [tool (validate-input tools (read-line))]
    (swap! players assoc-in [*player* :selected] tool))
  (evaluate)
  (dec rounds-left))

(defn wait-for-other-player []
  (while (< (count @players) 2)
    (println ":waiting")
    (flush)
    (Thread/sleep 2000)))

(defn play-rounds [rounds player]
  (binding [*oponent* (find-oponent player)
            *player* player]
    (loop [rounds-left rounds]
      (when (pos? rounds-left)
        (recur (make-selection rounds-left))))))

(defn register-players [in out]
  (binding [*in* (reader in)
            *out* (writer out)]
    (println "Enter player name: ") (flush)
    (let [player (get-unique-player-name (read-line))]
      (swap! players assoc player {:selected nil :result nil :score 0 :reported false})
      (wait-for-other-player)
      (play-rounds 100 player)))
  (println @players))

(def server (create-server 3333 register-players))