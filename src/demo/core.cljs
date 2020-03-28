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

(defn Paragraph
  [props]
  (let [props' (bean props)]
    (react/createElement ui/Text (-> props'
                                     (dissoc :children)
                                     (assoc :marginTop "1rem"
                                            :marginBottom "1rem")
                                     ->js)
                         (:children props'))))

(defcomponent Home
  [ui/Box {:maxW "800px"}
   [ui/Heading {:as "h1"} "Why another library ?"]
   [Paragraph "React is an awesome UI library which provides
              a nice abstraction and an amazing ecosystem of tools,
              libraries and best practices for building interfaces."]
   [Paragraph "On the other hand, Clojure / ClojureScript provide a
              language with powerful and flexible semantics that can
              make you really productive, using both an immutable by
              default programming paradigm combined with a REPL based
              developping exeperience."]
   [Paragraph "For a while, React encouraged the use of Javascript constructors
              to declare a React Component, however since the release of
              the hooks API React now favours the use of JS functions to declare
              components. Those are much both easier to write and understand and
              make a fine compilation target. One should note howeveer that
              the legacy syntax using constructors has not been deprecated
              and still has its use cases. React merely extended the semantics
              one can use to declare a Component."]
   [ui/Text {:fontSize "2xl"} "Isn't there already well known tools / libraries that exists for working with Reat ?"]
   [Paragraph]])

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
      [ui/Flex
       {:justifyContent "center"
        :flex "auto"}
       [ui/Box
        [Switch
         [Route {:path "/" :exact true}
          [Home]]
         [Route {:path "/tictac" :exact true}
          [TicTac]]
         [Route {:path "/hackernews"}
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
          [LeafletContainer]]]]]]]]])


(react-dom/render
  (jsx [App])
  root)
