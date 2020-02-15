(ns demo.hackernews
  (:require [react]
            [react-tools.component]
            [cljs-bean.core :refer [->clj]]
            [react-tools.devtool])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def top-stories "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty")

(defn get-top-stories
  []
  (-> (js/fetch top-stories)
      (.then #(.json %))))

(defn get-item
  [item-id]
  (let [url (str "https://hacker-news.firebaseio.com/v0/item/" item-id ".json")]
    (-> url
        js/fetch
        (.then #(.json %)))))

(defcomponent HackerNews
  :devtool true
  :state [top-stories nil]
  :let [top-stories-effect (react/useEffect (fn []
                                              (-> (get-top-stories)
                                                  (.then ->clj)
                                                  (.then set-top-stories))
                                              js/undefined)
                                            (array))]
  :state [top-story nil]
  :let [top-story-effect (react/useEffect (fn []
                                            (when top-stories
                                              (-> (get-item (first top-stories))
                                                  (.then ->clj)
                                                  (.then set-top-story)))
                                            js/undefined)
                                          (array top-stories))]

  [:div {:style {:color "red"}} "hackernews"
   (if top-stories
     (jsx [:div (str (count top-stories) " Trending Stories")])
     (jsx [:div {} "Loading stories"]))
   (if top-story
     (jsx [:div 
           [:h1 "Top story"]
           [:h2 (:title top-story)]]))])
