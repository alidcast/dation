(ns rejure.dation.schema "Define Datomic Schemas with EDN."
  (:require [clojure.edn :as edn]))

;; TODOS
;; ## installs
;; - need a function to get all schemas and their attribute checks
;; - consider where installs should be ascending or descending, or even in place with version numbers.
;; ## predicates 
;; - todo does not seem to be a way to curry arguments, open up issue in datomic about this
;; :ensures  [[:schema.fk [:file.owner] ['dation.preds/valid-fk? :user/id]]]
;; ## extra attrs 
;; - handle docs and extra attributes i.e. [doc [...attrs] {:schema/deprecated true}]
;; ## versioning / migrations 
;; maybe keep count of schema attributes / migrations and make sure non are deleted 
;; so verisioning can force update but it can't detract? 
;; well.. unless you're trying to remove an entity but maybe we should make that explicit.

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
          attrs  (cond->> {:db/ident      ident
                           :db/valueType  type
                           :db/cardinality cardinality}
                   (some? unique) (merge {:db/unique unique})
                   (some? comp?)  (merge {:db/isComponent comp?})
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
                          (assoc v :db/ident k))))
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
          attrs  (cond->> {:db/ident        ident
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

(defn read-edn "Reads schema as edn string `s`."
  [s] (edn/read-string {:readers (create-readers)}
                       s))
