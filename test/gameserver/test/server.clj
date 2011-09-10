(ns gameserver.test.server
  (:use gameserver.server)
  (:use clojure.test midje.sweet))

(deftest server
  (fact (add 2 3) => 6))

(run-tests)

