(ns gameserver.main
  (:require [gameserver.server :as server]))

(defn -main
  ([port] (server/create-server port server/register-players))
  ([] (-main 3333)))
