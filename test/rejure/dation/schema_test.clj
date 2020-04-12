(ns rejure.dation.schema-test
  (:require [clojure.test :refer [deftest testing is]]
            [rejure.dation.schema :as dschema]))

(deftest reader-edn-test
  (let [read-edn dschema/read-edn]
    (testing "reads edn literals correctly"
      (testing "#ent converts shorthand vector to datomic attribute map"
        (testing "form: [ident type cardinality]"
          (is (= (read-edn "[#ent [:user/username :db.type/string :db.cardinality/one]]")
                 [{:db.ident       :user/username
                   :db.type        :db.type/string
                   :db.cardinality :db.cardinality/one}])))
        (testing "form: [ident type cardinality unique]"
          (is (= (read-edn "[#ent [:user/username :db.type/string :db.cardinality/one :db.unique/identity]]")
                 [{:db.ident       :user/username
                   :db.type        :db.type/string
                   :db.cardinality :db.cardinality/one
                   :db.unique      :db.unique/identity}])))
        (testing "form: {ident attrs-vector}"
          (is (= (read-edn "[#ent #:user{:username [:db.type/string :db.cardinality/one]}]")
                 [[{:db.ident       :user/username
                    :db.type        :db.type/string
                    :db.cardinality :db.cardinality/one}]])))
        (testing "form: {ident attrs-map}"
          (is (= (read-edn "#ent #:user{:username {:db.type :db.type/string
                                                      :db.cardinality :db.cardinality/one}}")


                 [{:db.ident       :user/username
                   :db.type        :db.type/string
                   :db.cardinality :db.cardinality/one}])))))))

