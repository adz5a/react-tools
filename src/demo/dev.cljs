(ns demo.dev
  (:require [react]
            [react-dom]
            [react-tools.component :refer [->js]])
  (:require-macros [react-tools.component :refer [defcomponent jsx]]))

;; Pouvoir voter pour augmenter les points

(def developpement-reseau
  ["Developpement reseau"
   ["Invitation d'un contact a une RI / visio au siege" 2]
   ["L'invite est present a la RI / visio" 5]
   ["Presentation d'efficity a une recrue" 5]
   ["Publication d'efficity a une recrue" 5]
   ["Envoi du lien d'inscription - recrutement" 5]
   ["Nouvelle recrue (dossier valider" 5]
   ["Animation de formation" 5]
   ["Suivi de demarrage d'une recrue" 5]])

(def commercialisation
  ["Commercialisation"
   ["Participation formation (Masterclass)" 2]
   ["Traiter les leads vendeur du jour" 5]
   ["Prospection telephonique (pige)" 5]
   ["Prospection terrain" 5]
   ["Apporteur d'affaires / recommendations" 5]
   ["Relance (leads, pige)" 5] ;; Can only be done after the first one
   ["Nouveau RDV vendeur" 5]
   ["Nouveau mandat exclusif" 5] 
   ["Nouveau mandat simple" 5]
   ["Visite acquereur" 5] ;; ajouter un bouton "Planifier"
   ["Baisse de prix / Negociation d'offre" 5]]) ;; ajouter un bouton "Planifier"

(def developpement-personnel
  ["Developpement personnel"
   ["Partager video/SMS inspirant ou business" 2]
   ["Visionner une video inspirante" 5]
   ["Lire 10 pages d'un livre inspirant" 5] ;; mettre des liens vers des suggestions
   ["Pratiquer une activite physique" 5]
   ["Bonus : donner son temps a autrui" 5]])

(defcomponent Section
  [props]
  :let [[title & questions] (:data props)
        add-points (:add-points props)
        remove-points (:remove-points props)]
  [:div
   [:h2 title]
   (->js (map
           (fn [[question points] index]
             (jsx [:div {:key index}
                   [:p {:style {:display "flex"
                                :justifyContent "space-between"
                                :border "solid 1px"
                                :margin "1rem"}}
                    [:span {:style {:fontWeight "bold"}} question]
                    [:span {:style {:marginLeft "1rem"}}
                     [:span  points]
                     [:span {:style {:marginLeft "1rem"}} [:input {:type "checkbox"
                                                                   :onChange (fn [e]
                                                                               (let [checked (-> e .-target .-checked)]
                                                                                 (if checked
                                                                                   (add-points points)
                                                                                   (remove-points points))))}]]]]]))
           questions
           (range)))])

(defcomponent Plan
  [:section {:style {:display "inline-block"}}
   [:h1 "Plan de developpement personnel"]
   [Section {:data developpement-reseau
             :add-points (partial println "add points")
             :remove-points (partial println "add points")}]
   [Section {:data commercialisation
             :add-points (partial println "add points")
             :remove-points (partial println "add points")}]
   [Section {:data developpement-reseau
             :add-points (partial println "add points")
             :remove-points (partial println "add points")}]
   [:button {:type "button"} "Submit"]])


(comment)
