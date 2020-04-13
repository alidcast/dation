# Dation (alpha)

Dation provides tools for managing Datomic attribute installs and data migrations.

I'll be improving this library as the complexity of my app requires, but feel free to open up an issue if you'd like to discuss any improvements.

Note: I'll be improving this library as the complexity of my app requires, but feel free to open up an issue if you'd like to discuss any improvements.

- [Preview](#preview)
- [Rationale](#rationale)
- [Usage](#usage)

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

## Rationale

Dation, motivation: 

1. Datomic attribute maps are verbose but existing libraries that make them easier to write either expect you to do so as code or create their own custom domain model.
2. There is no built-in way to manage schema changes in Datomic. Existing libraries that help handle migrations but only work with Datomic On-Prem, and none are specifically tailored toward the accretion-only model Datomic encourages (specfically enforced in Datomic Cloud given their lack of support for excision).

Design goals:

1. A Datomic database schema serves as the source of truth (no custom domain model).
2. Make Datomic schema configurations explicit, taking advantage of EDN and reader literals.
3. Provide shorthands (not DSLs) for declaring datomic attribute maps--without sacrificing semantic meaning.
4. Provide a reliable yet simple way to handle schema accretions, supporting attribute installs and data migrations.
5. Work with Datomic Cloud (and optionally Datomic On-Prem).

