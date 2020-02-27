(ns demo.counter
  (:require [react]
            [react-dom]
            [react-tools.component]
            ["@chakra-ui/core" :as ui])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defcomponent Counter
  :let [initial-count 0]
  :state [total initial-count]
  [ui/Box
   [:h2 "Counter"]
   [:div "Count " total
    [:p [:button {:onClick #(set-total inc)} "Increase the counter"]]]])
