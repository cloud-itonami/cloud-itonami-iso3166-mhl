(ns statute.facts-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [statute.facts :as facts]))

(deftest mhl-has-spec-basis
  (let [sb (facts/spec-basis "MHL")]
    (is (= 4 (count sb)))
    (is (every? #(str/starts-with? (:statute/url %) "https://") sb))
    (is (every? :statute/law-number sb))
    (is (every? #(= "MHL" (:statute/jurisdiction %)) sb))
    (is (= (count sb) (count (set (map :statute/id sb)))))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "ATL")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["MHL" "FSM" "ATL"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["ATL" "FSM"] (:missing-jurisdictions c)))))

(deftest by-topic-filters
  (is (= ["mhl.title52-ch1-business-corporations-act-1990"]
         (mapv :statute/id (facts/by-topic "MHL" :corporate-governance))))
  (is (= #{"mhl.title16-ch4-minimum-wage-act-1986"
           "mhl.title16-ch1-labor-non-resident-workers-act-2018"}
         (set (mapv :statute/id (facts/by-topic "MHL" :labor)))))
  (is (= ["mhl.title48-ch1-income-tax-act-1989"]
         (mapv :statute/id (facts/by-topic "MHL" :tax))))
  (is (empty? (facts/by-topic "MHL" :data-protection))
      "no data-protection statute located this iteration -- honestly absent")
  (is (empty? (facts/by-topic "ATL" :labor))))

(deftest does-not-reintroduce-mica
  "marketentry.facts explicitly warns not to assert \"MICA\" as the
  current official name of the corporate/ships registry administrator.
  statute.facts must stay consistent with that -- no entry's law-number
  or title may assert MICA as a current name."
  (let [sb (facts/spec-basis "MHL")]
    (is (not-any? #(re-find #"(?i)\bMICA\b" (str (:statute/title %) " " (:statute/law-number %)))
                  sb))))
