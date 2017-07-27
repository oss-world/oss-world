(ns oss-world.api
  (:require [oss-world.gui :as gui]
            [oss-world.config :as config]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:gen-class
   :name oss_world.api
   :main false
   :methods [#^{:static true} [addActivity [String javafx.scene.image.Image
                                            String Runnable] void]
             #^{:static true} [addRecentActivity [String String] void]
             #^{:static true} [getConfig [String] java.util.Map]
             #^{:static true} [saveConfig [String java.util.Map] void]
             #^{:static true} [getPluginDir [String] String]
             #^{:static true} [setStageVisibility [boolean] void]]
             #^{:static true} [getDefaultStylesheets [] java.util.List]))

(def plugin-dir (str config/app-directory "/plugins"))

(defn add-activity
  "Adds an activity to the list of available activities and displays it in
  the GUI.

  Activities must be a seq with three elements in this order:
  [id img msg callback], where id is a keyword unique identifier, img is an
  Image object that represents the
  activity's icon, msg is the name of the activity, and callback is an fn
  to be called when the activity's button is pressed."
  ([activity]
   (swap! gui/app-state update :menus conj activity))
  ([id img msg callback]
   (add-activity [id img msg callback])))

(defn -addActivity [id img msg callback]
  ;;Java interop function
  (add-activity [(keyword id) img msg #(.run callback)]))

(defn add-recent-activity
  "Adds a recent activity entry to the list of recent activity on the home
  page.

  Activities must be a seq with two elements in this order:
  [img msg], where img is the url to the image that represents the
  activity's icon and msg is the name of the activity."
  ([ractivity]
   (let [old-activity (:activity @gui/app-state)
         new-activity (if (< (count old-activity) config/max-recent-activity)
                        (cons ractivity old-activity)
                        (cons ractivity (butlast old-activity)))])
   (swap! gui/app-state update :activity conj ractivity))
  ([img msg]
   (add-recent-activity [img msg])))

(defn -addRecentActivity
  ;;Java interop
  [img msg]
  (add-recent-activity img msg))

(defn get-plugin-dir
  "Returns the path to the directory that the plugin of the given id should
  put its files in."
  [id]
  (str plugin-dir "/" id))

(defn -getPluginDir
  [id]
  (get-plugin-dir id))

(defn- get-config-for-id
  "Returns the config file location for the given id."
  [id]
  (io/file (str (get-plugin-dir id) "/config.edn")))

(defn get-config
  "Tries to get the config for a given plugin id. Returns an empty map if
  no saved id found."
  [id]
  (let [config-file (get-config-for-id id)]
    (if (.exists config-file)
      (edn/read-string (slurp config-file))
      {})))

(defn -getConfig [id]
  ;;Java interop function
  (java.util.HashMap. (get-config id)))

(defn save-config
  "Saves the config for the passed id to its file."
  [id config]
  (let [config-loc (get-config-for-id id)]
    (io/make-parents config-loc)
    (spit config-loc (.toString config))))

(defn -saveConfig [id config]
  ;;Java interop
  (save-config id (into {} config)))

(defn set-stage-visibility
  "Allows plugins to hide or show the main stage (window) of the program."
  [visiblity]
  (swap! gui/app-state assoc-in [:ui :show-stage] visiblity))

(defn -setStageVisibility
  [visiblity]
  (set-stage-visibility visiblity))

(defn get-default-stylesheets
  "Returns the stylesheets used by the OSS World's main window."
  []
  (get-in @gui/app-state [:ui :stylesheets]))

(defn -getDefaultStylesheets
  []
  (get-default-stylesheets))
