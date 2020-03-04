(ns react-tools.devtool
  (:require [react]
            [react-dom]
            [goog.object :as gobject]
            [react-tools.component])

  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defcomponent DevTool
  [props]
  :let [component (:component props)]
  :state [spec-opened? false]
  [:section {:style {:margin "1rem"
                     :padding "1rem"
                     :fontFamily "system-ui"}}
   [:h1 (str "Component " (-> component :name))]])
