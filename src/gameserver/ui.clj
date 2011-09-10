(ns gameserver.ui
  (:use seesaw.core)
  (:require [gameserver.server :as server]))

(def status (label "Ready"))

(def p1 {:name (label "Player 1") :score (label "")})
(def p2 {:name (label "Player 1") :score (label "")})


(defn watcher [akey aref old-val new-val]
  (let [pname (first (keys new-val))]
    (text! (:name p1) pname)
    (text! (:score p1) 0)))

(defn start-server-handler [event]
  (let [rounds status]
    (text! status "Starting server...")
    (add-watch server/players "ui-watcher" watcher)
    (server/start-server rounds #(text! status "Server started, waiting for players..."))))

(defn app
  []
  (let [round-text (text "1000")]
    (frame
      :id :frame
      :title "Gameserver"
      :width 250 :height 300
      :content
      (border-panel
        :border 5
        :center
        (border-panel
          :north
          (horizontal-panel
            :border [5 "Number of rounds"]
            :items [round-text
                    (action :handler start-server-handler :name "Start")])
          :west
          (vertical-panel
            :border 30
            :items [(:name p1) (:score p1)])
          :east
          (vertical-panel
            :border 30
            :items [(:name p2) (:score p2)])
          :south status)))))