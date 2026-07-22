(ns marketentry.store
  "SSoT for the MHL market-entry compliance actor, behind a `Store`
  protocol so the backend is a swap, not a rewrite -- the same seam
  every prior cloud-itonami actor in this fleet uses.

    - `MemStore`     -- atom of EDN. The deterministic default for
                        dev/tests/demo (no deps).
    - `DatomicStore` -- backed by `langchain.db`, a Datomic-API-compatible
                        EAV store.

  Both implement the same protocol and pass the same contract
  (test/marketentry/store_contract_test.clj).

  The primary entity here is an `engagement` -- filing-draft and
  filing-submit actuation events apply SEQUENTIALLY to the SAME
  engagement record (draft first, submit later). Dedicated
  double-actuation-guard booleans (`:drafted?`/`:submitted?`, never a
  `:status` value).

  MHL's dual-track registration structure means an engagement carries
  BOTH `:has-domestic-license?` (Ministry-of-Finance domestic business
  license) and `:has-offshore-registration?` (IRI-administered RMI
  Corporate/Ships Registry) as independent booleans -- neither implies
  the other, and `marketentry.governor` verifies the correct track was
  actually obtained for what the engagement claims to do.

  The ledger stays append-only on every backend."
  (:require [marketentry.registry :as registry]
            [langchain.db :as d]
            [langchain-store.core :as ls]))

(defprotocol Store
  (engagement [s id])
  (all-engagements [s])
  (assessment-of [s engagement-id] "committed jurisdiction assessment, or nil")
  (ledger [s])
  (draft-history [s] "the append-only filing-draft history")
  (submit-history [s] "the append-only filing-submit history")
  (next-draft-sequence [s jurisdiction])
  (next-submit-sequence [s jurisdiction])
  (engagement-already-drafted? [s engagement-id])
  (engagement-already-submitted? [s engagement-id])
  (commit-record! [s record] "apply a committed op's record to the SSoT")
  (append-ledger! [s fact]   "append one immutable decision fact")
  (with-engagements [s engagements] "replace/seed the engagement directory"))

;; ----------------------------- demo data -----------------------------

(defn demo-data
  "A small, self-contained engagement set covering both actuation
  lifecycles (draft, submit) plus the governor's own new checks --
  one clean baseline plus one fixture per new check."
  []
  {:engagements
   {;; eng-1: clean baseline -- bids domestically WITH a domestic
    ;; license (not just offshore), foreign investment WITH FIBL +
    ;; reserved-list acknowledgement, current procurement legal basis,
    ;; gross-revenue-tax acknowledged, no fabricated COFA claim.
    "eng-1" {:id "eng-1" :operator "Kita Systems MH" :portal "RMI Ministry of Finance / IRI"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
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
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}
    ;; eng-2: unknown jurisdiction -- for the no-spec-basis fixture
    ;; (assess with :no-spec? true routes to "PLW", not on catalog).
    "eng-2" {:id "eng-2" :operator "Atlantis LLC" :portal "RMI Ministry of Finance / IRI"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :bids-domestically? true
             :has-domestic-license? true
             :has-offshore-registration? true
             :involves-foreign-investment? true
             :fibl-obtained? true
             :reserved-list-acknowledged? true
             :cited-procurement-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
             :gross-revenue-tax-acknowledged? true
             :claims-cofa-preference? false
             :bidder-nationality "US"
             :drafted? false :submitted? false
             :jurisdiction "PLW" :status :intake}
    ;; eng-3: engagement fee mismatch (claimed != base + months x rate).
    "eng-3" {:id "eng-3" :operator "Minami Systems" :portal "RMI Ministry of Finance / IRI"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 999000.0
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
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}
    ;; eng-4: FLAGSHIP fixture -- bids domestically, has ONLY an
    ;; offshore IRI registration, has NOT obtained the domestic
    ;; Ministry-of-Finance business license. Governor must HOLD this
    ;; regardless of the offshore registration being present.
    "eng-4" {:id "eng-4" :operator "Higashi Export" :portal "RMI Ministry of Finance / IRI"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :bids-domestically? true
             :has-domestic-license? false
             :has-offshore-registration? true
             :involves-foreign-investment? true
             :fibl-obtained? true
             :reserved-list-acknowledged? true
             :cited-procurement-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
             :gross-revenue-tax-acknowledged? true
             :claims-cofa-preference? false
             :bidder-nationality "US"
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}
    ;; eng-5: foreign investment involved, FIBL NOT obtained.
    "eng-5" {:id "eng-5" :operator "Nishi Logistics" :portal "RMI Ministry of Finance / IRI"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :bids-domestically? true
             :has-domestic-license? true
             :has-offshore-registration? true
             :involves-foreign-investment? true
             :fibl-obtained? false
             :reserved-list-acknowledged? false
             :cited-procurement-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
             :gross-revenue-tax-acknowledged? true
             :claims-cofa-preference? false
             :bidder-nationality "MH"
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}
    ;; eng-6: cites the REPEALED 1988 Procurement Code instead of the
    ;; current P.L. 2023-62.
    "eng-6" {:id "eng-6" :operator "Chuo Civic Tech" :portal "RMI Ministry of Finance / IRI"
             :base-fee 400000 :monthly-rate 25000 :monitoring-months 6
             :claimed-fee 550000.0
             :bids-domestically? true
             :has-domestic-license? true
             :has-offshore-registration? true
             :involves-foreign-investment? true
             :fibl-obtained? true
             :reserved-list-acknowledged? true
             :cited-procurement-basis "1988 Procurement Code (44 MIRC 1)"
             :gross-revenue-tax-acknowledged? true
             :claims-cofa-preference? false
             :bidder-nationality "MH"
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}
    ;; eng-7: operates domestically but has NOT acknowledged the
    ;; gross-revenue-tax obligation.
    "eng-7" {:id "eng-7" :operator "Marshall Freight Co" :portal "RMI Ministry of Finance / IRI"
             :base-fee 300000 :monthly-rate 20000 :monitoring-months 6
             :claimed-fee 420000.0
             :bids-domestically? true
             :has-domestic-license? true
             :has-offshore-registration? false
             :involves-foreign-investment? false
             :fibl-obtained? true
             :reserved-list-acknowledged? true
             :cited-procurement-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
             :gross-revenue-tax-acknowledged? false
             :claims-cofa-preference? false
             :bidder-nationality "MH"
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}
    ;; eng-8: claims a fabricated COFA procurement-bidding preference
    ;; for a US-affiliated bidder -- governor must reject the claim
    ;; regardless of the bidder's actual nationality.
    "eng-8" {:id "eng-8" :operator "Pacific Rim Contractors" :portal "RMI Ministry of Finance / IRI"
             :base-fee 500000 :monthly-rate 30000 :monitoring-months 12
             :claimed-fee 860000.0
             :bids-domestically? true
             :has-domestic-license? true
             :has-offshore-registration? true
             :involves-foreign-investment? true
             :fibl-obtained? true
             :reserved-list-acknowledged? true
             :cited-procurement-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
             :gross-revenue-tax-acknowledged? true
             :claims-cofa-preference? true
             :bidder-nationality "US"
             :drafted? false :submitted? false
             :jurisdiction "MHL" :status :intake}}})

;; ----------------------------- shared commit logic -----------------------------

(defn- draft-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-draft-sequence s (:jurisdiction e))
        result (registry/register-draft engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:drafted? true
                        :draft-number (get result "draft_number")}}))

(defn- submit-filing!
  [s engagement-id]
  (let [e (engagement s engagement-id)
        seq-n (next-submit-sequence s (:jurisdiction e))
        result (registry/register-submit engagement-id (:jurisdiction e) seq-n)]
    {:result result
     :engagement-patch {:submitted? true
                        :submit-number (get result "submit_number")}}))

;; ----------------------------- MemStore (default) -----------------------------

(defrecord MemStore [a]
  Store
  (engagement [_ id] (get-in @a [:engagements id]))
  (all-engagements [_] (sort-by :id (vals (:engagements @a))))
  (assessment-of [_ engagement-id] (get-in @a [:assessments engagement-id]))
  (ledger [_] (:ledger @a))
  (draft-history [_] (:draft-records @a))
  (submit-history [_] (:submit-records @a))
  (next-draft-sequence [_ jurisdiction] (get-in @a [:draft-sequences jurisdiction] 0))
  (next-submit-sequence [_ jurisdiction] (get-in @a [:submit-sequences jurisdiction] 0))
  (engagement-already-drafted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :drafted?])))
  (engagement-already-submitted? [_ engagement-id] (boolean (get-in @a [:engagements engagement-id :submitted?])))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (swap! a update-in [:engagements (:id value)] merge value)

      :assessment/set
      (swap! a assoc-in [:assessments (first path)] payload)

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:draft-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :draft-records registry/append result))))
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))]
        (swap! a (fn [state]
                   (-> state
                       (update-in [:submit-sequences jurisdiction] (fnil inc 0))
                       (update-in [:engagements engagement-id] merge engagement-patch)
                       (update :submit-records registry/append result))))
        result)
      nil)
    s)
  (append-ledger! [_ fact] (swap! a update :ledger conj fact) fact)
  (with-engagements [s engagements] (when (seq engagements) (swap! a assoc :engagements engagements)) s))

(defn seed-db
  "A MemStore seeded with the demo engagement set."
  []
  (->MemStore (atom (assoc (demo-data)
                           :assessments {}
                           :ledger [] :draft-sequences {} :draft-records []
                           :submit-sequences {} :submit-records []))))

;; ----------------------------- DatomicStore (langchain.db) -----------------------------

(def ^:private schema
  {:engagement/id                   {:db/unique :db.unique/identity}
   :assessment/engagement-id        {:db/unique :db.unique/identity}
   :ledger/seq                      {:db/unique :db.unique/identity}
   :draft-record/seq                {:db/unique :db.unique/identity}
   :submit-record/seq               {:db/unique :db.unique/identity}
   :draft-sequence/jurisdiction     {:db/unique :db.unique/identity}
   :submit-sequence/jurisdiction    {:db/unique :db.unique/identity}})

(defn- engagement->tx [{:keys [id operator portal base-fee monthly-rate monitoring-months claimed-fee
                               bids-domestically? has-domestic-license? has-offshore-registration?
                               involves-foreign-investment? fibl-obtained? reserved-list-acknowledged?
                               cited-procurement-basis gross-revenue-tax-acknowledged?
                               claims-cofa-preference? bidder-nationality
                               drafted? submitted?
                               jurisdiction status draft-number submit-number]}]
  (cond-> {:engagement/id id}
    operator                                    (assoc :engagement/operator operator)
    portal                                      (assoc :engagement/portal portal)
    base-fee                                    (assoc :engagement/base-fee base-fee)
    monthly-rate                                (assoc :engagement/monthly-rate monthly-rate)
    monitoring-months                           (assoc :engagement/monitoring-months monitoring-months)
    claimed-fee                                 (assoc :engagement/claimed-fee claimed-fee)
    (some? bids-domestically?)                  (assoc :engagement/bids-domestically? bids-domestically?)
    (some? has-domestic-license?)               (assoc :engagement/has-domestic-license? has-domestic-license?)
    (some? has-offshore-registration?)          (assoc :engagement/has-offshore-registration? has-offshore-registration?)
    (some? involves-foreign-investment?)        (assoc :engagement/involves-foreign-investment? involves-foreign-investment?)
    (some? fibl-obtained?)                      (assoc :engagement/fibl-obtained? fibl-obtained?)
    (some? reserved-list-acknowledged?)         (assoc :engagement/reserved-list-acknowledged? reserved-list-acknowledged?)
    cited-procurement-basis                     (assoc :engagement/cited-procurement-basis cited-procurement-basis)
    (some? gross-revenue-tax-acknowledged?)     (assoc :engagement/gross-revenue-tax-acknowledged? gross-revenue-tax-acknowledged?)
    (some? claims-cofa-preference?)             (assoc :engagement/claims-cofa-preference? claims-cofa-preference?)
    bidder-nationality                          (assoc :engagement/bidder-nationality bidder-nationality)
    (some? drafted?)                            (assoc :engagement/drafted? drafted?)
    (some? submitted?)                          (assoc :engagement/submitted? submitted?)
    jurisdiction                                (assoc :engagement/jurisdiction jurisdiction)
    status                                       (assoc :engagement/status status)
    draft-number                                 (assoc :engagement/draft-number draft-number)
    submit-number                                (assoc :engagement/submit-number submit-number)))

(def ^:private engagement-pull
  [:engagement/id :engagement/operator :engagement/portal :engagement/base-fee :engagement/monthly-rate
   :engagement/monitoring-months :engagement/claimed-fee
   :engagement/bids-domestically? :engagement/has-domestic-license? :engagement/has-offshore-registration?
   :engagement/involves-foreign-investment? :engagement/fibl-obtained? :engagement/reserved-list-acknowledged?
   :engagement/cited-procurement-basis :engagement/gross-revenue-tax-acknowledged?
   :engagement/claims-cofa-preference? :engagement/bidder-nationality
   :engagement/drafted? :engagement/submitted?
   :engagement/jurisdiction :engagement/status :engagement/draft-number :engagement/submit-number])

(defn- pull->engagement [m]
  (when (:engagement/id m)
    {:id (:engagement/id m) :operator (:engagement/operator m) :portal (:engagement/portal m)
     :base-fee (:engagement/base-fee m) :monthly-rate (:engagement/monthly-rate m)
     :monitoring-months (:engagement/monitoring-months m) :claimed-fee (:engagement/claimed-fee m)
     :bids-domestically? (boolean (:engagement/bids-domestically? m))
     :has-domestic-license? (boolean (:engagement/has-domestic-license? m))
     :has-offshore-registration? (boolean (:engagement/has-offshore-registration? m))
     :involves-foreign-investment? (boolean (:engagement/involves-foreign-investment? m))
     :fibl-obtained? (boolean (:engagement/fibl-obtained? m))
     :reserved-list-acknowledged? (boolean (:engagement/reserved-list-acknowledged? m))
     :cited-procurement-basis (:engagement/cited-procurement-basis m)
     :gross-revenue-tax-acknowledged? (boolean (:engagement/gross-revenue-tax-acknowledged? m))
     :claims-cofa-preference? (boolean (:engagement/claims-cofa-preference? m))
     :bidder-nationality (:engagement/bidder-nationality m)
     :drafted? (boolean (:engagement/drafted? m)) :submitted? (boolean (:engagement/submitted? m))
     :jurisdiction (:engagement/jurisdiction m) :status (:engagement/status m)
     :draft-number (:engagement/draft-number m) :submit-number (:engagement/submit-number m)}))

(defrecord DatomicStore [conn]
  Store
  (engagement [_ id]
    (pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id id])))
  (all-engagements [_]
    (->> (d/q '[:find [?id ...] :where [?e :engagement/id ?id]] (d/db conn))
         (map #(pull->engagement (d/pull (d/db conn) engagement-pull [:engagement/id %])))
         (sort-by :id)))
  (assessment-of [_ engagement-id]
    (ls/dec* (d/q '[:find ?p . :in $ ?eid
                   :where [?a :assessment/engagement-id ?eid] [?a :assessment/payload ?p]]
                 (d/db conn) engagement-id)))
  (ledger [_] (ls/read-stream conn :ledger/seq :ledger/fact))
  (draft-history [_] (ls/read-stream conn :draft-record/seq :draft-record/record))
  (submit-history [_] (ls/read-stream conn :submit-record/seq :submit-record/record))
  (next-draft-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :draft-sequence/jurisdiction ?j] [?e :draft-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (next-submit-sequence [_ jurisdiction]
    (or (d/q '[:find ?n . :in $ ?j
              :where [?e :submit-sequence/jurisdiction ?j] [?e :submit-sequence/next ?n]]
            (d/db conn) jurisdiction)
        0))
  (engagement-already-drafted? [s engagement-id]
    (boolean (:drafted? (engagement s engagement-id))))
  (engagement-already-submitted? [s engagement-id]
    (boolean (:submitted? (engagement s engagement-id))))
  (commit-record! [s {:keys [effect path value payload]}]
    (case effect
      :engagement/upsert
      (d/transact! conn [(engagement->tx value)])

      :assessment/set
      (d/transact! conn [{:assessment/engagement-id (first path) :assessment/payload (ls/enc payload)}])

      :engagement/mark-drafted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (draft-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-draft-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:draft-sequence/jurisdiction jurisdiction :draft-sequence/next next-n}
                      {:draft-record/seq (count (draft-history s)) :draft-record/record (ls/enc (get result "record"))}])
        result)

      :engagement/mark-submitted
      (let [engagement-id (first path)
            {:keys [result engagement-patch]} (submit-filing! s engagement-id)
            jurisdiction (:jurisdiction (engagement s engagement-id))
            next-n (inc (next-submit-sequence s jurisdiction))]
        (d/transact! conn
                     [(engagement->tx (assoc engagement-patch :id engagement-id))
                      {:submit-sequence/jurisdiction jurisdiction :submit-sequence/next next-n}
                      {:submit-record/seq (count (submit-history s)) :submit-record/record (ls/enc (get result "record"))}])
        result)
      nil)
    s)
  (append-ledger! [s fact]
    (ls/append-blob! conn :ledger/seq :ledger/fact (count (ledger s)) fact)
    fact)
  (with-engagements [s engagements]
    (when (seq engagements) (d/transact! conn (mapv engagement->tx (vals engagements)))) s))

(defn datomic-store
  ([] (datomic-store {}))
  ([{:keys [engagements]}]
   (let [s (->DatomicStore (d/create-conn schema))]
     (with-engagements s engagements))))

(defn datomic-seed-db
  []
  (datomic-store (demo-data)))
