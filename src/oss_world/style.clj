(ns oss-world.style
  (:require [garden.def :refer [defstyles]]
            [garden.stylesheet :refer [rule]]))

;; Base color palette: http://paletton.com/#uid=60m0u0kLixvp4JuxXK4MCq1OMk5

(def menu-item-background-color {:normal "#FF9E00"
                                 :hover "#FF8E4C"})

(def menu-background-color {:normal "black";"#A03C00"
                            :hover "#D15B17"})

(def activity-margins {:-fx-padding "5"})

(def root
  [:.root {:-fx-base "#FF5F00"}])

(def menubar-style
  [:.menu-bar {:-fx-background-color (:normal menu-background-color)}])

(def menu-style
  [:.menu {:-fx-padding "20"
           :-fx-background-color (:normal menu-background-color)
           :margin "0"}
   [:.label {:-fx-text-fill "#FF8237"
             :-fx-font-size "20"
             :-fx-font-family "serif"
             :-fx-font-weight "bold"}]
   [:&:hover {:-fx-background-color (:hover menu-background-color)}]])

(def menu-item-style
  [:.menu-item {:-fx-padding "10"
                :-fx-background-color (:normal menu-item-background-color)}
   [:.label {:-fx-text-fill "black"
             :-fx-font-size "20"
             :-fx-font-weight "bold"}]
   [:&:hover {:-fx-background-color (:hover menu-item-background-color)}]])

(def context-menu-style
  [:.context-menu {:-fx-skin "\"com.sun.javafx.scene.control.skin.ContextMenuSkin\""
                   :-fx-background-color (:normal menu-item-background-color)}
   [:&:hover {:-fx-background-color (:hover menu-item-background-color)}]])

(def grid-pane-style
  [:.grid-pane {:-fx-background-color "green"}])

(def bordered-style
  [:.bordered {:-fx-border-style "solid"
               :-fx-border-color "black"}])

(def activity-item-style
  [:.activity-button {:-fx-background-color "#00AA7F"
                      :-fx-background-radius "0"
                      :-fx-label-padding "10 5 10 5"
                      :-fx-max-width (str 10000)
                      :-fx-vgrow "false"
                      :-fx-alignment "CENTER_LEFT"
                      :-fx-text-alignment "LEFT"}
   [:&:hover {:-fx-background-color "#33BB99"
              :-fx-insets "0 0 0 0"}]])

(def finish-action-button
  [:.finish-action-button {:-fx-padding "5px"
                           :-fx-label-padding "2px"
                           :-fx-pref-width "10px"}])

(defstyles main
  root
  menubar-style
  menu-style
  menu-item-style
  context-menu-style
  grid-pane-style
  bordered-style
  activity-item-style
  [:.empty-activity (merge activity-margins
                           {:-fx-font-style "italic"
                            :-fx-text-alignment "center"})]
  [:#recent-activity {:-fx-font-weight "bold"
                      :-fx-font-size "20px"
                      :-fx-padding "10"
                      :-fx-text-fill "black"
                      :-fx-text-alignment "center"}])
