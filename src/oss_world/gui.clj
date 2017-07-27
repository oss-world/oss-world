(ns oss-world.gui
  (:require [fn-fx.fx-dom :as dom]
            [fn-fx.diff :refer [component defui render should-update?]]
            [fn-fx.controls :as ui]
            [digest :as di]
            [oss-world.config :as config]
            [clojure.java.io :as io]
            [clojure.edn :as edn])
  (:import (javafx.collections FXCollections)
           javafx.scene.image.Image
           javafx.application.Platform))
;;WARNING most methods in this namespace are impure and private. Generally
;;aplications should only add data to the app-state atom

(defn create-activity-button
  "This function takes a list of objects representing the activities (or favorites)
  of the user and returns a list of ui items appropriate to be the children of a
  JFK pane.

  Each activity should be a vector with three properties in this order: an icon
  (this should be a java Image object), a name, and a function to call when clicked."
  [activties empty-message args width-key]
  (if (empty? activties)
    [(ui/label
      :style-class ["empty-activity"]
      :text empty-message)]
    (letfn [(generate-favorite [[icon msg callback]]
              (ui/button
               :style-class ["activity-button"]
               :text msg
               :wrap-text true
               :min-width (get-in args width-key 0)
               :graphic (ui/image-view
                         :image icon
                         :preserve-ratio true
                         :fit-width 20)
               :on-action {:event :activity-clicked, :callback callback}))]
      (map generate-favorite activties))))

(defn- create-activity-menus
  "Creates the menu items for the activities.

  They are in the same format as the activity buttons above."
  [menus]
  (if (empty? menus)
    [(ui/menu-item
     :text "No activities installed."
     :style-class ["empty-activity"])]
    (letfn [(create-activity-menu-item [[id img msg callback]]
              (ui/menu-item
               :text msg
               :graphic (ui/image-view
                         :image img
                         :preserve-ratio true
                         :fit-width 10)
               :on-action {:event :activity-clicked, :callback callback}))]
      (map create-activity-menu-item menus))))


(def default-padding
  (ui/insets
   :bottom 25
   :left 25
   :right 25
   :top 25))

(defn- generate-menu
  "Generates the menu for the WelcomeWindow."
  [args]
  (ui/menu-bar
   :grid-pane/column-index 0
   :grid-pane/column-span 2
   :grid-pane/row-index 0
   :grid-pane/row-span 1
   :menus [(ui/menu
            :text "File"
            :items [(ui/menu-item
                     :text "Edit Favorites"
                     :on-action {:event :edit-favorites})
                    (ui/menu-item
                     :text "Exit"
                     :on-action {:event :exit!})])
           (ui/menu
            :text "Activities"
            :items (create-activity-menus (:menus args)))]))

(defn- generate-favorites
  "Generates the list of favorites for the logged in user."
  [args]
  (let [width-key [:ui :min-width :favorites]]
    (ui/scroll-pane
     :grid-pane/column-index 0
     :grid-pane/column-span 1
     :grid-pane/row-index 1
     :grid-pane/row-span 1
     :style-class ["bordered"]
     :content (ui/v-box
               :id :favorite-box
               :padding (ui/insets
                         :bottom 5
                         :left 2
                         :right 2
                         :top 5)
               :children (create-activity-button (map rest (:favorites args))
                                                 ;; The first argument in each favorite is the id, which is not used by the create-activity-button function.
                                                 "No favorites added."
                                                 args
                                                 width-key)))))

(def urls-for-image (atom {}))

(defn- get-image-for-url
  "Gets the image for the passed url"
  [url]
  (if (some #{url} (keys @urls-for-image))
    (get @urls-for-image url)
    (let [url-img (Image. url)
          img (if-not (.isError url-img)
                url-img
                (Image. (.toString (io/resource config/missing-image-location))))]
      (println img)
      (swap! urls-for-image assoc url img)
      img)))

(defn- create-recent-activity-buttons
  "Converts the passed recent-activity forms into a form acceptable by
  create-activity-button."
  [args width-key]
  (create-activity-button
   (->> (:activity args)
        ;; TODO Support actions from recent activity
        reverse ; This makes things appear in the correct order
        (map #(concat % [identity]))
        (map #(assoc (vec %) 0 (get-image-for-url (first %)))))
   "No recent activity. Go do something!"
   args width-key))

(defn- generate-activity
  "Generates the list of recent activity created by the app."
  [args]
  (let [width-key [:ui :min-width :activity]]
  (ui/scroll-pane
   :id :activity-scroll
   :grid-pane/column-index 1
   :grid-pane/column-span  1
   :grid-pane/row-index 1
   :grid-pane/row-span 1
   :style-class ["bordered"]
   :listen/width {:event :width-change
                  :key width-key
                  :fn-fx/include {:activity-scroll #{:width}}}
   :content (ui/v-box
             :padding (ui/insets
                       :bottom 5
                       :left 2
                       :right 2
                       :top 5)
             :children (concat [(ui/label
                                 :text "Recent Activity"
                                 :max-width Double/MAX_VALUE
                                 :id "recent-activity")]
                               (create-recent-activity-buttons args width-key))))))

(defui WelcomeWindow
  (render
   [this args]
   (ui/grid-pane
    :alignment :top-left
    :hgap 0
    :vgap 0
    :padding default-padding
    :children [(generate-menu args)
               (generate-favorites args)
               (generate-activity args)]
    :column-constraints [(ui/column-constraints)
                         (ui/column-constraints
                          :hgrow :always)]
    :row-constraints [(ui/row-constraints)
                      (ui/row-constraints
                       :vgrow :always)])))

(defui Stage
  (render [this args]
          (ui/stage
           :title "Welcome to the OSS World"
           :shown (get-in args [:ui :show-stage])
           :on-close-request {:event :exit!}
           :scene (ui/scene
                   :stylesheets (get-in args [:ui :stylesheets])
                   :root (welcome-window args)))))

(def app-state (atom {:ui {:stylesheets ["main.css"]
                           :show-stage true
                           :show-edit-favorites false}
                      :favorites []
                      :activity []
                      :menus []}))

(defui EditWindow
  (render
   [this args]
   (ui/border-pane
    :center (ui/h-box
             :children [(ui/v-box
                         :h-box/hgrow :always
                         :children [(ui/label
                                     :style-class ["edit-label"]
                                     :text "Activities")
                                    (ui/list-view
                                     :id :activity-view
                                     :v-box/vgrow :always
                                     :items (:activity-list args))])
                        (ui/v-box
                         :alignment :center
                         :children [(ui/button
                                     :text "<"
                                     :on-action {:event :remove-favorite
                                                 :fn-fx/include {:favorite-view #{:selection-model}}})
                                    (ui/button
                                     :text ">"
                                     :on-action {:event :add-favorite
                                                 :fn-fx/include {:activity-view
                                                                 #{:selection-model}}})])
                        (ui/v-box
                         :h-box/hgrow :always
                         :children [(ui/label
                                     :style-class ["edit-label"]
                                     :text "Favorites")
                                    (ui/list-view
                                     :id :favorite-view
                                     :v-box/vgrow :always
                                     :items (:favorites-list args))])])
    :bottom (ui/h-box
             :alignment :baseline-right
             :spacing 10
             :children [(ui/button
                         :style-class ["finish-action-button"]
                         :text "Cancel"
                         :on-action {:event :cancel})
                        (ui/button
                         :style-class ["finish-action-button"]
                         :text "Save"
                         :on-action {:event :save})]))))

(defui EditFavorites
  (render
   [this args]
   (ui/stage
    :title "Choose Favorites"
    :shown (get-in args [:ui :shown])
    :scene (ui/scene
            :stylesheets (get-in @app-state [:ui :stylesheets])
            :root (edit-window args)))))

(def edit-state (atom {:favorites-list []
                       :activity-list []
                       :ui {:shown true}}))

(defn remove-favorite [to-remove favorites activities]
  (if-not (or
           (not (some #{to-remove} favorites))
           (= "" to-remove))
    [:favorites-list (remove #{to-remove} favorites) ; This doesn't need to be sorted because removing an item doesn't change the sorting
     :activity-list (sort (conj activities to-remove))]
    [:favorites-list favorites
     :activity-list activities]))

(defn add-favorite [to-add favorites activities]
  (if-not (or (= "" to-add)
              (not (some #{to-add} activities)))
    [:favorites-list (sort (conj favorites to-add))
     :activity-list (remove #{to-add} activities)]
    [:favorites-list favorites
     :activity-list activities]))

(defn get-favorites-for-names [names activities]
  (filter #(some #{(nth % 2)} names) activities))

(defn show-edit []
  (let [favorites (map #(nth % 2) (:favorites @app-state))
        activities (map #(nth % 2) (:menus @app-state))
        non-favorite-activities (filter #(not (some #{%} favorites)) activities)
        handler-fn (fn [{:keys [event] :as evt-data}]
                     (println "Event Data:" evt-data)
                     ((case event
                        :remove-favorite #(apply swap! edit-state assoc
                                                 (remove-favorite
                                                  (str (first (.getSelectedItems (get-in evt-data [:fn-fx/includes :favorite-view :selection-model]))))
                                                  (:favorites-list @edit-state)
                                                  (:activity-list @edit-state)))
                      :add-favorite #(apply swap! edit-state assoc
                                            (add-favorite
                                             (str (first (.getSelectedItems
                                                          (get-in evt-data [:fn-fx/includes :activity-view :selection-model]))))
                                             (:favorites-list @edit-state)
                                             (:activity-list @edit-state)))
                      :save #(do (swap! app-state (fn [x]
                                                    (-> x
                                                        (assoc :favorites
                                                               (get-favorites-for-names (:favorites-list @edit-state) (:menus @app-state)))
                                                   (assoc-in [:ui :show-stage] true))))
                                 (swap! edit-state assoc-in [:ui :shown] false))
                      :cancel #(do (swap! app-state assoc-in [:ui :show-stage]
                                          true)
                                 (swap! edit-state assoc-in [:ui :shown] false)))))
        ui-state (agent (dom/app (edit-favorites @edit-state) handler-fn))]
    (add-watch edit-state :edit-ui (fn [_ _ old-state new-state]
                                     (send ui-state
                                           (fn [old-ui]
                                             (dom/update-app old-ui
                                                             (edit-favorites new-state))))))
  (reset! edit-state (-> {}
                         (assoc :favorites-list favorites)
                         (assoc :activity-list non-favorite-activities)
                         (assoc-in [:ui :shown] true)))
  (swap! app-state assoc-in [:ui :show-stage] false)))

(def gui-config (str config/app-directory "/gui-state.edn"))

(defn on-shutdown
  "Method to run inside a shutdown hook to save necessary GUI state."
  []
  (let [conf-file (io/file gui-config)]
    (spit conf-file (pr-str
                     {:favorites (map first (:favorites @app-state))
                      :activity (:activity @app-state)}))))

(defn display-stage []
  (let [handler-fn (fn [{:keys [event] :as all-data}]
                     (println "UI Event:" event)
                     (println "Full Data:" all-data)
                     ((case event
                        :exit! #(do (Platform/exit)
                                    (shutdown-agents))
                        :activity-clicked (:callback all-data)
                        :edit-favorites show-edit
                        :width-change #(swap! app-state assoc-in
                                              (:key all-data)
                                              (- (:fn-fx.listen/new all-data)
                                                 10)))))
        ui-state (agent (dom/app (stage @app-state) handler-fn))]
    (add-watch app-state :ui (fn [_ _ old-state new-state]
                               (send ui-state
                                     (fn [old-ui]
                                       (dom/update-app old-ui
                                                       (stage new-state)))))))
  (swap! app-state assoc-in [:ui :show-stage] true))

(defn read-configuration []
  (let [conf-file (io/file gui-config)
        conf (if (.exists conf-file)
               (edn/read-string (slurp gui-config)))]
    (if (.exists conf-file)
      (swap! app-state assoc
             :favorites (filter #(some #{(first %)} (:favorites conf))
                                (:menus @app-state))
             :activity (:activity conf)))))

(defn -main []
  (read-configuration)
  (.addShutdownHook (Runtime/getRuntime) (Thread. on-shutdown))
  (display-stage))
