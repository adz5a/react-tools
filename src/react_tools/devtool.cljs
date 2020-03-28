(ns react-tools.devtool
  (:require [react]
            [react-dom]
            [goog.object :as gobject]
            [react-tools.component])

  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defprotocol IDevtool
  :extend-via-metadata true
  (source-code [this])
  (spec [this])
  (get-binding [this binding-identifier])
  (instances [this])
  (reset-instances! [this])
  (get-var [this identifier]
           [this instance identifier]))

(defrecord DevTool [Component source-code component-spec instances*]
  IDevtool
  (source-code [this]
    source-code)
  (spec [this]
    spec)
  (instances [this]
    @instances*)
  (reset-instances! [this]
    (reset! instances* {}))
  (get-var [this target-identifier]
    (get-var this :default target-identifier))
  (get-var [this instance target-identifier]
    (let [component-instance (-> this instances :components instance)]
      (when component-instance
        (-> component-instance :vars target-identifier)))))

(defn devtool
  [Component]
  (.-devtool Component))
