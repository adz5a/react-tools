(ns react-tools.component
  (:require [clojure.spec.alpha :as s]))

(s/def ::jsx (s/or :dom-element (s/cat :element keyword?
                                       :props (s/? (s/map-of keyword? any?))
                                       :children (s/* ::jsx))
                   :react-component (s/cat :element symbol?
                                           :props (s/? (s/map-of keyword? any?))
                                           :children (s/* ::jsx))
                   :expression any?))

(s/def ::component
  (s/cat 
    :name symbol?
    :jsx (s/spec ::jsx)))

(defn render-props
  [props]
  (let [props-sym (gensym "props")]
    `(let [~props-sym (js/Object.)]
       ~@(map
              (fn [[prop-key prop-val]]
                `(set! (. ~props-sym ~(symbol (str "-" (name prop-key)))) ~prop-val))
              props)
       ~props-sym)))

(defmulti render-jsx key :default :dom-element)

(defmethod render-jsx :expression
  [[_ expression]]
  expression)

(defmethod render-jsx :dom-element
  [[_ element]]
  `(react/createElement (name ~(:element element))
                        ~(render-props (:props element))
                        ~@(map render-jsx (:children element))))

(defmethod render-jsx :react-component
  [[_ react-component]]
  `(react/createElement ~(:element react-component)
                        ~(render-props (:props react-component))
                        ~@(map render-jsx (:children react-component))))

(defn defcomponent-impl
  [& spec]
  (let [component-spec (s/conform ::component spec)]
    (if (= ::s/invalid component-spec)
      `(println (quote ~(s/explain-str ::component spec)))
      `(do (println (quote ~component-spec))
           (defn ~(:name component-spec)
             [props#]
             (println props#)
             ~(render-jsx (:jsx component-spec)))))))

(defmacro defcomponent
  [& spec]
  (apply defcomponent-impl spec))

(defmacro jsx
  [jsx-vector]
  (if (vector? jsx-vector)
    (let [jsx-spec (s/conform ::jsx jsx-vector)]
      (render-jsx jsx-spec))))
