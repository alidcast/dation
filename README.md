# Dation (alpha)

Motivation for [dation](https://github.com/alidlo/dation/tree/master/dation): 
* Standardize a succinct, declarative way to write Datomic schema using EDN.
  * Similar to Datomic, all schema reader literals are namespaced with `db`.
  * Datomic value types (e.g. `db.type/string`) are not shortened so as to not sacrifice semantic meaning.
* Provide utilities for managing schema attribute installs and data migrations.
* Work with Datomic Cloud (optionally, with On-Prem).

Example: 

```clj 
;; db/schema.edn
;; ~ declare schema attribute installs 
{:name :app-schema 
 :installs 
  [#db/ent #:user{:username  [:db.type/string :db.cardinality/one :db.unique/identity]
                  :email     [:db.type/string :db.cardinality/one :db.unique/identity db.preds.attrs/email?]
                  :password  [:db.type/string :db.cardinality/one]}} 
;; db/client.clj 
(require '[dation.schema :as ds])
(def schema (ds/read-edn (slurp (io/reader "db/schema.edn"))))

(comment 
  ;; Make sure attributes are installed.
  (ds/ensure-ready conn schema)

  ;; Get all installed attributes
  (ds/attrs conn schema))
```

## Usage 

See respective projects README for usage.


## License

MIT © [Alid Lorenzo](https://github.com/alidlo)
