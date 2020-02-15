(ns demo.tictac
  (:require [react]
            [react-tools.component :refer [->js]])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def winning-patterns
  (let [diag+ (map #(vector % %) (range 3))
        diag- (map #(vector (- 2 %) %) (range 3))
        lines (map #(map (partial vector %) (range 3)) (range 3))
        columns (map #(map (fn [[x y]] [y x]) %) lines)]
    (vec
      (into #{diag- diag+}
            (concat lines columns)))))

(defn get-player-tiles
  [game player]
  (map key
       (filter 
         (fn [[tile p]]
           (= p player))
         game)))

(defn is-winner [game player]
  (let [tiles (get-player-tiles game player)]
    (some (set tiles) winning-patterns)))

(defcomponent Square
  [props]
  :let [{:keys [x y onSquareClicked]} props
        col (-> x inc) 
        row (-> y inc)
        owner (:owner props)
        color (:color props)] 
  [:div {:style {:gridColumn col
                 :gridRow row
                 :border "solid 1px black"
                 :margin 1
                 :cursor "pointer"
                 :backgroundColor color}
         :onClick #(do
                       (when (and (not owner)
                                  (fn? onSquareClicked))
                         (onSquareClicked [x y])))}])

(defcomponent Grid
  [props]
  [:div {:style {:display "grid"
                 :gridTemplateColumns "50px 50px 50px"
                 :gridTemplateRows "50px 50px 50px"
                 :height 150}}
   (:children props)])


(def board (for [x (range 3)
                 y (range 3)]
             [x y]))

(defcomponent PatternSelect
  [props]

  :let [{:keys [set-game]} props]

  [:form
   {:onChange #(let [pattern (->> % .-target .-value int (nth winning-patterns))]
                 (set-game (into {}
                                 (map
                                   (fn [coord]
                                     [coord :player1])
                                   pattern))))} 
   (->js (map
           (fn [pattern index]
             (jsx [:label {:key index}
                   [:input {:key index :type "radio" :name "pattern" :value index}]
                   (str pattern)]))
           winning-patterns
           (range)))])

(defcomponent TicTac

  :let [modes #{:play :winning-patterns}
        players #{:player1 :player2}
        colors {:player1 "lightblue"
                :player2 "red"}]

  :state [game {}
          player :player1
          mode :play
          error-message nil]
  [:div
   [:h1 "Tic Tac"]
   [:p (str mode)]
   [:select {:defaultValue (name mode)
             :onChange #(do (-> % .-target .-value keyword set-mode)
                            (set-game {}))}
    [:option {:value (name :play)} (name :play)]
    [:option {:value (name :winning-patterns)} (name :winning-patterns)]]
   (when (= :winning-patterns mode)
     (jsx [PatternSelect {:set-game set-game}]))
   (when (= :play mode)
     (jsx [:p (str player)]))
   [Grid
    (map (fn [[x y] index]
           (let [owner (game [x y])]
             (jsx [Square {:key index
                           :x x
                           :y y
                           :color (or (colors owner) "")
                           :owner owner
                           :onSquareClicked (fn [coords]
                                              (when (= :play mode)
                                                (set-game #(conj % [coords player]))
                                                (set-player (case player
                                                              :player1 :player2
                                                              :player2 :player1))))}])))
         board
         (range))]])
