(ns react-tools.core
  (:require [react]
            [react-dom]
            [cljs-bean.core])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def root (.getElementById js/document "root"))

(defcomponent Link
  [:a {:href "/hello"} "A link"])

(defcomponent Message
  [props]
  [:p (str (:subject props) " " (:to props))])

(defcomponent App
  [props]
  :let [hello "you"]
  [:div {:onClick (partial println "hello from react")}
   (str "wesh " hello)
   [Link]
   [Message {:to "world"
             :subject (:hello props)}]])

(react-dom/render
  (jsx [App {:hello "hello"}])
  root)
