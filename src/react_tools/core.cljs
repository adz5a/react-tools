(ns react-tools.core
  (:require [react]
            [react-dom])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def root (.getElementById js/document "root"))

(defcomponent Link
  [:a {:href "/hello"} "A link"])

(defcomponent App
  [:div {:onClick (partial println "hello from react")}
   "wesh world"
   [Link]])

(react-dom/render
  (jsx [App])
  root)
