(ns app.logger)

(defn stringify [obj]
  (.stringify js/JSON (clj->js obj)))

(defn log [msg payload]
  (println msg (stringify payload) "\n"))
