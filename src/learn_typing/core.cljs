(ns learn-typing.core
  (:require
   [reagent.core :as r]
   [clojure.string :as s]))

(declare win-page)
(def articles [
               "ليسب هي لغة برمجة ذات تاريخ عريق، فقد تم وضع مواصفاتها عام ١٩٥٨ وبذلك تحل بعد الفورتران التي طورت قبلها بسنة، كثاني لغة برمجة عالية المستوى."
               "هاسكل هي لغة برمجة مطابقة للمعايير، للأغراض العامة، وهي لغة وظيفية إلى حد كبير، دون دلالات ألفاظ ملزمة وبكتابة ثابتة وقوية. وقد سميت بهاسكل على اسم عالم المنطق هاسكل كوري. وفي لغة هاسكل، تمثل الوظيفة مواطن من الدرجة الأولى من لغة البرمجة. ولكونها لغة برمجة وظيفية فإن بنية التحكم الرئيسية هي الوظيفة. وترجع أصول اللغة إلى ملاحظات هاسكل كوري وأتباعه من المفكرين، بأن الإثبات هو برنامج والمعادلة التي يثبتها هي نوع للبرنامج"
               ])
(def article (rand-nth articles))
(def words (s/split article #" "))
(def app-state (r/atom {:goal (first words)
                        :current-word 1
                        :words words
                        :total-words (count words)
                        :user-input ""
                        :score 0
                        :multiplier 1
                        :max-multiplier 5.0
                        :highlight 0
                        :last-time 0}))

(defn calc-score [score multiplier word-len]
  (+ score
     (* (if (< multiplier 1) 1 multiplier)
        word-len)))

(defn start-page []
  [:div
   [:div.columns.is-centered
    ]]
  )

(defn game-page []
  [:div
   [:div.columns.is-centered
    [:div.column.is-half.has-text-centered
     [:h3 "درجتك " (.round js/Math (:score @app-state))]]]

   [:div.columns.is-centered
    [:div.column.is-full.has-text-centered
     (let [highlight (:highlight @app-state)
           words (:words @app-state)]
       (doall
        (interpose [:span {:class :ws} " "]
                   (map-indexed (fn [i w]
                                  [:span {:class
                                          (cond
                                            (< i highlight) :done
                                            (> i highlight) :normal
                                            (= i highlight) :high)}
                                   w])
                                words))))]]
   [:div.columns
    [:div.column.is-full
     [:input.input.has-text-centered
      {:type "text"
       :value (:user-input @app-state)
       :placeholder (when (= 0 (:score @app-state)) "أكتب هنا")
       :on-focus (fn [e] (swap! app-state assoc :last-time (.now js/Date)))
       :on-change (fn [e]
                    (let [words (:words @app-state)
                          max-multiplier (:max-multiplier @app-state)]
                      (swap! app-state assoc :user-input (-> e .-target .-value))
                      (when (= (:goal @app-state) (:user-input @app-state))
                        (if (= (:current-word @app-state) (:total-words @app-state))
                          (do
                            (swap! app-state assoc
                                   :score (calc-score (:score @app-state)
                                                      (- max-multiplier (/ (- (.now js/Date)
                                                                              (:last-time @app-state))
                                                                           1000))
                                                      (count (last words))))
                            (r/render [win-page] (.getElementById js/document "app")))
                          (swap! app-state assoc
                                 :current-word (inc (:current-word @app-state))
                                 :goal (nth words (:current-word @app-state))
                                 :user-input ""
                                 :multiplier (- max-multiplier (/ (- (.now js/Date) (:last-time @app-state)) 1000))
                                 :last-time (.now js/Date)
                                 :score (calc-score (:score @app-state)
                                                    (:multiplier @app-state)
                                                    (count (nth words (dec (:current-word @app-state)))))
                                 :highlight (inc (:highlight @app-state)))))))}]]]])

(defn win-page []
  [:div.columns
   [:div.column.is-full.has-text-centered
    [:h1 "ما شاء الله عليك، فزت"]
    [:h1 (.round js/Math (:score @app-state))]
    [:h5 "أعلى درجة ممكنة هي"]
    [:h3 (reduce #(+ %1 (* (:max-multiplier @app-state) %2)) (map count (:words @app-state)))]]])

(defn mount-root []
  (r/render [game-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
