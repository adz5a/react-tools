(ns demo.hackernews
  (:require [react]
            [react-tools.component]
            [cljs-bean.core :refer [->clj]]
            [react-tools.devtool :refer [devtool get-var instances]]
            [react-router-dom :refer [Link Route useRouteMatch]]
            ["@chakra-ui/core" :as ui]
            [react-tools.hooks :as hooks :refer [set-state!]])
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

(defcomponent HackerNewsStory
  [props]
  :devtool {:id (:story-id props)}
  :let [story-id (:story-id props)
        url (:url props)
        story (hooks/use-promise->state
                (fn []
                  (-> (get-item story-id)
                      (.then ->clj)))
                nil
                [story-id])]

  [ui/Box
   (if (:result @story)
     (jsx [ui/Box
           [ui/Heading {:as "h4"
                        :fontSize "s"}
            (if-let [story-url (-> @story :result :url)]
              (jsx [Link {:to story-url
                          :style {:marginRight "1rem"}}
                    [ui/Badge {:variantColor "purple"} "URl"]]))
            [Link
             {:to url}
             (-> @story :result :title)
             (str " (" (-> @story :result :score) ")")]]]))])

(defcomponent HackerNewsStoryBody
  [props]
  [:div "hello world"])

(defcomponent HackerNews
  [props]
  :devtool true
  :let [top-stories (hooks/use-promise->state
                      (fn []
                        (-> (get-top-stories)
                            (.then ->clj)))
                      nil
                      [])
        page (hooks/use-state 0)
        url (.-url (useRouteMatch))]
  [ui/Box
   [ui/Heading "hackernews feed"]
   (if (:pending @top-stories)
     (jsx [ui/CircularProgress {:isIndeterminate true :color "blue"}]))
   (if (:result @top-stories)
     (jsx [ui/Box
           (map
             (fn [story-id]
               (jsx [HackerNewsStory {:key story-id
                                      :story-id story-id
                                      :url (str url "/" story-id)}]))
             (take 10 (:result @top-stories)))]))
   [Route {:path "hackernews/story/:story-id"}
    [HackerNewsStoryBody]]])

(comment
  (println HackerNews-devtool-atom)
  (do (alter-meta! HackerNews assoc :wesh {:hello "world"})
     (meta #'HackerNews))
  (meta  #'HackerNews)
  (-> HackerNewsStory devtool instances)
  (-> HackerNews devtool d/reset-instances!)
  (-> HackerNews devtool (get-var 'top-stories) deref)
  (let [devtool (d/devtool Ha)])
  (hooks/set-state! (-> HackerNews devtool (d/get-var 'top-stories)) {:result []})
  (hooks/set-state! (first (d/get-var (d/devtool HackerNews) 'top-stories))
                    {:result []}))
