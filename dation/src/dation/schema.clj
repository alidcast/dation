(ns dation.schema "Define Datomic schema attributes in EDN."
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]))

;; == #db/attr == 

(s/def ::attr-preds
  #(or (symbol? %) (every? symbol? %)))

(s/def ::attr-defs
  (s/cat :type keyword?
         :cardinality keyword?
         :unique (s/? keyword?)
         :component (s/? boolean?)))

(s/def ::attr
  (s/cat :ident keyword?
         :doc (s/? string?)
         :defs #(s/valid? ::attr-defs %)
         :preds (s/? ::attr-preds)))

(comment
  (s/conform ::attr [:user/username [:db.type/string :db.cardinality/one]])
  (s/conform ::attr [:user/username [:db.type/string :db.cardinality/one :db.unique/identity]])
  (s/conform ::attr [:user/username [:db.type/string :db.cardinality/one true] 'db.fns/foo]))

(defn- attr->datomic-attr-map
  "Converts generic attribute shorthand to datomic attribute map.
   Excepts vector `v` of [ident ?doc [type cardinality (?unique | ?component)] ?pred]"
  [v]
  (let [parsed (s/conform ::attr v)]
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid #db/attr input." (s/explain-data ::attr parsed)))
      (let [{:keys [ident doc preds]} parsed
            {:keys [type cardinality unique comp?]} (s/conform ::attr-defs (:defs parsed))]
        (cond-> {:db/ident       ident
                 :db/valueType   type
                 :db/cardinality cardinality}
          (some? doc)    (assoc :db/doc doc)
          (some? unique) (assoc :db/unique unique)
          (some? comp?)  (assoc :db/isComponent comp?)
          (some? preds)  (assoc :db.attr/preds preds))))))

(comment 
  (attr->datomic-attr-map [:user/username [:db.type/string :db.cardinality/one]]))

;; == #db/spec == 

(s/def ::spec
  (s/cat :ident  keyword?
         :doc    (s/? string?)
         :req-attrs (s/coll-of keyword?)
         :preds (s/? ::attr-preds)))

(comment
  (s/conform ::spec [:user.spec/new [:user/email] 'foo]))


(defn- spec->datomic-attr-map
  "Converts spec attribute shorthand to datomic attribute map.
   Excepts vector `v` of [ident req-attrs ?pred]"
  [v]
  (let [parsed (s/conform ::spec v)]
    (println parsed)
    (if (= parsed ::s/invalid)
      (throw (ex-info "Invalid #db/spec input." (s/explain-data ::spec parsed)))
      (let [{:keys [ident doc req-attrs preds]} parsed]
        (cond-> {:db/ident        ident
                 :db/doc         doc
                 :db.entity/attrs req-attrs}
          (some? preds) (assoc :db.entity/preds preds))))))

(comment
  (spec->datomic-attr-map [:user.spec/new [:user/username]])
  (spec->datomic-attr-map [:user.spec/new [:user/username] 'db.fns/new-user?]))

;; == #db/enum == 

(defn- enum->datomic-attr-map
  [kw]
  (if-not (keyword? kw)
    (throw (AssertionError. "#enum declaration must be a keyword."))
    {:db/ident kw}))

;; == schema readers == 

(defn create-readers
  "Create edn reader literal attribute shorthands.
   Similar to Datomic, all reader literals are namespaced with `db` key."
  []
  {'db/attr  attr->datomic-attr-map
   'db/spec  spec->datomic-attr-map
   'db/enum  enum->datomic-attr-map})

(defn read-edn
  "Reads schema configuration edn string `s`.
   The config should be a map with the following properties: 
      :installs    List of schema attributes to install.
      :migrations  List of data migrations to run."
  [s] (edn/read-string {:readers (create-readers)}
                       s))
