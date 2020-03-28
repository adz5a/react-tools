(ns react-tools.component
  (:require [clojure.spec.alpha :as s]))

(s/def ::bindings
   (s/+ (s/cat :identifier any?
               :value any?)))

(s/def ::jsx
  (s/or
    :if-expression
    (s/and vector?
           (s/cat :element #{:react/if}
                  :assertion any?
                  :true? ::jsx
                  :else? (s/? ::jsx)))
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

(s/def :devtool/id any?)

(s/def ::devtool-component
       (s/or :boolean-flag boolean?
             :options (s/keys :req-un [:devtool/id])))

(s/def ::component
       (s/cat 
         :name symbol?
         :prop-binding (s/? (s/and vector?
                                   (s/cat :binding symbol?)))
         :devtool (s/? (s/cat :keyword #{:devtool} :options ::devtool-component))
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

(defmethod render-jsx :if-expression
  [[_ element]]
  (let [{:keys [assertion]} element]
    `(if ~assertion
       ~(render-jsx (:true? element))
       ~(when (:else? element)
          (render-jsx (:else? element))))))

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

(defmulti render-bindings
  (fn [react-bindings & _]
    (react-bindings :keyword))
  :default :let)

(defmethod render-bindings :let
  ([{:keys [bindings]}]
   (apply concat
          (map
            (fn [{:keys [identifier value]}]
              [identifier value])
            bindings)))
  ([{:keys [bindings]} devtool-atom-identifier bloc-index]
   (apply concat
          (map
            (fn [{:keys [identifier value]} index]
              (let [devtool-identifier (gensym (str identifier))
                    component-value value
                    component-id-identifier (gensym)]
                `[~devtool-identifier ~component-value
                  ~identifier (do
                                (let [component-id# (-> ~devtool-atom-identifier deref :rendering)]
                                  (swap! ~devtool-atom-identifier assoc-in [:components component-id# :bindings ~bloc-index ~index] ~devtool-identifier)
                                  (swap! ~devtool-atom-identifier assoc-in [:components component-id# :vars (quote ~identifier)] ~devtool-identifier))
                                ~devtool-identifier)]))
            bindings
            (range)))))

(defmethod render-bindings :state
  ([{:keys [bindings]}]
   (apply concat
          (map
            (fn [{:keys [identifier value]}]
              (let [computed-value `(react/useState ~value)
                    computed-identifier [identifier (symbol (str "set-" identifier))]] 
                `[~computed-identifier ~computed-value]))
            bindings)))
  ([{:keys [bindings]} devtool-atom-identifier bloc-index]
   (apply concat
          (map
            (fn [{:keys [identifier value]} binding-index]
              (let [computed-value `(react/useState ~value)
                    devtool-identifier (gensym (str identifier))
                    computed-identifier [identifier (symbol (str "set-" identifier))]] 
                `[~devtool-identifier ~computed-value
                  ~computed-identifier ~devtool-identifier]))
            bindings
            (range)))))

(defn defcomponent-impl
  [& spec]
  (let [component-spec (s/conform ::component spec)
        ComponentName (:name component-spec)
        devtool-enabled? (:devtool component-spec)
        devtools (atom {:spec component-spec})
        devtool-atom-identifier (symbol (str ComponentName "-devtool-atom"))
        devtool-id-identifier (symbol (str "id-ref"))]
    (if (= ::s/invalid component-spec)
      (throw (ex-info "defcomponent spec violation" (s/explain-data ::component spec)))
      `(do
         ~(if devtool-enabled?
            `(defonce ~devtool-atom-identifier (atom {})))
         (defn
           ~ComponentName
           "React Component."
           [props#]
           (let [~(or (:binding (:prop-binding component-spec)) (gensym)) (react-tools.component/bean props#)
                 ~devtool-id-identifier ~(when devtool-enabled?
                                           `(let [id-ref# (react/useRef ~(let [devtool (-> component-spec :devtool :options)]
                                                                           (case (key devtool)
                                                                             :boolean-flag :default
                                                                             :options (-> devtool val :id))))]
                                              (swap! ~devtool-atom-identifier assoc :rendering (.-current id-ref#))
                                              id-ref#))]
             (let ~(let [bindings (vec (apply concat (map (fn [bindings index] 
                                                            (if devtool-enabled?
                                                              (render-bindings bindings devtool-atom-identifier index)
                                                              (render-bindings bindings)))
                                                          (:react-bindings component-spec)
                                                          (range))))]
                     bindings)
               ~(let [rendered-jsx (render-jsx (:jsx component-spec))]
                  rendered-jsx))))
         ~(if devtool-enabled?
            `(set! (.-devtool ~ComponentName) (react-tools.devtool/DevTool. ~ComponentName
                                                                            (quote (defcomponent ~ComponentName ~@spec))
                                                                            (quote ~component-spec)
                                                                            ~devtool-atom-identifier)))
         ~ComponentName))))

(defmacro defcomponent
  [& spec]
  (apply defcomponent-impl spec))


(comment

  (pprint *e)
  (let [component-declaration '(Hello
                                 :devtool true
                                 :state [^{:name "wesh"}yolo "swag"]
                                 [:div "world"])]
    (pprint (s/conform ::component component-declaration))
    (pprint (macroexpand (conj component-declaration 'defcomponent)))))
