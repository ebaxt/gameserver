(ns gameserver.gameserver
  (:gen-class)
  (:use [clojure.java.io :only (reader writer)]
    [clojure.contrib.server-socket :only (create-server connection-count close-server)]
    [clojure.tools.logging :only (info)]))

(def players (atom {}))
(def last-call (atom {}))
(def score (atom {}))
(def tools #{:rock :paper :scissors})
(def rules {:rock :scissors :scissors :paper :paper :rock})

(defn- get-unique-player-name [name]
  (if (@players name)
    (do (println "That name is already in use, try again: ")
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
  (info (str player " " state))
  (case state
    :select-move (select player)
    :wait (do (while (< (count @last-call) 2)) (simulate-game player :evaluate))
    :evaluate (evaluate player)
    :report-and-clear (report-and-clear player r)
    :restart (restart player)
    ))

(defn report-and-clear [player [[result _]]]
  (info @last-call)
  (if (@last-call :clear)
    (swap! last-call {})
    (swap! last-call assoc :clear true))
  (if (= result :winner)
    (swap! score assoc player (inc (@score player))))
  (simulate-game player :restart))

(defn restart [player]
  (if (empty? @last-call)
    (simulate-game player :select-move)
    (do (Thread/sleep 10) (recur player))))

(defn validate-input [valid-inputs input]
  (if (contains? valid-inputs (keyword input))
    (keyword input)
    (do (println ":invalid-input")
      (flush)
      (recur valid-inputs (read-line)))))

(defn select [player]
  (println :select) (flush)
  (let [tool (validate-input tools (read-line))]
    (swap! last-call assoc player tool)
    (simulate-game player :wait)))

(defn winner? [playerMove oponentMove]
  (if (= playerMove oponentMove)
    :tie
    (if (= (rules playerMove) oponentMove)
      :win
      :loose)))

(defn evaluate-round [player last-call]
  (let [oponentMove (first (dissoc (dissoc last-call player) :clear))
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
    (println "Enter player name: ") (flush)
    (let [player (get-unique-player-name (read-line))]
        (swap! players assoc player {:in *in* :out *out*})
        (swap! score assoc player 0)
      (wait-for-enough-players) (simulate-game player :select-move)))))


(defn -main
  ([port]
    (def server (create-server (Integer. port) register-players))
    (println "Launching game server on port" port))
  ([] (-main 3333)))