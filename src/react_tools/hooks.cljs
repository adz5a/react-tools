(ns react-tools.hooks
  (:require [react :refer [useState useEffect]]))
  

(defprotocol IState
  (set-state! [this value-or-fn]
              [this value-or-fn a]
              [this value-or-fn a b]
              [this value-or-fn a b c]
              [this value-or-fn a b c d]
              [this value-or-fn a b c d e]
              [this value-or-fn a b c d e f]
              [this value-or-fn a b c d e f g]
              [this value-or-fn a b c d e f g h]
              "Update the state asynchronously."))

(defprotocol IAsyncConsumer
  (consume-promise [this promise-producer-fn]
                   "Runs promise-producer-fn in an effect, putting the result of the promise into a new state.
                   The new state is a map that contains :error and :result"))

(deftype State
  [state set-state]
  IState
  (set-state! [this value-or-fn]
    (set-state value-or-fn))
  (set-state! [this value-or-fn a]
    (set-state #(value-or-fn % a)))
  (set-state! [this value-or-fn a b]
    (set-state #(value-or-fn % a b)))
  (set-state! [this value-or-fn a b c]
    (set-state #(value-or-fn % a b c)))
  (set-state! [this value-or-fn a b c d]
    (set-state #(value-or-fn % a b c d)))
  (set-state! [this value-or-fn a b c d e]
    (set-state #(value-or-fn % a b c d e)))
  (set-state! [this value-or-fn a b c d e f]
    (set-state #(value-or-fn % a b c d e f)))
  (set-state! [this value-or-fn a b c d e f g]
    (set-state #(value-or-fn % a b c d e f)))
  (set-state! [this value-or-fn a b c d e f g h]
    (set-state #(value-or-fn % a b c d e f g h)))
  IDeref
  (-deref [this]
    (.-state this)))

(defn use-state
  ([]
   (use-state nil))
  ([initial-state]
   (let [state-hook (useState initial-state)]
     (State. (aget state-hook 0) (aget state-hook 1)))))

(defn use-promise->state
  ([promise-producer-fn]
   (use-promise->state promise-producer-fn nil js/undefined))
  ([promise-producer-fn initial-state]
   (use-promise->state promise-producer-fn initial-state js/undefined))
  ([promise-producer-fn initial-state deps]
   (let [result-state (use-state {:error nil :result nil :pending true})]
     (useEffect (fn []
                  (let [guard #js {:unmounted false}]
                    (-> (promise-producer-fn)
                        (.then (fn [result]
                                 (if (-> guard .-unmounted not)
                                   (set-state! result-state {:result result
                                                             :error nil
                                                             :pending false})))
                               (fn [error]
                                 (if (-> guard .-unmounted not)
                                   (set-state! result-state {:result nil
                                                             :error error
                                                             :pending false})))))
                    (when-not (:pending @result-state)
                      (set-state! result-state #(assoc % :pending true)))
                    (fn [] (set! (.-unmounted guard) true))))
                (if (and deps
                         (not= js/undefined deps))
                  (apply array deps)
                  js/undefined))
     result-state)))
