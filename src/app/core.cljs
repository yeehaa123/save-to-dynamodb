(ns app.core
  (:require [app.action :as action]
            [app.db :as db]
            [cljs.core.async :refer [<!]]
            [cljs.core.match :refer-macros [match]]
            [cljs.nodejs :as node])
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
