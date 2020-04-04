# REACT-TOOLS

## Rationale

### Minimal React interop

- React has a thriving ecosystem
    - Design components: rebass, bootstrap, semantic-ui etc all have packages for react.
    - Structure components: react-router-dom, reach-router.
    - Hooks: a lot of functionalities are developped around hooks in JavaScript or TypeScript,
    those we should be able to consume those in ClojureScript without friction and introducing
    new concepts.
    - But we also want to be able to use the power of the Clojure, using protocols, multimethods
    and macros to share semantics and improve the quality of our code.
- Shadow-cljs solves most of the problems interacting with npm [0].


## What is a React component ?

In JavaScript a React component can be declared using a function declaration: `function MyComponent ...`
and its body is just vanilla Javascript. However writing a valid (and thus useful) component comes
with some constraints:

-  the function takes one or two argument, the first is an associative data structure, the second is a ref
if present.
- it must return valid React Elements for it to be renderable.
- calling a React component outside a render pass will lead to unexpected results if React hooks are
used in the component

In short, a React component declared as a function is actually **not really a function** and cannot really
be used as one in your application. However because it is such a "simple construct" it can be a compilation
target for ClojureScript macros.

This is what this library is about: it provides somes macros to make the task of writing such things a
little bit easier, without getting in your way when you want to consume the regular JS ecosystem.


## Examples

### Import the library

```clojurescript
;; Require the react and react-dom packages. Those are the vanilla NPM packages, not CLJS(JS) package.
;; react-tools.component exports vars used by the `defcomponent` and `jsx` macros. Those macros will
;; emit code that compiles to regular function declaration, and react/createElement calls. react-dom
;; can be used to render those components
(ns my-project.core
    (:require [react-tools.component :refer [defcomponent jsx]]
              [react]
              [react-dom]))
```


### Write some components

```clojurescript
;; Simplest component which renders a div with the text"hello world" inside it.
(defcomponent Hello
    [:div "hello world"])
```

A component is specified as a list of sequential bindings evaluated one at a time.

```clojurescript
;; Components which reads the recipient of the greeting and prints it
;; the [props] binding is optionnal
(defcomponent Hello
    [props]
    :let [to (:to props)]
    [:div (str "hello " to])
```

In the above component the `props` are wrapped in a `cljs-bean` which allow for associative
access to its fields using regular keywords. The `:let` keyword is used to declare the start
of a binding sequence, it will compile to a regular `(let [])` form. However any keyword can
be used to label those bindings, allowing for greater expressiveness in your component declarations.


```clojurescript
;; Components which reads the recipient of the greeting and prints it
(defcomponent Greeting
    [props]
    :let [to (:to props)]
    :state [[recipient set-recipient] (react/useState nil)] ;; native react hook
    [:div
        [:p (str "Send a greeting to ")
            [:input {:value greetings-recipient
                     :onChange #(-> e .-target .-value set-recipient)}])]
        [:p (str "hello " recipient)]])
```


Render the component using react-dom.

```clojurescript
;; defcomponent creates a simple JS function. You can render it using react-dom
;; as usual. `jsx` is a macro that allows you to convert the hiccup-like symtax
;; into `react/createElement` calls outside the body of a `defcomponent` declaration.
(react-dom/render
    (jsx [Hello {:to "you"}])
    (.getElementById js/document "root"))
```
