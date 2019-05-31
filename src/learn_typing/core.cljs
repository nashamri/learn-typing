(ns learn-typing.core
  (:require
   [reagent.core :as r]
   [clojure.string :as s]))

(declare win-page)
(declare game-page)
(def articles {:easy ["ليسب هي لغة برمجة ذات تاريخ عريق، فقد تم وضع مواصفاتها عام 1958 وبذلك تحل بعد الفورتران التي طورت قبلها بسنة، كثاني لغة برمجة عالية المستوى."]
               :medium ["بايثون هي لغة برمجة، من لغات المستوى العالي، تتميز ببساطة كتابتها وقراءتها، سهلة التعلم، تستخدم أسلوب البرمجة الكائنية، مفتوحة المصدر، وقابلة للتطوير. تعتبر لغة بايثون لغة تفسيرية، متعددة الأغراض وتستخدم بشكل واسع في العديد من المجالات، كبناء البرامج المستقلة باستخدام الواجهات الرسومية المعروفة وفي عمل برامج الويب،"]
               :hard ["هاسكل هي لغة برمجة مطابقة للمعايير، للأغراض العامة، وهي لغة وظيفية إلى حد كبير، دون دلالات ألفاظ ملزمة وبكتابة ثابتة وقوية. وقد سميت بهاسكل على اسم عالم المنطق هاسكل كوري. وفي لغة هاسكل، تمثل الوظيفة مواطن من الدرجة الأولى من لغة البرمجة. ولكونها لغة برمجة وظيفية فإن بنية التحكم الرئيسية هي الوظيفة. وترجع أصول اللغة إلى ملاحظات هاسكل كوري وأتباعه من المفكرين، بأن الإثبات هو برنامج والمعادلة التي يثبتها هي نوع للبرنامج"]})

(def app-state (r/atom {:init {:difficulty "easy"
                               :custom-text ""}
                        :goal ""
                        :current-word 1
                        :words []
                        :total-words 0
                        :user-input ""
                        :score 0
                        :multiplier 1
                        :max-multiplier 5.0
                        :highlight 0
                        :last-time 0}))

(defn reset-app-state! []
  (swap! app-state assoc
         :goal ""
         :current-word 1
         :words []
         :total-words 0
         :user-input ""
         :score 0
         :multiplier 1
         :max-multiplier 5.0
         :highlight 0
         :last-time 0))

(defn calc-score [score multiplier word-len]
  (+ score
     (* (if (< multiplier 1) 1 multiplier)
        word-len)))

(defn start-page []
  [:div
   [:div.columns.is-centered
    [:div.column.is-full.has-text-centered
     [:h3 "اختر مستوى الصعوبة"]
     [:div.column.is-full.has-text-centered
      [:select {:name :game-diff
                :on-change (fn [e] (swap! app-state assoc-in
                                          [:init :difficulty] (-> e .-target .-value)))}
       [:option {:value :easy} "سهل"]
       [:option {:value :medium} "متوسط"]
       [:option {:value :hard} "صعب"]
       [:option {:value :custom} "مخصص"]
       ]]
     [:textarea {:style {:display (if (= (get-in @app-state [:init :difficulty]) "custom")
                                       :block
                                       :none)}
                 :cols "50"
                 :row "9"
                 :placeholder "أضف النص هنا"
                 :on-change (fn [e] (swap! app-state assoc-in
                                           [:init :custom-text] (-> e .-target .-value)))}]
     [:div.column.is-full.has-text-centered
      [:a.button.is-link
       {:on-click (fn [e]
                    (let [diff (keyword (get-in @app-state [:init :difficulty]))
                          raw-words (if (= diff :custom)
                                      (get-in @app-state [:init :custom-text])
                                      (rand-nth (diff articles)))
                          words (interpose " " (s/split raw-words #" "))]
                      (swap! app-state assoc
                             :words words
                             :goal (first words)
                             :total-words (count words))
                      (r/render [game-page] (.getElementById js/document "app"))))}
       "إبدأ"]]
     ]]])


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
    [:h1 "لقد فزت!"]
    [:h1 (.round js/Math (:score @app-state))]
    [:h5 (str "أعلى درجة ممكنة هي: " (reduce #(+ %1 (* (:max-multiplier @app-state) %2)) (map count (:words @app-state))))]
    [:a.button.is-link {:on-click (fn [e] (do
                                 (r/render [start-page] (.getElementById js/document "app"))
                                 (reset-app-state!)))} "إلعب مرة أخرى؟"]]])

(defn mount-root []
  (r/render [start-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
