(ns rejure.dation.db-dev
  (:require [datomic.client.api :as d]))

(defn $name "Get db name for given environment."
  [env] (str "dation-db-" (name env)))

(defn $info "Get env info."
  []
  {:system  (System/getenv "DB_SYSTEM")
   :profile (System/getenv "AWS_PROFILE")
   :region  (System/getenv "AWS_REGION")})

(def $client "Get local system's database client."
  (memoize (fn []
             (let [info     ($info)
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

(defn $conn "Get local system's database connection."
  [env] (d/connect ($client) {:db-name ($name env)}))

(defn $inst "Get local system's database instance."
  [env] (d/db ($conn env)))

(comment
  ;; Test env info and connection
  ($info)
  ($client)
  ($conn :dev)
  
  ;; Manage database
  (d/create-database ($client) {:db-name ($name :dev) })
  (d/delete-database ($client) {:db-name ($name :dev)})
  )
