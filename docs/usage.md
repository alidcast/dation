## Dation Usage

### Configuring your schema

Your schema expects the following properties:

* `name`: keyword identifier for the schema, used to track this specific schema's changes.
* `installs`: vector of schema attributes to install.

#### Declaring Attributes

* Similar to Datomic, all schema reader literals are namespaced with `db`.
* Datomic value types (e.g. `db.type/string`) are not shortened so as to not sacrifice semantic meaning.

List of available reader literals:

##### `db/attr`

Generic attribute shorthand. 

Expects vector of: `[ident ?doc constraints ?pred]`

The attributes `ident` keyword is always required, hence why its first.

An attributes `constraints` is a positional vector of built-in Datomic checks: `[type cardinality (?unique | ?isComponent)]`.

As the last argument you can optionally pass an attribute's predicate symbol(s).

Examples:

```clj
#db/attr [:user/email [:db.type/string :db.cardinality/one :db.unique/identity]]
;; {:db/ident       :user/username
;;  :db/valueType   :db.type/string
;;  :db/cardinality :db.cardinality/one
;;  :db/unique      :db.unique/identity}

#db/attr [:user/username "Unique email for a user."
          [:db.type/string :db.cardinality/one :db.unique/identity]
          db.fns.attr-preds/email?]
;; {:db/ident       :user/email
;;  :db/doc         "Unique email for a user."
;;  :db/valueType   :db.type/string
;;  :db/cardinality :db.cardinality/one
;;  :db/unique      :db.unique/identity
;;  :db.attr/preds 'db.fns.attr-preds/email?}
```

##### `db/spec`

Entity spec attribute shorthand. 

Expects vector of following format: `[ident req-attrs ?pred]`.

Require attributes `req-attrs` is a vector of attributes that must be present in transaction.

Predicate `pred` is a symbol referencing an entity's predicate function.

```clj
#db/spec [:doc.spec/owner-fk [:doc/owner] 'myapp.doc.preds/owner?]
;; {:db.ident :doc.spec/owner-fk
;;  :db.entity/attrs [:doc/owner]
;;  :db.entity/preds 'myapp.doc.preds/owner?}
```
