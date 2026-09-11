(ns marketentry.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.facts :as facts]))

(deftest mhl-has-spec-basis
  (let [sb (facts/spec-basis "MHL")]
    (is (some? sb))
    (is (string? (:provenance sb)))
    (is (seq (:required-evidence sb)))
    (is (some? (facts/rep-spec-basis "MHL")))
    (is (some? (facts/fibl-spec-basis "MHL")))
    (is (some? (facts/procurement-spec-basis "MHL")))))

(deftest unknown-jurisdiction-has-no-spec-basis
  (is (nil? (facts/spec-basis "PLW")))
  (is (nil? (facts/spec-basis "ZZZ"))))

(deftest required-evidence-satisfied
  (let [sb (facts/spec-basis "MHL")
        all (:required-evidence sb)]
    (is (true? (facts/required-evidence-satisfied? "MHL" all)))
    (is (not (facts/required-evidence-satisfied? "MHL" (take 1 all))))
    (is (nil? (facts/required-evidence-satisfied? "PLW" all)))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["MHL" "USA" "PLW"])]
    (is (= 3 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["PLW" "USA"] (:missing-jurisdictions c)))))

(deftest does-not-assert-mica-naming
  (testing "the catalog uses the safe IRI framing, never asserts MICA as current official name"
    (let [sb (facts/spec-basis "MHL")]
      (is (not (re-find #"(?i)\bMICA\b" (:owner-authority sb))))
      (is (re-find #"IRI" (:owner-authority sb))))))

(deftest procurement-basis-current-vs-repealed
  (testing "current basis (P.L. 2023-62) is recognized"
    (is (true? (facts/procurement-basis-current?
                "MHL" "Procurement Code Act, 2023 (P.L. 2023-62)"))))
  (testing "the repealed 1988 Code (44 MIRC 1) is NOT the current basis"
    (is (false? (facts/procurement-basis-current? "MHL" "1988 Procurement Code (44 MIRC 1)"))))
  (testing "an unknown jurisdiction has no basis to compare against"
    (is (nil? (facts/procurement-basis-current? "PLW" "anything")))))

(deftest cofa-never-grants-a-bidding-preference
  (testing "COFA grants NO procurement-bidding preference, for any claimed bidder nationality"
    (is (false? (facts/cofa-grants-bidding-preference? "US")))
    (is (false? (facts/cofa-grants-bidding-preference? "MH")))
    (is (false? (facts/cofa-grants-bidding-preference? "JP")))
    (is (false? (facts/cofa-grants-bidding-preference? nil)))))

(deftest does-not-enumerate-reserved-list-sectors
  (testing "FIBL spec-basis carries authority/legal-basis/provenance only, no sector list"
    (let [fb (facts/fibl-spec-basis "MHL")]
      (is (= #{:fibl-owner-authority :fibl-legal-basis :fibl-provenance} (set (keys fb)))))))
