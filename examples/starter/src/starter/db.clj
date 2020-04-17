(ns starter.db
  (:require [clojure.java.io :as io]
            [rejure.dation.schema :as ds]))

(def schema (ds/read-edn (slurp (io/reader "resources/starter/db-schema.edn"))))
