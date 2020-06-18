(ns starter.db
  (:require [clojure.java.io :as io]
            [dation.schema :as ds]))

(def schema (ds/read-edn (slurp (io/reader (io/resource "starter/schema.edn")))))
 