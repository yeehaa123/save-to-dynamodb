(ns app.core
  (:require [cljs.nodejs :as node]
            [app.action :as action]
            [app.db :as db]
            [app.logger :as logger]
            [app.specs :as specs]
            [cljs.spec :as spec]
            [cljs.core.async :refer [<! put! close! chan >!]]
            [cljs.core.match :refer-macros [match]]
            [clojure.walk :as walk]
            [clojure.string :as str])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(node/enable-util-print!)

(defn ^:export handler [event context cb]
  (if-let [{:keys [payload type]} (action/convert event)]
    (go
      (match [(<! (apply db/save payload))]
             [{:success _}] (cb nil "Save Succeeded")
             [{:error _}] (cb "Save Failed" nil)))
    (cb "Invalid Event" nil)))


(defn -main [] identity)
(set! *main-cli-fn* -main)
