(ns app.specs
  (:require [cljs.spec :as spec]))

(spec/def ::timestamp string?)
(spec/def ::user string?)
(spec/def ::url string?)
(spec/def ::min-id string?)

(spec/def ::entities map?)
(spec/def ::created_at string?)
(spec/def ::text string?)

(spec/def ::keywords (spec/* map?))
(spec/def ::description (spec/nilable string?))
(spec/def ::content (spec/nilable string?))
(spec/def ::media map?)

(spec/def ::tweet (spec/keys :req-un [::entities ::created_at ::text]))

(spec/def ::user-data (spec/keys :req-un [::user]
                                 :opt-un [::min-id]))

(spec/def ::bookmark (spec/keys :req-un [::url ::user ::timestamp]))
(spec/def ::bookmarks (spec/* ::bookmark))
(spec/def ::tweets (spec/* ::tweet))

(spec/def ::resource (spec/keys :req-un [::url ::description ::keywords ::content ::media]))
(spec/def ::resources (spec/* ::resource))

(spec/def ::payload (spec/or :resource  ::resource
                             :tweets    ::tweets
                             :bookmarks ::bookmarks
                             :resources ::resources
                             :user-data ::user-data))

(spec/def ::type string?)
(spec/def ::action (spec/keys :req-un [::payload ::type]))
