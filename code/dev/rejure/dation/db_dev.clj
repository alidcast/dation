(ns rejure.dation.db-dev
  (:require [datomic.client.api :as d]))

(def db-name "dation/db-dev")

(defn get-info []
    {:system  (System/getenv "DB_SYSTEM")
     :profile (System/getenv "AWS_PROFILE")
     :region  (System/getenv "AWS_REGION")})

(def get-client "Get local system's database client."
  (memoize (fn []
             (let [info     (get-info)
                   system   (:system info)
                   profile  (:profile info)
                   region   (:region info)]
               (if-not (every? some? [system profile region])
                 (throw (Exception. "Dev environment not setup, run 'source env'."))
                 (d/client {:server-type   :ion
                            :creds-profile profile
                            :region        region
                            :system        system
                            :query-group   system
                            :endpoint      (format "http://entry.%s.%s.datomic.net:8182/" system region)
                            :proxy-port   8182}))))))

(defn get-conn "Get local system's database connection."
  [] (d/connect (get-client) {:db-name db-name}))

(defn get-inst "Get local system's database instance."
  [] (d/db (get-conn)))

(comment
  ;; Test env info and connection
  (get-info)
  (get-client)
  (get-conn)
  
  ;; Manage database
  (d/create-database (get-client) {:db-name db-name})
  (d/delete-database (get-client) {:db-name db-name})
  
  )
