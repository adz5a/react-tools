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
            [demo.ui :refer [Link NavItem theme]]
            ["@chakra-ui/core" :as ui])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def root (.getElementById js/document "root"))

(defcomponent App
  [ui/ThemeProvider {:theme theme}
   [ui/CSSReset]
   [BrowserRouter
    [ui/Box {:fontFamily "body"}
     [ui/Box
       [ui/Heading {:paddingLeft "1rem"} [Link {:to "/" :color "gray.800"} "react-tools"]]
       [ui/Divider]]
     [ui/Flex {:direction "row"}
      [ui/Box
       {:w "15rem"}
       [:h2 {:style {:paddingLeft "1rem"}} "Examples"]
       [ui/List {:styleType "none"
                 :background "1rem"
                 :paddingLeft "0.5rem"
                 :spacing 2}
        [NavItem {:to "/tictac"} "Tic Tac"]
        [NavItem {:to "/hackernews"} "HackerNews"]
        [NavItem {:to "/counter"} "Counter"]
        [NavItem {:to "/devtools"} "Dev Tools"]
        [NavItem {:to "/developpement-personnel"} "Developpement personnel"]
        [NavItem {:to "/leaflet"} "Leaflet"]]]
      [ui/Box
       {:flex "auto"
        :marginLeft "2rem"}
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
