(ns app.db
  (:require [app.logger :as logger]
            [cljs.core.async :as async :refer [>! chan]]
            [cljs.nodejs :as node]
            [clojure.walk :as walk])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def AWS (node/require "aws-sdk"))
(def dynamo (AWS.DynamoDB.DocumentClient.))

(defn replaceEmptyStrings [obj]
  (walk/postwalk-replace {"" nil} obj))

(defn marshal [item]
  (-> item
      replaceEmptyStrings
      clj->js))

(defn create-query [table-name item]
  {:TableName table-name
   :Item item})

(defn -save [query]
  (let [c (chan)]
    (.put dynamo (clj->js query) #(go
                                    (let [response (if %1
                                                     {:error %1}
                                                     {:success (:id (:Item query))})]
                                      (when (= :error response)
                                        (logger/log "Error Saving Item: " query))
                                      (>! c response)
                                      (async/close! c))))
    c))

(defn save [table-name items]
  (let [queries (map #(create-query table-name %1) items)
        query-chans (async/merge (map -save queries))]
    (async/into [] query-chans)))
