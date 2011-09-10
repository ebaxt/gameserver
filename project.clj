(defproject gameserver "1.0.0-SNAPSHOT"
  :description "Simple gameserver to play Rock-Paper-Scissors"
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [midje "1.1.1"]
                 [org.clojure/tools.logging "0.1.2"]
                 [log4j "1.2.16"]
                 [seesaw "1.2.0"]]

  :repositories {"stuartsierra-releases" "http://stuartsierra.com/maven2"}

  :dev-dependencies [[swank-clojure "1.2.1"]
                     [com.stuartsierra/lazytest "1.1.2"]
                     [lein-lazytest "1.0.1"]]

  :lazytest-path ["src" "test"]

  :main gameserver.main)
