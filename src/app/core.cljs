(ns app.core
  (:require [cljs.nodejs :as node]
            [app.action :as action]
            [app.specs :as specs]
            [cljs.spec :as spec]
            [cljs.core.async :refer [<! put! close! chan >!]]
            [cljs.core.match :refer-macros [match]]
            [clojure.walk :as walk]
            [clojure.string :as str])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(node/enable-util-print!)
(def AWS (node/require "aws-sdk"))
(def dynamo (AWS.DynamoDB.DocumentClient.))

(defn replaceEmptyStrings [obj]
  (walk/postwalk-replace {"" nil} obj))

(defn marshal [item]
  (-> item
      replaceEmptyStrings
      clj->js))

(defn create-query [table-name items]
  {:RequestItems
   {table-name (map (fn [item]
                      {:PutRequest
                       {:Item (marshal item)}}) items)}})

(defn save [table-name items]
  (let [c (chan)
        query (create-query table-name items)]
    (.batchWrite dynamo (clj->js query) #(go (>! c (if %1
                                                     {:error %1}
                                                     {:success %2}))))
    c))

(defn handle-error [reason payload cb]
  (let [error (clj->js {:type :error
                        :error reason
                        :payload payload})]
    (println (.stringify js/JSON error))
    (cb error nil)))

(defn ^:export handler [event context cb]
  (println "Event: " (.stringify js/JSON (clj->js event)) "\n")
  (let [incoming-action (action/convert event)]
    (println "Incoming: " (.stringify js/JSON (clj->js incoming-action)) "\n")
    (if (spec/valid? ::specs/action incoming-action)
      (go
        (let [{:keys [payload type]} (spec/conform ::specs/action incoming-action)
              response (<! (apply save payload))]
          (match [response]
                 [{:success _}] (cb nil (.stringify js/JSON (clj->js response)))
                 [{:error _}] (cb (.stringify js/JSON (clj->js response)) nil))))
    (handle-error :invalid-incoming-action (spec/explain-data ::specs/action incoming-action) cb))))

(defn -main [] identity)
(set! *main-cli-fn* -main)
