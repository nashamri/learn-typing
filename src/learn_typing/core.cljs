(ns learn-typing.core
    (:require
     [reagent.core :as r]
     [clojure.string :as s]))

(def article "ليسب هي لغة برمجة ذات تاريخ عريق، فقد تم وضع مواصفاتها عام ١٩٥٨ وبذلك تحل بعد الفورتران التي طورت قبلها بسنة، كثاني لغة برمجة عالية المستوى.")
(def words (s/split article #" "))
(def app-state (r/atom {:goal (first words) :user-input "" :score 0}))

(defn home-page []
  [:div
   [:h3 "درجتك: " (:score @app-state)]
   [:p article]
   [:h3 "اكتب: " (:goal @app-state)]
   ;; [:h3 (:user-input @app-state)]
   [:input {:type "text"
            :value (:user-input @app-state)
            ;; :on-key-press (fn [e] (println "key press" (.-charCode e)))
            :on-change (fn [e] (swap! app-state assoc :user-input (-> e .-target .-value)))
            }]
   [:h4 (when (= (:goal @app-state)
                 (:user-input @app-state))
          (swap! app-state assoc
                 :goal (nth words (inc (:score @app-state)))
                 :user-input ""
                 :score (inc (:score @app-state))))]])


(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
