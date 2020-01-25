(ns demo.hackernews
  (:require [react]
            [cljs-bean.core])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defcomponent HackerNews
  [:div "hackernews"])
