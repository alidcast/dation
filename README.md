# Dation (alpha)

First declare your Datomic schema using EDN and the reader literal shorthands we provide: 

```clj 
;; resources/sdb-app-schema.edn
{:name    :app-schema 
 :version "1.0"
 :installs [#ent #:user{:username  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :email     [:db.type/string :db.cardinality/one :db.unique/identity]
                        :password  [:db.type/string :db.cardinality/one]}
            #ent #:note{:title  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :owner  [:db.type/ref    :db.cardinality/one true]}
            #spec [:file.spec/owner-fk [:file/owner] 'resoflect.api.app.file.preds/owner?]]}  
```

Then read your schema confirmation and install the attributes:

```clj
;; myapp/db.clj
(ns myapp.db
  (:require [dation.schema :as dsch]))

(def schema (dsch/read-edn (io/reader "resources/app-db-schema.edn")))
```
