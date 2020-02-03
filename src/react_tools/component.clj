(ns react-tools.component
  (:require [clojure.spec.alpha :as s]))

(s/def ::jsx
  (s/or
    :dom-element
    (s/and vector?
           (s/cat :element keyword?
                  :props (s/? (s/map-of keyword? any?))
                  :children (s/* ::jsx)))
    :react-component
    (s/and vector?
           (s/cat :element symbol?
                  :props (s/? (s/map-of keyword? any?))
                  :children (s/* ::jsx)))
    :expression any?))

(s/def ::bindings
   (s/+ (s/cat :identifier any?
               :value any?)))

(s/def ::component
  (s/cat 
    :name symbol?
    :prop-binding (s/? (s/and vector?
                              (s/cat :binding symbol?)))
    :devtool (s/? (s/cat :keyword #{:devtool} :enabled boolean?))
    :react-bindings
    (s/* (s/cat :keyword #{:let :state}
                :bindings (s/spec ::bindings)))
    :jsx (s/spec ::jsx)))

(defn render-props
  [props]
  (let [props' `(doto (js/Object.)
                  ~@(map
                      (fn [[prop-key prop-val]]
                        `(react-tools.component/object-set
                           ~(str (name prop-key))
                           ~(if (= :style prop-key)
                              `(react-tools.component/->js ~prop-val)
                              prop-val)))
                      props))]
    props'))

(defmulti render-jsx key :default :dom-element)

(defmethod render-jsx :expression
  [[_ expression]]
  expression)

(defmethod render-jsx :dom-element
  [[_ element]]
  `(react/createElement ~(name (:element element))
                        ~(render-props (:props element))
                        ~@(map render-jsx (:children element))))

(defmethod render-jsx :react-component
  [[_ react-component]]
  `(react/createElement ~(:element react-component)
                        ~(render-props (:props react-component))
                        ~@(map render-jsx (:children react-component))))

(defn jsx-impl
  [jsx-vector]
  (if (vector? jsx-vector)
    (let [jsx-spec (s/conform ::jsx jsx-vector)]
      (render-jsx jsx-spec))))

(defmacro jsx
  [jsx-vector]
  (jsx-impl jsx-vector))

(defmulti render-bindings :keyword)

(defmethod render-bindings :let
  [{:keys [bindings]}]
  (apply concat
         (map
           (fn [{:keys [identifier value]}]
             [identifier value])
           bindings)))

(defmethod render-bindings :state
  [{:keys [bindings]}]
  (apply concat
         (map
           (fn [{:keys [identifier value]}]
             `[[~identifier ~(symbol (str "set-" identifier))] (react/useState ~value)])
           bindings)))

(defn defcomponent-impl
  [& spec]
  (let [component-spec (s/conform ::component spec)]
    (if (= ::s/invalid component-spec)
      (throw (ex-info "defcomponent spec violation" (s/explain-data ::component spec)))
      `(do 
           (defn ~(:name component-spec)
             [props#]
             (let [~(or (:binding (:prop-binding component-spec)) (gensym)) (react-tools.component/bean props#)]
               (let ~(let [bindings (vec (apply concat (map render-bindings (:react-bindings component-spec))))]
                       bindings)
                 ~(let [rendered-jsx (render-jsx (:jsx component-spec))]
                    (if (-> component-spec :devtool :enabled true?)
                      (jsx-impl
                        `[react/Fragment
                          [react-tools.devtool/DevToolPortal
                           [react-tools.devtool/DevTool {:component (quote ~component-spec)}]]
                          ~rendered-jsx])
                      rendered-jsx)))))))))

(defmacro defcomponent
  [& spec]
  (apply defcomponent-impl spec))


(comment
  (s/conform (s/and vector? ::jsx) [:div (:to "you")])
  (s/conform ::jsx '[:div (:to "you")])
  (let [component (quote
                    (App
                      [props]
                      [:div {:onClick (partial println "hello from react")}
                       "wesh world"
                       (:to props)
                       [Link]
                       [Mesage (:to "you")]]))]
                       
     (pprint (apply defcomponent-impl component))))

(comment
  (s/conform ::bindings '[a 1])
  (s/conform ::bindings '[a 1])
  (str :lol)
  (pprint
    (apply defcomponent-impl
      '(TicTac
         :let [board (for [x (range 3)
                           y (range 3)]
                       [x y])]
         :state [yolo yola]

         [:div {:style {:css "world"}}])))

  (pprint
    (s/conform  ::component
      '(TicTac
         :let [board (for [x (range 3)
                           y (range 3)]
                       [x y])]

         [:div]))))
(comment
  (name :component-spec)
  (repeat 3 1)
  (range 3))
