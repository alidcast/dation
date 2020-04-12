(ns rejure.dation.schema "Define Datomic Schemas with EDN."
  (:require [clojure.edn :as edn]))

(defn- ent-vec->datomic-attr-map "Converts entity vector attribute shorthand to datomic attribute map."
  [v]
  (if (>= (count v) 3)
    (let [[ident type cardinality] v
          unique (get v 3)
          attrs  (cond->> {:db.ident       ident
                           :db.type        type
                           :db.cardinality cardinality}
                   (some? unique) (merge {:db.unique unique}))]
      attrs)
    (throw (AssertionError. "Entity vector must have ident, type, and cardinality attributes declared."))))

;; TODO handle references vs regular attributes (unique vs isComponent)
(defn- ent->datomic-attr-map 
  "Convert entity shorthand `x` to datomic attribute map.
   Declaration can be a vector of form '[ident type cardinality ?unique].
   Or it can be a map of form '{ident attrs} where attrs can be a map or above vector shorthand, 
   and the ident key will be merged with the final attrs."
  [x]
  (cond
    (vector? x) (ent-vec->datomic-attr-map x)
    (map? x)    (reduce (fn [acc [k v]]
                          (if (vector? v)
                            (conj acc (ent-vec->datomic-attr-map (into [k] v)))
                            (conj acc (assoc v :db.ident k))))
                        []
                        x)
    :else (throw (AssertionError. "Entity declaration must be a vector or a map."))))


(defn create-readers "Create edn reader literals."
  []
  {'ent ent->datomic-attr-map})

(defn read-edn "Reads schema as edn string `s`."
  [s] (edn/read-string {:readers (create-readers)}
                       s))
