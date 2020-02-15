(ns demo.core
  (:require [react]
            [react-dom]
            [cljs-bean.core :refer [bean ->js]]
            [react-router-dom :as react-router :refer [BrowserRouter Route Switch]]
            [react-tools.component]

            [demo.hackernews :refer [HackerNews]]
            [demo.tictac :refer [TicTac]]
            [demo.counter :refer [Counter]]
            [react-tools.devtool :refer [DevTool]]
            [demo.dev :refer [Plan]]
            [demo.leaflet :refer [LeafletContainer]]
            ["@chakra-ui/core" :as ui :refer [ThemeProvider theme Box Heading Divider Text]])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))



(def root (.getElementById js/document "root"))

(defonce state (atom nil))

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

(defcomponent App
  [ThemeProvider {:theme theme}
   [BrowserRouter
    [Box {:color "rgb(26, 32, 44)"}
     [Heading [Text "react-tools"]]
     [Divider]
     [ui/Flex {:direction "row"}
      [ui/Flex
       {:w "15rem"}
       [ui/List
        [ui/ListItem [Link {:to "/home"} "Home"]]
        [ui/ListItem [Link {:to "/tictac"} "Tic Tac"]]
        [ui/ListItem [Link {:to "/hackernews"} "HackerNews"]]
        [ui/ListItem [Link {:to "/counter"} "Counter"]]
        [ui/ListItem [Link {:to "/devtools"} "Dev Tools"]]
        [ui/ListItem [Link {:to "/developpement-personnel"} "Developpement personnel"]]
        [ui/ListItem [Link {:to "/leaflet"} "Leaflet"]]]]
      [ui/Box
       {:flex "auto"}
       [Switch
        [Route {:path "/home" :exact true}
         [:div "Home"]]
        [Route {:path "/tictac" :exact true}
         [TicTac]]
        [Route {:path "/hackernews" :exact true}
         [HackerNews]]
        [Route {:path "/counter" :exact true}
         [Counter]]
        [Route {:path "/developpement-personnel" :exact true}
         [Plan]]
        [Route {:path "/devtools" :exact true}
         [:section
          [:h1 "Dev Tools"]
          [DevTool]]]
        [Route {:path "/leaflet" :exact true}
         [LeafletContainer]]]]]]]])


(react-dom/render
  (jsx [App])
  root)
