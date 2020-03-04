(ns react-tools.component
  (:require [clojure.spec.alpha :as s]))

(s/def ::bindings
   (s/+ (s/cat :identifier any?
               :value any?)))

(s/def ::jsx
  (s/or
    :map-expression
    (s/and vector?
           (s/cat :element #{:react/map}
                  :bindings (s/spec ::bindings)
                  :jsx ::jsx))

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


(s/def ::component
  (s/cat 
    :name symbol?
    :prop-binding (s/? (s/and vector?
                              (s/cat :binding symbol?)))
    :devtool (s/? (s/cat :keyword #{:devtool} :enabled boolean?))
    :react-bindings
    (s/* (s/cat :keyword keyword?
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

(defmethod render-jsx :map-expression
  [[_ element]]
  (let [{:keys [bindings jsx let-bindings]} element
        identifiers (map :identifier bindings)
        colls (map :value bindings)]
    `(apply cljs.core/array
            (map (fn ~(vec identifiers)
                   ~(if (:let element)
                      `(let ~(vec let-bindings)
                         ~(render-jsx jsx))
                      (render-jsx jsx)))
                 ~@colls))))

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

(defmulti render-bindings :keyword :default :let)

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
  (let [component-spec (s/conform ::component spec)
        ComponentName (:name component-spec)]
    (if (= ::s/invalid component-spec)
      (throw (ex-info "defcomponent spec violation" (s/explain-data ::component spec)))
      `(do
         (defn ~ComponentName
           [props#]
           (let [~(or (:binding (:prop-binding component-spec)) (gensym)) (react-tools.component/bean props#)]
             (let ~(let [bindings (vec (apply concat (map render-bindings (:react-bindings component-spec))))]
                     bindings)
               ~(let [rendered-jsx (render-jsx (:jsx component-spec))]
                  rendered-jsx))))))))

(defmacro defcomponent
  [& spec]
  (apply defcomponent-impl spec))
