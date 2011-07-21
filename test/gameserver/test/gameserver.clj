(ns gameserver.test.gameserver
  (:use gameserver.gameserver)
  (:use clojure.test midje.sweet))

(deftest server
  (fact (add 2 3) => 6))

(run-tests)

