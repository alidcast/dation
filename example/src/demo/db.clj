(ns demo.db
  (:require [clojure.java.io :as io]
            [datomic.client.api :as d]
            [rejure.dation.schema :as dschema]
            [rejure.dation.attr   :as dattr]))

(def schema (dschema/read-edn (slurp (io/reader "example/resources/demo/db-schema.edn"))))

(def get-client "Get local system's database client."
  (memoize (fn []
             (d/client {:server-type   :ion
                        :creds-profile "resoflect"
                        :region        "us-east-1"
                        :system        "resodb-storage-dev"
                        :query-group   "resodb-storage-dev"
                        :endpoint      (format "http://entry.%s.%s.datomic.net:8182/" "resodb-storage-dev" "us-east-1")
                        :proxy-port   8182}))))

(defn get-conn "Get local system's database connection."
  [] (d/connect (get-client) {:db-name "dation-db-dev"}))

(defn get-inst "Get local system's database instance."
  [] (d/db (get-conn)))

(comment
  (get-client)
  (def conn (get-conn))
  
  (d/create-database (get-client) {:db-name "dation-db-dev"})
  (d/delete-database (get-client) {:db-name "dation-db-dev"})

  (dschema/ensure-dation-attrs (get-conn))

  (dattr/exists? (get-inst) :dation.schema/name)

  (dschema/installed? (get-inst) :demo-schema))
