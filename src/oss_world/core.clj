(ns oss-world.core
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [oss-world.gui :as gui]
            [oss-world.config :as config]
            [oss-world.aether :as aether]
            [cemerick.pomegranate :as pom])
  (:gen-class))

(def main-classloader
  "The claassloader to use when adding plugins."
  (last (filter pom/modifiable-classloader? (pom/classloader-hierarchy))))

(defn- load-clojure-plugin
  [init del]
  (require (symbol (namespace init)))
  ((resolve init))
  (when del
    (do (require (symbol (namespace del)))
        (.addShutdownHook (Runtime/getRuntime)
                          (Thread. (resolve del))))))

(defn- load-java-plugin
  [init del]
  ;; Java methods don't evaluate from symbols as nicely, so we need to use eval
  (eval `(~init))
  (when del
    (.addShutdownHook (Runtime/getRuntime) (Thread. #(eval `(~del))))))

(defn load-plugin
  "Loads the specified plugin from the passed url to the edn file."
  [plugin]
  (let [{:keys [init del java?]} (edn/read-string (slurp plugin))]
    (println "Loading plugin key:" init)
    (if java?
      (load-java-plugin init del)
      (load-clojure-plugin init del))))

(defn load-plugins
  "Searches for plugins on the classpath and calls the specified init function."
  []
  (let [plugins (.getResources main-classloader "plugin.edn")]
    (if (.hasMoreElements plugins)
      (loop []
        (load-plugin (.. plugins nextElement openStream))
        (when (.hasMoreElements  plugins)
          (recur)))
      (println "No plugins"))))

(defn -main
  "The main entry point function of the OSS World."
  [& args]
  (let [plugin-file (io/file (str config/app-directory "/plugins/plugins.edn"))
        plugins (if (.exists plugin-file)
                  (edn/read-string (slurp plugin-file))
                  [])]
    (io/make-parents plugin-file)
    (aether/add-dependencies plugins))
  (load-plugins)
  (gui/-main))
