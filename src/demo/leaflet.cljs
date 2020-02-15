(ns demo.leaflet
  (:require [react])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defn render-leaflet
  [dom-container lat-lng zoom]
  (let [[lat lng] lat-lng
        leaflet-map (.map js/L dom-container)
        _create-view (.setView leaflet-map
                               (array lat lng)
                               zoom)
        _ (-> js/L
              (.tileLayer
                "https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpejY4NXVycTA2emYycXBndHRqcmZ3N3gifQ.rJcFIG214AriISLbB6B5aw"
                #js {:maxZoom 18
                     :attribution "yolo"
                     :id "mapbox/streets-v11"})
              (.addTo leaflet-map))]
    leaflet-map))

(defcomponent Marker
  [react/Fragment])

(defcomponent Leaflet
  [props]
  :let [{:keys [lat lng zoom]} props
        dom-container-ref (react/useRef nil)
        map-ref (react/useRef nil)
        on-mount-effect (react/useEffect (fn []
                                           (set! (.-current map-ref)
                                                 (render-leaflet
                                                   (.-current dom-container-ref)
                                                   [51.505 -0.9] 13))
                                           js/undefined)
                                         (array))]

  :let [update-view-effect (react/useEffect (fn []
                                              (.setView (.-current map-ref)
                                                        (array lat lng)
                                                        zoom)
                                              js/undefined)
                                            (array lat lng zoom))]
  :let [rerender-effect (react/useEffect
                          (fn []
                            (println "rerender")
                            (.log js/console map-ref)
                            js/undefined))]
  [:div {:style {:border "solid 1px"
                 :width "500px"
                 :height "500px"}
         :ref dom-container-ref}
   [react/Fragment (:children props)]])

(defcomponent LeafletContainer
  :state [lat 51.505
          lng -0.9
          zoom 13]
  [:section
   [:h1 "Leaflet"]
   [:form
    [:p [:label "Latitude" [:input {:value lat
                                    :onChange #(-> % .-target .-value set-lat)}]]]
    [:p [:label "Longitude" [:input {:value lng
                                     :onChange #(-> % .-target .-value set-lng)}]]]
    [:p [:label "Zoom" [:input {:value zoom
                                :type "number"
                                :onChange #(-> % .-target .-value set-zoom)}]]]]
   [Leaflet {:lat lat
             :lng lng
             :zoom zoom}
    [Marker]]])
