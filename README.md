# Dation (alpha)

Dation provides tools for defining Datomic schemas, installing attributes, and running migrations, with everything configured declaratively in EDN format.

- [Preview](#preview)
- [Rationale](#rationale)
- [Usage](#usage)

## Preview 

First declare your Datomic schema using EDN and the reader literal shorthands we provide: 

```clj 
;; resources/sdb-app-schema.edn
{:name     :app-schema
 :version  "1.2"
 :installs [#ent #:user{:username  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :email     [:db.type/string :db.cardinality/one :db.unique/identity]
                        :password  [:db.type/string :db.cardinality/one]}

            #ent #:note{:title  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :owner  [:db.type/ref    :db.cardinality/one true]}
            #spec [:file.spec/owner-fk [:file/owner] 'myapp.schema/file-owner?]]
 :migrations [{:name           :add-default-note-title
               :tx-fn          'dation.migrations/foo}]} 
```

Then read your schema confirmation and install the attributes:

```clj
;; myapp/db.clj
(ns myapp.db
  (:require [dation.schema]))

(def schema (dation.schema/read-edn (io/reader "resources/app-db-schema.edn")))
```

## Rationale

Datomic attribute declarations are verbose and m

1. Explicit schema declaration as data in EDN files, not in code.
2. 


## Usage
