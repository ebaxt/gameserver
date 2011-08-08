(ns gameserver.gameserver
  (:use [clojure.java.io :only [reader writer]]
    [clojure.contrib.server-socket :only [create-server connection-count close-server]]))

(def players (ref {}))
(def last-call (ref {}))
(def score (ref {}))
(def tools #(:stone :paper :sissors))

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

(defn execute [command player]
  (str player ":" command))

(defn validate-input [valid-inputs input]
  (if (contains? valid-inputs input)
    input
    (do (println ":invalid-input")
      (flush)
      (recur valid-inputs (read-line)))))

(defn select [player]
  (println :select) (flush)
  (let [tool (validate-input tools (read-line))]
    (dosync
      last-call assoc player tool)
    (simulate-game player :evaluate)))

(defn wait [player pred next]
  (if (pred)
    (next)
    (recur player pred next)))

(defn evaluate [player]
  (if )
  )

(defn simulate-game [player state & r]
  (case state
    :select (select player)
    :wait (wait player #(= (count @last-call) 2) #(simulate-game player :evaluate))
    :evaluate (evaluate player)
    ))


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
        (alter players assoc player {:in *in* :out *out*}))
      (wait-for-enough-players) (simulate-game player :start)))))


(defn -main
  ([port]
    (def server (create-server (Integer. port) register-players))
    (println "Launching game server on port" port))
  ([] (-main 3333)))