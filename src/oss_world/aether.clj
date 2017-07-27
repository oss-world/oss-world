(ns oss-world.aether
  (:require [cemerick.pomegranate.aether :as aether]
            [cemerick.pomegranate :as pom]
            [oss-world.config :as config]
            [oss-world.kahn :refer [kahn-sort]]
            [clojure.java.io :as io]))

(def offline? (atom false))

;; Much of this code is inspired by boot's handling of dependencies

(defn resolve-dependencies
  "Uses an Aether repository to download the dependencies based on the
  dependency graph given.

  dependencies should be in the format used by lein/boot."
  [dependencies]
  (->
   (try
     (aether/resolve-dependencies
      :coordinates dependencies
      :repositories config/default-repositories
      :offline? @offline?)
     (catch Exception e
       (let [root-cause (last (take-while identity (iterate (memfn getCause) e)))]
         (if-not (and (not @offline?) (instance? java.net.UnknownHostException root-cause))
           (throw e)
           (do (reset! offline? true)
               (resolve-dependencies dependencies))))))))

(defn add-dependencies
  "Adds dependencies to the classpath, downloading them if needed."
  [dependencies]
  (doseq [jar (->> dependencies
                   resolve-dependencies
                   kahn-sort
                   (map #(->> % meta :file)))]
    (pom/add-classpath jar)))
