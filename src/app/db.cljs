(ns app.db
  (:require [cljs.nodejs :as node]
            [app.logger :as logger]
            [cljs.core.async :refer [<! put! close! chan >!]]
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

(defn create-query [table-name items]
  {:RequestItems
   {table-name (map (fn [item]
                      {:PutRequest
                       {:Item (marshal item)}})
                    (into #{} items))}})

(defn save [table-name items]
  (let [c (chan)
        hashkey (table-name {:resources :url
                             :tweets :id
                             :bookmarks :timestamp})
        unique-items (vals (into {} (map (fn [item] [(hashkey item) item]) items)))
        query (create-query table-name unique-items)]
    (.batchWrite dynamo (clj->js query) #(go
                                           (let [response (if %1
                                                            {:error %1}
                                                            {:success %2})]
                                             (logger/log "Response: " response)
                                             (>! c response))))
    c))
