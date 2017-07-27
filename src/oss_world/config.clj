(ns oss-world.config
  (:require [clojure.java.io :as io]
            [cemerick.pomegranate.aether]))

(def app-directory
  (let [os-name (System/getProperty "os.name")
        app-directory "/oss-world"]
    (if (.contains os-name "Win")
      (str (System/getenv "AppData") app-directory)
      (let [user-home (System/getProperty "user.home")]
        (if (.contains os-name "Mac")
          (str user-home "/Library/Application Support/" app-directory)
          ;; If not Mac or Windows, assuming Linux-like
          (str user-home "/.oss-world"))))))

(def max-recent-activity 15)
(def missing-image-location "not-found.png")
(def default-repositories (merge cemerick.pomegranate.aether/maven-central
                                 {"clojars" "https://clojars.org/repo"}))
