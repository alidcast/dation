# Dation 

- [Configuring your schema](#configuring-your-schema)
  - [Declaring Attributes](#declaring-attributes)
- [Managing your schema](#managing-your-schema)

*This documentation is still a WIP.*

### Configuring your schema

Your schema expects the following properties:

* `name`: keyword identifier for the schema, used to track this specific schema's changes.
* `installs`: vector of schema attributes to install.

#### Declaring Attributes

List of available reader literals:

##### `db/attr`

Generic attribute shorthand. Excepts vector of format `[ident type cardinality (?unique | ?isComponent) ?pred]`.

When you're specifying an entities attributes you'll almost always want to specifiy its ident, type, and cardinality which is why this shorthand makes it easy to specifiy them as a vector.

For the fourth item, if `type` is not a reference (i.e. `:db.type/ref`) then it expects a `:db.unique` value, if it is a reference then it expects an optional `:db/isComponent` boolean.

Lastly, the fifth item is an optional predicate symbol (TODO).

```clj
#db/attr [:user/username :db.type/string :db.cardinality/one :db.unique/identity]
;; {:db/ident       :user/username
;;  :db/valueType   :db.type/string
;;  :db/cardinality :db.cardinality/one
;;  :db/unique      :db.unique/identity}
```

##### `db/ent` 

Entity attribute shorthand, useful when declaring multiple attributes for the same namespace. Excepts a map of attribute declarations. Works similar to `#attr` literal except the `ident` declarations are keys in a map.

```clj
#db/ent {:user/username [:db.type/string :db.cardinality/one :db.unique/identity]
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

##### `db/spec`

// TODO, not yet ready for usage. 

Spec attribute shorthand. Expects vector of following format: `[ident req-attrs ?pred]`

```clj
#db/spec [:doc.spec/owner-fk [:doc/owner] 'myapp.doc.preds/owner?]
;; {:db.ident :doc.spec/owner-fk
;;  :db.entity/attrs [:doc/owner]
;;  :db.entity/preds 'myapp.doc.preds/owner?}
```

### Managing your schema

* `ensure-ready`: ensure that schema attribute installs and data migrations are run.
* `attrs`: get a list of install attributes.
