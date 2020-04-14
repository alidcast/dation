(ns starter.db
  (:require [clojure.java.io :as io]
            [rejure.dation.schema :as dschema]))

(def schema (dschema/read-edn (slurp (io/reader "example/resources/demo/db-schema.edn"))))
