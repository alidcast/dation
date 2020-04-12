(ns rejure.dation.schema "Define Datomic Schemas with EDN."
  (:require [clojure.edn :as edn]))

(defn- attr->datomic-attr-map
  "Converts generic attribute shorthand to datomic attribute map.
   Excepts vector `v` of [ident type cardinality (?unique | ?component) ?pred]"
  [v]
  (if-not (and (vector? v) (>= (count v) 3))
    (throw (AssertionError. "#attr declaration must be vector with min of ident, type, and cardinality attrs."))
    (let [[ident type cardinality] v
          constraint (get v 3)
          unique     (when (keyword? constraint) constraint)
          comp?      (when (boolean? constraint) constraint)
          pred       (get v 4)
          attrs  (cond->> {:db.ident       ident
                           :db.type        type
                           :db.cardinality cardinality}
                   (some? unique) (merge {:db.unique unique})
                   (some? comp?)  (merge {:db.isComponent comp?})
                   (some? pred)   (merge {:db.attr/preds pred}))]
      attrs)))

(defn- ent->datomic-attr-map 
  "Converts entity attribute shorthand to datomic attribute map.
   Exects map `m` of form '{ident attrs} where `attrs` is [type cardinality (?unique | ?component) ?pred]."
  [m]
  (if-not (map? m)
    (throw (AssertionError. "#ent declaration must a map of {ident attrs}."))
    (reduce (fn [acc [k v]]
              (conj acc (if (vector? v)
                          (attr->datomic-attr-map (into [k] v))
                          (assoc v :db.ident k))))
            []
            m)))

(defn- spec->datomic-attr-map
  "Converts spec attribute shorthand to datomic attribute map.
   Excepts vector `v` of [ident req-attrs ?pred]"
  [v]
  (if-not (and (vector? v) (>= (count v) 2))
    (throw (AssertionError. "#spec declaration must be vector with ident and req-attrs."))
    (let [[ident req-attrs] v
          pred  (get v 2)
          attrs  (cond->> {:db.ident        ident
                           :db.entity/attrs req-attrs}
                   (some? pred)   (merge {:db.attr/preds pred}))]
      attrs)))

(defn- enum->datomic-attr-map
  [kw]
  (if-not (keyword? kw)
    (throw (AssertionError. "#enum declaration must be a keyword."))
    {:db/ident kw}))

(defn create-readers "Create edn reader literal attribute shorthands."
  []
  {'attr attr->datomic-attr-map
   'ent  ent->datomic-attr-map
   'spec spec->datomic-attr-map
   'enum enum->datomic-attr-map})

;; TODO consider where installs should be ascending or descending, or even in place with version numbers.
(defn read-edn "Reads schema as edn string `s`."
  [s] (edn/read-string {:readers (create-readers)}
                       s))
