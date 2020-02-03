(ns react-tools.component
  (:require [goog.object :as gobject]
            [cljs-bean.core :as cljs-bean]))

(def object-set gobject/set)

(def ->js cljs-bean/->js)

(def bean cljs-bean/bean)
