(ns culture.facts-test
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [culture.facts :as facts]))

(deftest mhl-has-culture-basis
  (let [sb (facts/spec-basis "MHL")]
    (is (= 5 (count sb)))
    (is (= (count sb) (count (set (map :culture/id sb)))))
    (is (every? #(str/starts-with? (:culture/url %) "https://") sb))
    (is (every? #(= "MHL" (:culture/country %)) sb))
    (is (every? #(nil? (:culture/municipality %)) sb))
    (is (every? #(seq (:culture/summary %)) sb))
    (is (every? #(string? (:culture/retrieved-at %)) sb))))

(deftest unknown-jurisdiction-has-no-basis
  (is (nil? (facts/spec-basis "FSM")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["MHL" "FSM"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["FSM"] (:missing-jurisdictions c)))))

(deftest by-kind-filters
  (is (= 3 (count (facts/by-kind "MHL" :dish))))
  (is (= ["mhl.craft.stick-chart"]
         (mapv :culture/id (facts/by-kind "MHL" :craft))))
  (is (empty? (facts/by-kind "MHL" :other)))
  (is (empty? (facts/by-kind "FSM" :dish))))

(deftest tx-file-matches-catalog
  (let [tx (edn/read-string (slurp "data/culture-tx.edn"))
        flat (mapcat val (sort-by key facts/catalog))]
    (is (= (vec flat) (vec tx)))))
