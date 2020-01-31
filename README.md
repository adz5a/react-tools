# REACT-TOOLS

## Minimal React interop

- React has a thriving ecosystem
    - Design components: rebass, bootstrap, semantic-ui etc all have packages for react
    - Structure components: react-router-dom
- Shadow-cljs solves most of the problems interacting with npm [0]


## Examples

```clojurescript
;; Require the react and react-dom packages. Those are vanilla NPM packages, not CLJS(JS) package.
;; react-tools.component exports vars used by the `defcomponent` and `jsx` macros.
(ns my-project.core
    (:require [react-tools.component]
              [react]
              [react-dom]))
    (:require-macros [react-tools.component :refer [defcomponent jsx]]))
```


```clojurescript
;; Simplest component which renders a div with the text"hello world" inside it.
(defcomponent Hello
    [:div "hello world"])
```


```clojurescript
;; Components which reads the recipient of the greeting and prints it
;; the [props] binding is optionnal
(defcomponent Hello
    [props]
    :let [to (:to props)]
    [:div (str "hello " to])
```


```clojurescript
;; Components which reads the recipient of the greeting and prints it
(defcomponent Hello
    [props]
    :let [to (:to props)]
    :state [greetings-recipient ""]
    [:div
        [:p (str "Send a greeting to " [:input {:value greetings-recipient :onChange #(-> e .-target .-value set-greetings-recipient)}])]
        [:p (str "hello " greetings-recipient)]])
```


```clojurescript
;; defcomponent creates a simple JS function. You can render it using react-dom
;; as usual. `jsx` is a macro that allows you to convert the hiccup-like symtax
;; into `react/createElement` calls outside the body of a `defcomponent` declaration.
(react-dom/render
    (jsx [Hello {:to "you"}])
    (.getElementById js/document "root"))
```


## Component as a spec


### :let
### :state

## Atomic state

Up until hooks were introduced a React component could have zero or one _state atom_: function components could
have none and class components automatically had one through the `this.state` / `this.setState` API. With hooks,
a function component could have a arbitrary number of state atoms, each declared using the `useState` hook.


- [0] https://code.thheller.com/blog/shadow-cljs/2018/06/15/why-not-webpack.html
- [1] https://kentcdodds.com/blog/dont-sync-state-derive-it

