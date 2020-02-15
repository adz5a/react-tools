(ns demo.ui
  (:require [react]
            [react-dom]
            [cljs-bean.core :refer [bean ->js]]
            [react-tools.component]
            [react-router-dom :as react-router]
            ["@chakra-ui/core" :as ui])
  (:require-macros [react-tools.component :refer [defcomponent jsx]])) 

(def font-family "'Inconsolata', monospace")

(def theme (-> (bean ui/theme :recursive true)
               (assoc-in [:fonts :body] font-family)
               (assoc-in [:fonts :heading] font-family)
               ->js))

(defn Link
  [props]
  (let [props (bean props)
        children (:children props)]
    (react/createElement ui/Link
                         (-> props
                             (dissoc :children)
                             (assoc :as react-router/Link)
                             ->js)
                         children)))

(defcomponent NavItem
  [props]
  :let [{:keys [to children]} props
        match (-> to react-router/useRouteMatch)]
  [ui/ListItem 
   [ui/ListIcon {:icon (if match "minus" "chevron-right")}]
   [Link {:to to :color "gray.800"}
    children]])
