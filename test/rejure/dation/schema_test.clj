(ns rejure.dation.schema-test
  (:require [clojure.test :refer [deftest testing is]]
            [rejure.dation.schema :as dschema]))

(deftest reader-edn-test
  (let [read-edn dschema/read-edn]
    (testing "reads edn literals correctly"
      (testing "#attr converts shorthand vector to datomic attribute map"
        (testing "[ident, type, cardinality]"
          (is (= (read-edn "[#attr [:user/username :db.type/string :db.cardinality/one]]")
                 [{:db.ident       :user/username
                   :db.type        :db.type/string
                   :db.cardinality :db.cardinality/one}])))
        (testing "[ident, type, cardinality, unqiue]"
          (is (= (read-edn "[#attr [:user/username :db.type/string :db.cardinality/one :db.unique/identity]]")
                 [{:db.ident       :user/username
                   :db.type        :db.type/string
                   :db.cardinality :db.cardinality/one
                   :db.unique      :db.unique/identity}])))))))

