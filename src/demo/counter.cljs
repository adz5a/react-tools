(ns demo.counter
  (:require [react]
            [react-dom]
            [react-tools.component])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defcomponent Counter
  :let [initial-count 0]
  :state [total initial-count]
  [:div "Count " total
   [:p [:button {:onClick #(set-total inc)} "Increase the counter"]]])
