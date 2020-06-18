(ns dation.schema-test
  (:require [clojure.test :refer [deftest testing is] :as t]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [datomic.client.api :as d]
            [dation.db-dev :as db]
            [dation.schema :as ds]))

(deftest test-read-edn
  (let [test ds/read-edn]
    (testing "reads edn literals correctly"
      (testing "#attr converts shorthand vector to datomic attribute map"
        (testing "form: [ident type cardinality]"
          (is (= (test "[#attr [:user/username :db.type/string :db.cardinality/one]]")
                 [{:db/ident       :user/username
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one}])))
        (testing "form: [ident type cardinality unique]"
          (is (= (test "[#attr [:user/username :db.type/string :db.cardinality/one :db.unique/identity]]")
                 [{:db/ident       :user/username
                   :db/valueType   :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique      :db.unique/identity}])))
        ;; (testing "form: [ident type cardinality unique pred]"
        ;;   (is (= (test "[#attr [:user/username :db.type/string :db.cardinality/one :db.unique/identity foo]]")
        ;;          [{:db/ident       :user/username
        ;;            :db/valueType   :db.type/string
        ;;            :db/cardinality :db.cardinality/one
        ;;            :db/unique      :db.unique/identity
        ;;            :db.attr/preds  'foo}])))
        )
      (testing "#ent converts shorthand vector to datomic attribute map"
        (testing "form: {ident attrs-map}"
          (is (= (test "#ent #:user{:username {:db/valueType  :db.type/string
                                                   :db/cardinality :db.cardinality/one}}")
                 [{:db/ident       :user/username
                   :db/valueType  :db.type/string
                   :db/cardinality :db.cardinality/one}])))
        (testing "form: {ident attrs-vector}"
          (is (= (test "[#ent #:user{:username [:db.type/string :db.cardinality/one]}]")
                 [[{:db/ident       :user/username
                    :db/valueType  :db.type/string
                    :db/cardinality :db.cardinality/one}]])))))))

(defn reset-db! []
  ;; TODO issues with teardown, likely async issues
  ; (d/delete-database (db/client) {:db-name (db/ename :test)})
  (d/create-database (db/client) {:db-name (db/ename :test)}))

(defn schema-attrs "Gets user installed database attributes, filtering out fressian and dation namespaces."
  []
  (into [] 
        (filter
         (fn [m] (not-any? #(str/includes? (namespace (get m :db/ident)) %) ["fressian" "dation.schema"]))
         (ds/attrs (db/inst :test)))))

(deftest test-ensure-ready 
  (reset-db!)
  (let [schema (ds/read-edn (slurp (io/reader "lib/test/fixtures/schema.edn")))]
    (testing "installs attributes"
      (ds/ensure-ready (db/conn :test) schema)
      (is (= (map #(get % :ident) (schema-attrs))
             (map #(get % :ident) (first (:installs schema))))))

    (testing "checks installed attributes for given version"
      (testing "cond: same version"
        (is (= (ds/installed? (db/inst :test) :test-schema "1.0")
               true)))
      (testing "cond: greater version"
        (is (= (ds/installed? (db/inst :test) :test-schema "1.2")
               false)))
      (testing "cond: smaller version"
        (is (= (ds/installed? (db/inst :test) :test-schema "0.9")
               true))))))

