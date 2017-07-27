(ns manual-test-utils
  "This provides various functions and data to serve as utilities for testing
  non-automated testable parts of this program. It will have few, if any,
  automated tests."
  (:require  [clojure.test :as t]
             [fn-fx.controls :as ui]))

(def marker (atom {})) ; 

(def dummy-favorite-data [[(ui/image
                            :url "https://www.clker.com/cliparts/f/8/5/8/1314063746963744001red%20square-md.png")
                           "fake favorite"
                           #(do (println "awesome")
                                (swap! marker assoc :awesome (inc (get :awesome @marker 0))))]
                          [(ui/image
                            :url "http://www.freeiconspng.com/uploads/square-frame-png-32.png")
                           "cool favorite"
                           #(do (println "amazing")
                                (swap! marker assoc :amazing (inc (get :amazing @marker 0))))]])

(def img (ui/image
          :url "http://www.freeiconspng.com/uploads/square-frame-png-32.png"))

(def lots-of-data (repeat 10 [img "activity" #(println "Amazing click!")]))
