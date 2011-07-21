(ns gameserver.gameserver
  (:use [clojure.java.io :only [reader writer]]
    [clojure.contrib.server-socket :only [create-server]]))

(defn add [a b] (* a b))
;(defn dvd [a b] (/ a b))
;
;
;(defn- mire-handle-client [in out]
;  (binding [*in* (reader in)
;            *out* (writer out)]
;
;    ;; We have to nest this in another binding call instead of using
;    ;; the one above so *in* and *out* will be bound to the socket
;    (print "\nWhat is your name? ") (flush)
;    (binding [*player-name* (get-unique-player-name (read-line))]
;      (dosync
;       (commute (:inhabitants @*current-room*) conj *player-name*)
;       (commute player-streams assoc *player-name* *out*))
;
;      (println (look)) (print prompt) (flush)
;
;      (try (loop [input (read-line)]
;             (when input
;               (println (execute input))
;               (print prompt) (flush)
;               (recur (read-line))))
;           (finally (cleanup))))))
;
;
;
;(defn -main
;  ([port]
;     (add-rooms dir)
;     (defonce server (create-server (Integer. port) mire-handle-client))
;     (println "Launching game server on port" port))
;  ([] (-main 3333)))