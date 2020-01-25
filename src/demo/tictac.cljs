(ns demo.tictac
  (:require [react]
            [cljs-bean.core])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

(defonce winning-patterns
  (let [diag+ (map #(vector % %) (range 3))
        diag- (map #(vector (- 2 %) %) (range 3))
        lines (map #(map (partial vector %) (range 3)) (range 3))
        columns (map #(map (fn [[x y]] [y x]) %) lines)]
    (into #{diag- diag+}
          (concat lines columns))))

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

(defcomponent TicTac
  :let [board (for [x (range 3)
                    y (range 3)]
                [x y])
        players #{:player1 :player2}]
  :state [game {}
          player :player1
          error-message nil]
  :let [_ (println (get-player-tiles game :player1))
        _ (println game)]

  [:div
   [:h1 (str (name player) " turn to play")]
   (when error-message
     (jsx [:h3 error-message]))
   (when (is-winner game :player1)
     (jsx [:h3 "Player 1 wins"]))
   (when (is-winner game :player2)
     (jsx [:h3 "Player 2 wins"]))
   [:div {:style {:display "grid"
                  :gridTemplateColumns "50px 50px 50px"
                  :gridTemplateRows "50px 50px 50px"
                  :height 150}}
    (map (fn [[x y]]
           (let [owner (game [x y])]
             (jsx [:div {:style {:gridColumn (str (inc x))
                                 :gridRow (str (inc y))
                                 :border "solid 1px black"
                                 :margin 1
                                 :backgroundColor (if (game [x y]) "green" "red")
                                 :cursor "pointer"}
                         :onClick (fn [_]
                                    (if-not owner
                                      (do
                                        (set-game #(conj % [[x y] player]))
                                        (set-player (if (= player :player1)
                                                      :player2
                                                      :player1))
                                        (set-error-message nil))
                                      (set-error-message "Tiled has already been played")))}
                   (if owner
                     (if (= :player1 owner) "x" "o"))])))
         board)]])
