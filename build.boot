(set-env!
 :source-paths #{"src"}
 :dependencies '[[adzerk/boot-cljs          "1.7.228-1" :scope "test"]
                 [adzerk/boot-cljs-repl     "0.3.2"]
                 [com.cemerick/piggieback   "0.2.1" :scope "test"]
                 [weasel                    "0.7.0" :scope "test"]
                 [org.clojure/tools.nrepl   "0.2.12" :scope "test"]
                 [org.clojure/clojurescript "1.9.89"]
                 [org.clojure/core.match    "0.3.0-alpha4"]
                 [org.clojure/core.async    "0.2.374"]])

(require
 '[adzerk.boot-cljs      :refer :all]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]])

(deftask build []
  (task-options! cljs   {:compiler-options {:optimizations :simple
                                            :target :nodejs}})
  (comp (cljs)
        (target)))

(deftask dev []
  (comp (watch)
        (cljs-repl)
        (build)))
