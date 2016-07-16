(ns app.logger
  (:require [cljs.spec :as spec]
            [app.specs :as specs]))

(defn stringify [obj]
  (.stringify js/JSON (clj->js obj)))

(defn log [msg payload]
  (println msg (stringify payload) "\n"))

(defn log-error [reason action]
  (let [error (clj->js {:type :error
                        :error reason
                        :payload (spec/explain-data ::specs/action action)})]
    (log "Error "error)))
