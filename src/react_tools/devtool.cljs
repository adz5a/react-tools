(ns react-tools.devtool
  (:require [react]
            [react-dom]
            [cljs-bean.core])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))


(defn DevToolPortal
  [props]
  (let [dom-element (react/useRef (.createElement js/document "div"))
        insert-dom-element-effect (react/useEffect (fn []
                                                     (.appendChild (.-body js/document) (.-current dom-element))
                                                     js/undefined)
                                                   (array))]
    (react-dom/createPortal
      (.-children props)
      (.-current dom-element))))

(defcomponent DevTool
  [props]
  :let [_ (println "Mounted")
        _ (println (:component props))]
  [:div (str "dev tool for: " (-> props :component :name))])
