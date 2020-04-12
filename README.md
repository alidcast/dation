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

Dation, motivation: 

1. Datomic attribute declarations feel verbose and while some libraries have attempted to make it easier to declare attributes they either expect you to do so via code or create their own custom DSL.
2. There is no built-in way to manage schema changes in Datomic. Some existing libraries help handle migrations but only work with Datomic On-Prem, and none are specifically tailored toward the accretion-only model Datomic Cloud encourages.

Design goals:

1. Make Datomic schema configurations explicit, taking advantage of EDN and reader literals.
2. Provide shorthands (not DSLs) for declaring datomic attribute maps, without sacrificing semantic meaning.
3. Provide a reliable yet simple way to handle schema accretions, supporting attribute installs and data migrations.
4. Work with Datomic Cloud (and optionally Datomic On-Prem).

