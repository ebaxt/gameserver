(ns gameserver.ui
  (:use seesaw.core)
  (:require
    [gameserver.server :as server]
    [seesaw.bind :as b]))

(def status (label "Ready"))

(def p1 (label :text "Player1" :halign :center :font {:style :bold}))
(def p2 (label :text "Player2" :halign :center :font {:style :bold}))

(def score1 (label :text 0 :halign :center :font {:style :bold :size 30}))
(def score2 (label :text 0 :halign :center :font {:style :bold :size 30}))

(defn load-players [m]
  (if (= "Player1" (text p1))
    (let [players (keys m)
          pl1 (first players)
          pl2 (second players)]
      (text! p1 pl1)
      (text! p2 pl2)
      (text! status "Running"))))

(defn update-ui [m]
  (let [pl1 (get m (text p1))
        pl2 (get m (text p2))]
    (text! score1 (:score pl1))
    (text! score2 (:score pl2))))

(defn watcher [_ _ _ new-val]
  (if (= 2 (count new-val))
    (do
      (load-players new-val)
      (update-ui new-val))))

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
            :items [p1 score1])
          :east
          (vertical-panel
            :border 30
            :items [p2 score2])
          :south status)))))

(defn start []
  (show! (app)))