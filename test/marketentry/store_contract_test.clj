(ns marketentry.store-contract-test
  "MemStore ≡ DatomicStore parity for the Store protocol."
  (:require [clojure.test :refer [deftest is testing]]
            [marketentry.store :as store]
            [marketentry.registry :as registry]))

(defn- exercise [s]
  (store/commit-record! s {:effect :engagement/upsert
                           :value {:id "eng-x" :operator "X Co" :jurisdiction "MHL"
                                   :base-fee 100 :monthly-rate 10 :monitoring-months 1
                                   :claimed-fee 110.0
                                   :bids-domestically? true
                                   :has-domestic-license? true
                                   :has-offshore-registration? true
                                   :involves-foreign-investment? true
                                   :fibl-obtained? true
                                   :reserved-list-acknowledged? true
                                   :cited-procurement-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
                                   :gross-revenue-tax-acknowledged? true
                                   :claims-cofa-preference? false
                                   :bidder-nationality "MH"
                                   :drafted? false :submitted? false :status :intake}})
  (store/commit-record! s {:effect :assessment/set
                           :path ["eng-x"]
                           :payload {:jurisdiction "MHL" :checklist ["a"] :spec-basis "x"}})
  (store/commit-record! s {:effect :engagement/mark-drafted :path ["eng-x"]})
  (store/commit-record! s {:effect :engagement/mark-submitted :path ["eng-x"]})
  (store/append-ledger! s {:t :committed :op :test})
  {:engagement (store/engagement s "eng-x")
   :assessment (store/assessment-of s "eng-x")
   :drafts (store/draft-history s)
   :submits (store/submit-history s)
   :ledger (store/ledger s)
   :drafted? (store/engagement-already-drafted? s "eng-x")
   :submitted? (store/engagement-already-submitted? s "eng-x")})

(deftest mem-and-datomic-parity
  (let [mem (store/seed-db)
        dat (store/datomic-seed-db)
        ;; use empty stores for parity of exercised mutations
        mem* (store/->MemStore (atom {:engagements {} :assessments {} :ledger []
                                      :draft-sequences {} :draft-records []
                                      :submit-sequences {} :submit-records []}))
        dat* (store/datomic-store {})
        m (exercise mem*)
        d (exercise dat*)]
    (is (= (:operator (:engagement m)) (:operator (:engagement d))))
    (is (= (:has-domestic-license? (:engagement m)) (:has-domestic-license? (:engagement d))))
    (is (= (:cited-procurement-basis (:engagement m)) (:cited-procurement-basis (:engagement d))))
    (is (true? (:drafted? m)) (true? (:drafted? d)))
    (is (true? (:submitted? m)) (true? (:submitted? d)))
    (is (= 1 (count (:drafts m))) (= 1 (count (:drafts d))))
    (is (= 1 (count (:submits m))) (= 1 (count (:submits d))))
    (is (= 1 (count (:ledger m))) (= 1 (count (:ledger d))))
    (is (= (:assessment m) (:assessment d)))))
