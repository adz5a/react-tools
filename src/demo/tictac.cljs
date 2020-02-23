(ns demo.tictac
  (:require [react]
            [react-tools.component :refer [->js]]
            ["@chakra-ui/core" :as ui]
            [clojure.set :refer [subset?]])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(def winning-patterns
  (let [diag+ (map #(vector % %) (range 3))
        diag- (map #(vector (- 2 %) %) (range 3))
        lines (map #(map (partial vector %) (range 3)) (range 3))
        columns (map #(map (fn [[x y]] [y x]) %) lines)]
    (vec
      (into #{diag- diag+}
            (concat lines columns)))))

(def board (for [x (range 3)
                 y (range 3)]
             [x y]))

(defcomponent Square
  [props]
  :let [{:keys [x y onClick]} props
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
                                  (fn? onClick))
                         (onClick [x y])))}])

(defcomponent Grid
  [props]
  [:div {:style {:display "grid"
                 :gridTemplateColumns "50px 50px 50px"
                 :gridTemplateRows "50px 50px 50px"
                 :height 150}}
   (:children props)])

(defcomponent PatternSelect
  :state [patterns #{}]
  ;; derive the tiles from the patterns
  :let [tiles (reduce (partial apply conj) #{} patterns)]
  [ui/Box
   [ui/Box
    [ui/Heading {:as "h4" :size "s"} "Choose winning combinations"]
    [ui/Flex
     [ui/Stack
      [:react/map [pattern winning-patterns
                   index (range)]
       [ui/Checkbox
        {:key index
         :isChecked (contains? patterns pattern)
         :onChange (fn [event]
                     (let [checked? (-> event .-target .-checked)]
                       (if checked?
                         (set-patterns (fn [patterns]
                                         (conj patterns pattern)))
                         (set-patterns (fn [patterns]
                                         (disj patterns pattern))))))}
        (str pattern)]]]
     [ui/Box {:flex "auto"
              :paddingLeft "4rem"}
       [Grid 
        [:react/map [[x y] board
                     index (range)]
         [Square {:x x
                  :y y
                  :key index
                  :color (if (contains? tiles [x y]) "green" "white")}]]]]]]])

(defcomponent Game
  :let [players #{:red :green}
        winning-patterns-set (into #{}
                                   (map set winning-patterns))
        _ (println winning-patterns-set)]
  :state [current-player :red
          played-tiles {}]
  :let [board-is-full? (= (count board) (count played-tiles))
        green-tiles (->> played-tiles
                         (filter #(= :green (val %)))
                         (map key)
                         set)
        red-tiles (->> played-tiles
                       (filter #(= :red (val %)))
                       (map key)
                       set)
        red-won? (some (fn [pattern] (subset? pattern red-tiles))
                       winning-patterns-set)
        green-won? (some (fn [pattern] (subset? pattern green-tiles))
                         winning-patterns-set)
        game-over? (or board-is-full? red-won? green-won?)]
  [ui/Box
   [ui/Heading {:as "h4" :size "s"} "Play Tic Tac Toe"]
   [ui/Text 
    (cond
      red-won? "Game over :red won" 
      green-won? "Game over :red won" 
      game-over? "Game over no one won"
      :else (str "Turn of player " (name current-player)))] 
   [Grid
    [:react/map [[x y] board
                 index (range)]
     [Square {:x x
              :y y
              :key index
              :color (let [owner (played-tiles [x y])]
                       (if owner (name owner) "white"))
              :onClick (fn []
                         (let [owned? (contains? played-tiles [x y])]
                           (when-not owned? 
                             (set-played-tiles
                               (fn [played-tiles]
                                 (conj played-tiles [[x y] current-player])))
                             (set-current-player
                               (fn [current-player]
                                 (case current-player
                                   :red :green
                                   :green :red))))))}]]]

   (if game-over?
     (jsx
       [ui/Button
        {:variantColor "green"
         :onClick #(do
                    (set-current-player :red)
                    (set-played-tiles {}))}
        "Replay !"]))])

(defcomponent TicTac
  [ui/Box
   [ui/Heading "Tic Tac Toe"]
   [PatternSelect]
   [ui/Divider]
   [Game]])
