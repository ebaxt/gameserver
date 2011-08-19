(ns gameserver.gameserver
  (:use [clojure.java.io :only [reader writer]]
    [clojure.contrib.server-socket :only [create-server connection-count close-server]]))

(def players (ref {}))
(def last-call (ref {}))
(def score (ref {}))
(def tools #{:rock :paper :scissors})
(def rules {:rock :scissors :scissors :paper :paper :rock})

(defn- get-unique-player-name [name]
  (if (@players name)
    (do (print "That name is in use; try again: ")
      (flush)
      (recur (read-line)))
    name))

(defn send-to-players [message players]
  (doseq [player players]
    (binding [*out* (get-in player [1 :out])]
      (println (str message player)) (flush))))

(declare select)
(declare evaluate)
(declare report-and-clear)
(declare restart)

(defn simulate-game [player state & r]
  (case state
    :select-move (select player)
    :wait (do (while (< (count @last-call) 2)) (simulate-game player :evaluate))
    :evaluate (evaluate player)
    :report-and-clear (report-and-clear player r)
    :restart (restart player)
    ))

(defn report-and-clear [player [[result _]]]
  (dosync (alter last-call dissoc player))
  (if (= result :winner)
    (dosync
      (alter score assoc player (inc (@score player)))))
  (simulate-game player :restart))

(defn restart [player]
  (while (not (empty? @last-call)))
  (simulate-game player :select-move))

(defn validate-input [valid-inputs input]
  (if (contains? valid-inputs (keyword input))
    (keyword input)
    (do (println ":invalid-input")
      (flush)
      (recur valid-inputs (read-line)))))

(defn select [player]
  (println :select) (flush)
  (let [tool (validate-input tools (read-line))]
    (dosync
      (alter last-call assoc player tool))
    (simulate-game player :wait)))

(defn winner? [playerMove oponentMove]
  (if (= playerMove oponentMove)
    :tie
    (if (= (rules playerMove) oponentMove)
      :win
      :loose)))

(defn evaluate-round [player last-call]
  (let [oponentMove (first (dissoc last-call player))
        playerMove (vector player (last-call player))
        player-wins (winner? (playerMove 1) (oponentMove 1))]
    (case player-wins
      :win [:winner (second oponentMove)]
      :loose [:looser (second oponentMove)]
      :tie [:tie (second oponentMove)])))

(defn evaluate [player]
  (let [roundStatus (evaluate-round player @last-call)]
    (println roundStatus) (flush)
    (simulate-game player :report-and-clear roundStatus)))

(defn wait-for-enough-players []
  (while (< (count @players) 2)
    (println ":waiting")
    (flush)
    (Thread/sleep 2000)))

(def register-players (fn [in out]
  (binding [*in* (reader in)
            *out* (writer out)]
    (print "\nEnter player name: ") (flush)
    (let [player (get-unique-player-name (read-line))]
      (dosync
        (alter players assoc player {:in *in* :out *out*})
        (alter score assoc player 0))
      (wait-for-enough-players) (simulate-game player :select-move)))))


(defn -main
  ([port]
    (def server (create-server (Integer. port) register-players))
    (println "Launching game server on port" port))
  ([] (-main 3333)))