(ns react-tools.devtool
  (:require [react]
            [react-dom]
            [goog.object :as gobject])

  (:require-macros [react-tools.component :refer [defcomponent jsx]]))


(defn DevToolPortal
  [props]
  (let [dom-element (react/useRef (.createElement js/document "div"))
        insert-dom-element-effect (react/useEffect (fn []
                                                     (let [dev-tool-container (.getElementById js/document "react-tools-dev-tool-container")
                                                           has-container? (not (nil? dev-tool-container))]
                                                       (if has-container?
                                                         (.appendChild dev-tool-container (.-current dom-element))
                                                         (let [dev-tool-container (.createElement js/document "div")]
                                                           (.appendChild (.-body js/document) dev-tool-container)
                                                           (gobject/set dev-tool-container "id" "react-tools-dev-tool-container")
                                                           (.setAttribute dev-tool-container "style" "position: fixed; top: 0; background: white; left: 0; display: inline-block;")
                                                           (.appendChild dev-tool-container (.-current dom-element)))))
                                                     js/undefined)
                                                   (array))]
    (react-dom/createPortal
      (.-children props)
      (.-current dom-element))))

(defcomponent DevTool
  [props]
  :let [component (:component props)]
  :state [spec-opened? false]
  [:section {:style {:margin "1rem"
                     :padding "1rem"
                     :fontFamily "system-ui"}}
   [:h1 (str "Component " (-> component :name))]])
