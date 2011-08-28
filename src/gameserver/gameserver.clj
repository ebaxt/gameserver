(ns gameserver.gameserver
  (:use [clojure.java.io :only (reader writer)]
    [clojure.contrib.server-socket :only (create-server connection-count close-server)]
    [clojure.tools.logging :only (info)]))

(def players (atom {}))

(def *oponent*)
(def *player*)
(def tools #{:rock :paper :scissors})
(def rules {:rock :scissors :scissors :paper :paper :rock})

(defn- get-unique-player-name [name]
  (if (@players name)
    (do (println "That name is already in use, try again: ")
      (flush)
      (recur (read-line)))
    name))

(defn oponent [player]
  (first (filter #(not= % player) (keys @players))))

(defn find-result [player-move oponent-move]
  (if (= player-move oponent-move)
    :tie
    (if (= (rules player-move) oponent-move)
      :winner
      :looser)))

(declare make-selection)

(defn clear-and-restart []
  (while (not (get-in @players [*oponent* :reported])))
  (swap! players assoc-in [*oponent* :reported] false)
  (swap! players assoc-in [*oponent* :selection] nil)
  (swap! players assoc-in [*oponent* :result] nil))

(defn evaluate []
  (while (not (get-in @players [*oponent* :selection])))
  (let [player-move (get-in @players [*player* :selection])
        oponent-move (get-in @players [*oponent* :selection])
        result (find-result player-move oponent-move)]
    (swap! players assoc-in [*player* :result] result)
    (swap! players assoc-in [*player* :reported] true)
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
  (while (get-in @players [*player* :selection]))
  (println ":select") (flush)
  (let [tool (validate-input tools (read-line))]
    (swap! players assoc-in [*player* :selection] tool))
  (evaluate)
  (dec rounds-left))

(defn wait-for-other-player [player]
  (while (< (count @players) 2)
    (println ":waiting")
    (flush)
    (Thread/sleep 2000))
  (binding [*oponent* (oponent player)
            *player* player]
    (loop [rounds-left 100000]
      (when (pos? rounds-left)
        (recur (make-selection rounds-left))))))

(def register-players (fn [in out]
  (binding [*in* (reader in)
            *out* (writer out)]
    (println "Enter player name: ") (flush)
    (let [player (get-unique-player-name (read-line))]
      (swap! players assoc player {:selection nil :result nil :score 0 :reported false})
      (wait-for-other-player player)))))

(def server (create-server 3333 register-players))