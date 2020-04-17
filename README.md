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

1. The Datomic database schema serves is the source of truth (no DSL).
2. Make Datomic schema configurations explicit, taking advantage of EDN and reader literals.
3. Work well with the accretion-only model Datomic encourages.
4. Provide shorthands for declaring datomic attribute maps without sacrificing semantic meaning.
5. Work with Datomic Cloud (and optionally Datomic On-Prem).

## Status 

Alpha. Clojar has not been deployed.

## Usage 

- [Configuring your schema](#configuring-your-schema)
  - [Declaring Attributes](#declaring-attributes)
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

#### Declaring Attributes

List of available reader literals:

##### `attr`

Generic attribute shorthand. Excepts vector of format `[ident type cardinality (?unique | ?isComponent) ?pred]`.

When you're specifying an entities attributes you'll almost always want to specifiy its ident, type, and cardinality which is why this shorthand makes it easy to specifiy them as a vector.

For the fourth item, if `type` is not a reference (i.e. `:db.type/ref`) then it expects a `:db.unique` value, if it is a reference then it expects an optional `:db/isComponent` boolean.

Lastly, the fifth item is an optional predicate symbol (TODO).

```clj
#attr [:user/username :db.type/string :db.cardinality/one :db.unique/identity]
;; {:db/ident       :user/username
;;  :db/valueType   :db.type/string
;;  :db/cardinality :db.cardinality/one
;;  :db/unique      :db.unique/identity}
```

##### `ent` 

Entity attribute shorthand, useful when declaring multiple attributes for the same namespace. Excepts a map of attribute declarations. Works similar to `#attr` literal except the `ident` declarations are keys in a map.

```clj
#ent {:user/username [:db.type/string :db.cardinality/one :db.unique/identity]
      :user/email    [:db.type/string :db.cardinality/one :db.unique/identity]}
;; [{:db/ident       :user/username
;;  :db/valueType   :db.type/string
;;  :db/cardinality :db.cardinality/one
;;  :db/unique      :db.unique/identity}
;; {:db/ident       :user/email
;;  :db/valueType   :db.type/string
;;  :db/cardinality :db.cardinality/one
;;  :db/unique      :db.unique/identity}]
```

##### `spec`

// TODO, not yet ready for usage. 

Spec attribute shorthand. Expects vector of following format: `[ident req-attrs ?pred]`

```clj
#spec [:doc.spec/owner-fk [:doc/owner] 'myapp.doc.preds/owner?]
;; {:db.ident :doc.spec/owner-fk
;;  :db.entity/attrs [:doc/owner]
;;  :db.entity/preds 'myapp.doc.preds/owner?}
```

### Managing your schema

* `ensure-ready`: ensure that schema attribute installs and data migrations are run.
* `attrs`: get a list of install attributes.

## License

MIT © [Alid Lorenzo](https://github.com/alidlo)
