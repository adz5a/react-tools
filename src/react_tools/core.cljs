(ns react-tools.core
  (:require [react]
            [react-dom]
            [cljs-bean.core]
            [react-router-dom :refer [BrowserRouter Route Link Switch]]

            [demo.hackernews :refer [HackerNews]]
            [demo.tictac :refer [TicTac]])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def root (.getElementById js/document "root"))

(defonce state (atom nil))

(defcomponent DevTool
  [props]
  :let [{:keys [state]} props]
  [:div
   (map
     (fn [state-sym state-val]
       (jsx [:div
             [:h4 (str state-sym)]
             [:p (str state-val)]]))
     state)])

(defcomponent App
  [BrowserRouter
   [:div
    [:ul
     [:li [Link {:to "/home"} "Home"]]
     [:li [Link {:to "/tictac"} "Tic Tac"]]
     [:li [Link {:to "/hackernews"} "HackerNews"]]]]
   [Switch
    [Route {:path "/home" :exact true}
     [:div "Home"]]
    [Route {:path "/tictac" :exact true}
     [TicTac]]
    [Route {:path "/hackernews" :exact true}
     [HackerNews]]]])


(react-dom/render
  (jsx [App])
  root)
