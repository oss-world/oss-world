(set-env!
 :source-paths #{"src"}
 :resource-paths #{"resources"}
 :dependencies '[[org.clojure/clojure "1.8.0"]
                 [halgari/fn-fx "0.3.0"]
                 [garden "1.3.2" :scope "test"]
                 [com.cemerick/pomegranate "0.3.1"]
                 [adzerk/boot-test "1.2.0" :scope "test"]
                 [digest "1.4.6"]
                 [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]])

(require '[clojure.java.io :as io]
         '[org.martinklepsch.boot-garden :refer [garden]])

(def +version+ "0.1.0-SNAPSHOT")

(task-options!
 pom {:project 'oss-world/oss-world
      :version +version+
      :description "The foundation of the OSS World worldbuild platform."})

(deftask run
  "Runs the project"
  []
  (require '[oss-world.core :as c])
  (let [m (resolve 'c/-main)]
    (comp
     (with-pass-thru _
       (m))
     (wait))))

(deftask testing
  "Enables testing environment"
  []
  (merge-env! :source-paths #{"tests"})
  identity)

(deftask build
  "Builds a runnable uberjar"
  [u uber? bool "If true add an uberjar"]
  (let [jar-name #"oss-world.jar"]
    (comp
     (garden :styles-var 'oss-world.style/main :output-to "main.css")
     (pom)
     (aot :namespace #{'oss-world.core 'oss-world.api})
     (if uber?
       (uber)
       identity)
     (jar :file (str jar-name) :main 'oss-world.core)
     (sift :include #{jar-name}))))
