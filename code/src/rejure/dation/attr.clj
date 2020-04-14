(ns rejure.dation.attr "Datomic attribute query helpers."
  (:require [datomic.client.api :as d]
            [clojure.string :as string]))

(defn exists? "Checks if `db` has attribute `ident`."
  [db ident]
  (-> (d/pull db {:selector '[:db/valueType] 
                  :eid ident})
      seq
      boolean))

(defn cardinality "Checks `db` for cardinality of attribute `ident`."
  [db ident]
  (-> (d/pull db {:selector '[:db/cardinality]
                  :eid ident})
      (get-in [:db/cardinality :db/ident])))
