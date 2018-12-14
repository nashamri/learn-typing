(ns ^:figwheel-no-load learn-typing.dev
  (:require
    [learn-typing.core :as core]
    [devtools.core :as devtools]))


(enable-console-print!)

(devtools/install!)

(core/init!)
