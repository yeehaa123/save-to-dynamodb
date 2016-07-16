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

(defn handle-error [reason action]
  (let [error (clj->js {:type :error
                        :error reason
                        :payload (spec/explain-data ::specs/action action)})]
    (logger/log "Error "error)))

(defn ^:export handler [event context cb]
  (let [incoming-action (action/convert event)]
    (go
      (if (spec/valid? ::specs/action incoming-action)
        (let [{:keys [payload type]} (spec/conform ::specs/action incoming-action)
              response               (<! (apply db/save payload))]
          (match [response]
                 [{:success _}] (cb nil (logger/stringify response))
                 [{:error _}] (cb (logger/stringify response) nil)))
        (let [error (<! (handle-error :invalid-incoming-action incoming-action))]
          (cb error nil))))))


(defn -main [] identity)
(set! *main-cli-fn* -main)
