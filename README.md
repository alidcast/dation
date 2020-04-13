# Dation (alpha)

Dation provides tools for managing Datomic attribute installs and data migrations.

## Preview

First configure your Datomic schema in EDN with reader literal shorthands we provide: 

```clj 
;; resources/sdb-app-schema.edn
{:name     :app-schema
 :version  "1.2"
 ;; Attribute Installs
 :installs [#ent #:user{:username  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :email     [:db.type/string :db.cardinality/one :db.unique/identity]
                        :password  [:db.type/string :db.cardinality/one]}

            #ent #:note{:title  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :owner  [:db.type/ref    :db.cardinality/one true]}
            #spec [:file.spec/owner-fk [:file/owner] 'myapp.schema/file-owner?]]
 ;; Data Migrations
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

## Table Of Contents 

- [Rationale](#rationale)

### Rationale

Dation, motivation: 

1. Datomic attribute maps are verbose but existing libraries that make them easier to write expect you to do so as code or create their own custom domain model.
2. There is no built-in way to manage schema changes in Datomic and existing libraries that help handle migrations only work with Datomic On-Prem.

Design goals:

1. A Datomic database schema serves as the source of truth.
2. Make Datomic schema configurations explicit, taking advantage of EDN and reader literals.
3. Work well with the accretion-only model Datomic encourages, with appropriate support for attribute installs and data migrations.
4. Provide shorthands for declaring datomic attribute maps without sacrificing semantic meaning.
5. Work with Datomic Cloud (and optionally Datomic On-Prem).

