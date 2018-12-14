(ns learn-typing.prod
  (:require
    [learn-typing.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
