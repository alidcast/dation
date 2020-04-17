(ns rejure.dation-test-setup
  (:require [datomic.client.api :as d]
            [rejure.dation.db-dev :as db]))

(defn db-fixture [tests]
  (d/create-database (db/$client) {:db-name (db/$name :test)})
  (d/delete-database (db/$client) {:db-name (db/$name :test)})
  (tests)
  )


(comment 
  (db/$conn :test))
