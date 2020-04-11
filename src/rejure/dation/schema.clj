(ns rejure.dation.schema "Define Datomic Schemas with EDN."
  (:require [clojure.edn :as edn]))

(defn- attr-vec->datomic-attr-map "Convert attribute vector shorthand `v` to datomic attribute map."
  [v]
  (let [[ident type cardinality] v
        unique (get v 3)
        schema (cond->> {:db.ident       ident
                         :db.type        type
                         :db.cardinality cardinality}
                 (some? unique) (merge {:db.unique unique}))]
    schema))

(comment 
  (attr-vec->datomic-attr-map [:user/username :db.type/string :db.cardinality/one :db.unique/identity]))

(defn create-readers "Create edn reader literals."
  []
  {'attr attr-vec->datomic-attr-map})

(defn read-edn "Reads schema as edn string `s`."
  [s] (edn/read-string {:readers (create-readers)}
                       s))
