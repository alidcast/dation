# Dation (alpha)

Dation provides tools for managing Datomic attribute installs and data migrations.

## Preview

Imagine you're setting up a user model in your database.

First configure your Datomic schema in EDN with reader literal shorthands we provide: 

```clj 
;; resources/db-app-schema.edn
{:name     :app-schema 
 :version  "1.2"
 ;; Attribute Installs
 :installs [#ent #:user{:username  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :email     [:db.type/string :db.cardinality/one :db.unique/identity]
                        :password  [:db.type/string :db.cardinality/one]}
            #ent #:note{:title  [:db.type/string :db.cardinality/one :db.unique/identity]
                        :owner  [:db.type/ref    :db.cardinality/one true]}]} 
```

Then read your schema confirmation and install the attributes:

```clj
;; myapp/db.clj
(ns myapp.db
  (:require [dation.schema :as ds]))

(def schema (ds/read-edn (slurp (io/reader "resources/app-db-schema.edn"))))

(comment 
  ;; Make sure attributes are installed.
   (ds/ensure-ready conn schema)
  
  ;; Get all installed attributes.
  (ds/attrs conn schema)
```

## Rationale

Dation, motivation: 

* Datomic attribute maps are verbose but the existing libraries that make them easier to write expect you to do so as code or create their own custom domain model.
* There is no built-in way to manage schema changes in Datomic and existing libraries that help handle migrations only work with Datomic On-Prem.

Design goals:

1. A Datomic database schema serves as the source of truth.
2. Make Datomic schema configurations explicit, taking advantage of EDN and reader literals.
3. Work well with the accretion-only model Datomic encourages, with appropriate support for attribute installs and data migrations.
4. Provide shorthands for declaring datomic attribute maps without sacrificing semantic meaning.
5. Work with Datomic Cloud (and optionally Datomic On-Prem).

## Status 

Alpha. Clojar has not been deployed.

## Usage 

- [Configuring your schema](#configuring-your-schema)
- [Managing your schema](#managing-your-schema)

*This documentation is still a WIP.*

### Configuring your schema

Each schema is identified by it's `name` and `version`.

Even though Datomic transactions are idempotent (so if you were to reinstall the schema's attributes every time on startup only changes would be transacted), Dation requires a version number so that you can have control over when schema transaction changes are run. This is useful, particularly, in cases when your schema change also involves a data migration.

Your schema expects the following properties:

* `name`: keyword identifier for the schema, used to track this specific schema's changes.
* `version`: number of current schema version. Manually increment the version to reinstall schema.
* `installs`: vector of schema attributes to install.
* `migrations`: (TODO) vector of data migrations to run.

### Managing your schema

* `ensure-ready`: ensure that schema attribute installs and data migrations are run.
* `attrs`: get a list of install attributes.

## License

MIT © [Alid Lorenzo](https://github.com/alidlo)
