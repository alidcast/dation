(ns dation.schema 
  "Datomic schema accretion tools."
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [datomic.client.api :as d]))

;; # Config Reader 
;; Edn literal shortshands for declaring datomic schema attributes.

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

(defn create-readers 
  "Create edn reader literal attribute shorthands.
   Similar to Datomic, all reader literals are namespaced with `db` key."
  []
  {'db/attr  attr->datomic-attr-map
   'db/ent   ent->datomic-attr-map
   'db/spec  spec->datomic-attr-map
   'db/enum  enum->datomic-attr-map})

(defn read-edn 
  "Reads schema configuration edn string `s`.
   The config should be a map with the following properties: 
      :installs    List of schema attributes to install.
      :migrations  List of data migrations to run."
  [s] (edn/read-string {:readers (create-readers)}
                       s))

;; # Attribute Installer
;; Ensures that given schema attributes are install in database.

(defn has-attr? "Checks if `db` has attribute by `ident`."
  [db ident]
  (-> (d/pull db {:selector '[:db/valueType]
                  :eid ident})
      seq
      boolean))


(defn ensure-admin-attrs "Ensures that attributes used for tracking schema accretions are installed."
  [conn]
  (when-not (has-attr? (d/db conn) :dation.schema/install)
    (d/transact conn {:tx-data (ent->datomic-attr-map
                                {:dation.schema/name [:db.type/keyword :db.cardinality/one]})})))

(defn installed? "Checks if `db` has attributes of schema name `sn` installed."
  [db sn]
  (and (-> (d/q {:query '[:find ?e
                          :in $ ?sn
                          :where [?e :dation.schema/name ?sn]]
                 :args [db sn]})
           seq
           boolean)))

(defn attrs "Gets map of all installed `db` schema attributes."
  [db]
  (->> (d/pull db '{:eid 0 :selector [{:db.install/attribute [*]}]})
       :db.install/attribute
       (remove (fn [m] (str/starts-with? (namespace (:db/ident m)) "db")))
       (map #(update % :db/valueType :db/ident))
       (map #(update % :db/cardinality :db/ident))))

(defn ensure-ready
  "Ensure that `schema` migrations have run (TODO) and attributes have been installed.
   See [[read-edn]] for configuration details."
  [conn schema]
  (ensure-admin-attrs conn)
  (let [name (:name schema)]
    (when-not (installed? (d/db conn) name)
      (doseq [attrs (:installs schema)]
        (d/transact conn {:tx-data (cons {:dation.schema/name name}
                                         attrs)})))
    :ready))
