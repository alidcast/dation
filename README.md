# Dation 

Motivation for [dation](https://github.com/alidlo/dation/tree/master/dation): 
* Standardize a succinct, declarative way to write Datomic schema using EDN.
  * Similar to Datomic, all schema reader literals are namespaced with `db`.
  * Datomic value types (e.g. `db.type/string`) are not shortened so as to not sacrifice semantic meaning.
* Provide utilities for managing schema attribute installs and data migrations.
* Work with Datomic Cloud (optionally, with On-Prem).

## Status 

Unreleased. 

## Usage

- [Configuring Schema](/docs/schema.md)

## License

MIT © [Alid Lorenzo](https://github.com/alidlo)
