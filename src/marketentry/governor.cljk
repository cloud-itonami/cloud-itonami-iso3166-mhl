(ns marketentry.governor
  "Market-Entry Compliance Governor -- the independent compliance layer
  that earns the MarketEntry-LLM the right to commit. The LLM has no
  notion of jurisdictional procurement law, whether a Marshall
  Islands engagement actually holds the RIGHT registration track for
  what it claims to do, whether a claimed engagement fee actually
  equals base + months x rate, whether a filing cites the CURRENT
  procurement legal basis, or when a draft stops being a draft and
  becomes a real-world portal submission, so this MUST be a separate
  system able to *reject* a proposal and fall back to HOLD.

  `:itonami.blueprint/governor` is `:market-entry-compliance-governor`
  (shared family keyword on blueprints).

  MHL's structure is genuinely unusual among this fleet's
  jurisdictions: it has TWO independent registration tracks --

    - `:offshore` -- the RMI Corporate and Ships Registries,
      statutorily the Office of the Registrar of Corporations but
      administered in practice by International Registries, Inc.
      (IRI), a private US-incorporated company (register.iri.com).
    - `:domestic` -- a general business license issued by the
      Ministry of Finance for any entity conducting business WITHIN
      RMI, including bidding on RMI public contracts.

  An engagement that only ever obtained the offshore IRI registration
  does NOT thereby satisfy the domestic requirement -- the single most
  important disambiguation for this actor, and the flagship check
  below.

  Ten checks, in priority order, ALL HARD violations: a human
  approver CANNOT override them. The confidence/actuation gate is
  SOFT: it asks a human to look (low confidence / actuation), and the
  human may approve -- but see `marketentry.phase`: for `:stake
  :actuation/draft-filing`/`:actuation/submit-filing` NO phase ever
  allows auto-commit either. Two independent layers agree that
  actuation is always a human call.

    1. Spec-basis                  -- did the jurisdiction proposal cite
                                       an OFFICIAL source
                                       (`marketentry.facts`), or invent
                                       one?
    2. Evidence incomplete         -- for `:filing/draft`/
                                       `:filing/submit`, has the
                                       jurisdiction actually been
                                       assessed with a full evidence
                                       checklist on file?
    3. Domestic-track missing      -- for `:filing/submit`, when the
                                       engagement declares
                                       `:bids-domestically? true`
                                       (claims to bid on a domestic RMI
                                       public contract), INDEPENDENTLY
                                       verify `:has-domestic-license?`
                                       is true. `:has-offshore-
                                       registration?` alone (IRI /
                                       register.iri.com) does NOT
                                       satisfy this -- a foreign entity
                                       that wants to operate/bid
                                       INSIDE RMI needs the domestic
                                       Ministry-of-Finance business
                                       license, not (only) an offshore
                                       corporate registration.
                                       FLAGSHIP genuinely new check for
                                       this vertical, grounded in the
                                       dual-track registry structure
                                       (Office of the Registrar of
                                       Corporations delegated to IRI
                                       for offshore vs. Ministry of
                                       Finance for domestic).
    4. FIBL missing                -- for `:filing/submit`, when the
                                       engagement declares
                                       `:involves-foreign-investment?
                                       true`, INDEPENDENTLY verify BOTH
                                       `:fibl-obtained?` (Foreign
                                       Investment Business License,
                                       Registrar of Foreign Investment,
                                       Office of the Attorney General,
                                       P.L. 2000-5) AND
                                       `:reserved-list-acknowledged?`
                                       -- a structural National-
                                       Reserved-List-awareness gate.
                                       This gate does NOT enumerate any
                                       specific Reserved List sector;
                                       only its existence/general scope
                                       is verified.
    5. Procurement-basis stale     -- for `:filing/submit`,
                                       INDEPENDENTLY verify the
                                       engagement's own
                                       `:cited-procurement-basis`
                                       equals the CURRENT legal basis
                                       (Procurement Code Act, 2023 --
                                       P.L. 2023-62). A filing citing
                                       the REPEALED 1988 Procurement
                                       Code (44 MIRC 1) is not citing
                                       current law.
    6. Gross-revenue-tax unacked   -- for `:filing/submit`, when the
                                       engagement operates domestically
                                       (`:has-domestic-license?
                                       true` or `:bids-domestically?
                                       true`), INDEPENDENTLY verify
                                       `:gross-revenue-tax-
                                       acknowledged?` is true. No
                                       specific rate figure is ever
                                       asserted by this check.
    7. Fabricated COFA preference  -- for `:filing/submit`,
                                       INDEPENDENTLY verify the
                                       engagement does NOT declare
                                       `:claims-cofa-preference?
                                       true`. COFA (Compact of Free
                                       Association) confers trade /
                                       currency / mobility benefits
                                       ONLY -- it creates NO
                                       procurement-bidding preference
                                       for US-affiliated bidders, so
                                       any engagement claiming one is
                                       asserting a false regulatory
                                       benefit regardless of the
                                       bidder's actual nationality.
                                       Encoding this verified negative
                                       is itself the honest, correctly
                                       scoped fact -- it prevents an
                                       LLM advisor from ever improvising
                                       a fabricated COFA preference.
    8. Engagement fee mismatch     -- for `:filing/submit`,
                                       INDEPENDENTLY recompute whether
                                       the engagement's own `:claimed-
                                       fee` equals `base-fee +
                                       monthly-rate x monitoring-
                                       months` -- honest reapplication
                                       of the ground-truth-recompute
                                       discipline sibling actors use.
    9. Confidence floor / actuation
       gate                          -- LLM confidence below threshold,
                                       OR the op is `:filing/draft`/
                                       `:filing/submit` (REAL acts)
                                       -> escalate.

  Two more guards, double-draft/double-submit prevention, are enforced
  off dedicated `:drafted?`/`:submitted?` facts (never a `:status`
  value)."
  (:require [marketentry.facts :as facts]
            [marketentry.registry :as registry]
            [marketentry.store :as store]))

(def confidence-floor 0.6)

(def high-stakes
  "Stakes grave enough to always require a human, even when clean.
  Drafting a real portal package and submitting a real portal
  registration are the two real-world actuation events this actor
  performs."
  #{:actuation/draft-filing :actuation/submit-filing})

;; ----------------------------- checks -----------------------------

(defn- spec-basis-violations
  "A `:jurisdiction/assess` (or `:filing/draft`/`:filing/submit`)
  proposal with no spec-basis citation is a HARD violation -- never
  invent a jurisdiction's market-entry requirements."
  [{:keys [op]} proposal]
  (when (contains? #{:jurisdiction/assess :filing/draft :filing/submit} op)
    (let [value (:value proposal)]
      (when (or (empty? (:cites proposal))
                (and (contains? value :spec-basis) (nil? (:spec-basis value))))
        [{:rule :no-spec-basis
          :detail "公式spec-basisの引用が無い提案は法域要件として扱えない"}]))))

(defn- evidence-incomplete-violations
  "For `:filing/draft`/`:filing/submit`, the jurisdiction's required
  registration evidence must actually be satisfied."
  [{:keys [op subject]} st]
  (when (contains? #{:filing/draft :filing/submit} op)
    (let [e (store/engagement st subject)
          assessment (store/assessment-of st subject)]
      (when-not (and assessment
                     (facts/required-evidence-satisfied?
                      (:jurisdiction e) (:checklist assessment)))
        [{:rule :evidence-incomplete
          :detail "法域の必要書類(Ministry of Finance営業許可/IRI登記/FIBL/代理人確認等)が充足していない状態での提案"}]))))

(defn- domestic-track-missing-violations
  "For `:filing/submit`, when the engagement declares
  `:bids-domestically? true`, INDEPENDENTLY verify
  `:has-domestic-license? true` -- the flagship genuinely new check
  this vertical adds. An offshore IRI registration
  (`:has-offshore-registration?`) does NOT satisfy this; the two
  tracks are independent."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:bids-domestically? e))
                 (not (true? (:has-domestic-license? e))))
        [{:rule :domestic-track-missing
          :detail (str subject " はRMI国内での入札を主張しているが、Ministry of Finance発行の"
                      "国内事業ライセンスが未取得(オフショアIRI登記の有無に関わらず) -- 提出提案は進められない")}]))))

(defn- fibl-missing-violations
  "For `:filing/submit`, when the engagement declares
  `:involves-foreign-investment? true`, INDEPENDENTLY check BOTH
  `:fibl-obtained?` AND `:reserved-list-acknowledged?` -- CONDITIONAL
  on the engagement's own ground truth. Never enumerates a specific
  Reserved List sector."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (true? (:involves-foreign-investment? e))
                 (not (and (true? (:fibl-obtained? e))
                          (true? (:reserved-list-acknowledged? e)))))
        [{:rule :fibl-missing
          :detail (str subject " は外国投資案件だがForeign Investment Business License(FIBL)取得"
                      "またはNational Reserved List確認が未完了 -- 提出提案は進められない")}]))))

(defn- procurement-basis-stale-violations
  "For `:filing/submit`, INDEPENDENTLY verify the engagement's own
  `:cited-procurement-basis` equals the CURRENT legal basis
  (Procurement Code Act, 2023 -- P.L. 2023-62), not the REPEALED 1988
  Procurement Code (44 MIRC 1)."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (facts/procurement-basis-current? (:jurisdiction e) (:cited-procurement-basis e))
        [{:rule :procurement-basis-stale
          :detail (str subject " は現行法(Procurement Code Act, 2023 -- P.L. 2023-62)ではなく"
                      "廃止済みの根拠(" (:cited-procurement-basis e) ")を引用している -- 提出提案は進められない")}]))))

(defn- gross-revenue-tax-unacked-violations
  "For `:filing/submit`, when the engagement operates domestically
  (`:has-domestic-license?` or `:bids-domestically?`), INDEPENDENTLY
  check `:gross-revenue-tax-acknowledged?`. No specific rate figure is
  ever asserted here."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (and (or (true? (:has-domestic-license? e)) (true? (:bids-domestically? e)))
                 (not (true? (:gross-revenue-tax-acknowledged? e))))
        [{:rule :gross-revenue-tax-unacknowledged
          :detail (str subject " はRMI国内事業者としてのgross-revenue-tax(総収入税)課税ベースの"
                      "認識確認が未完了 -- 提出提案は進められない")}]))))

(defn- fabricated-cofa-preference-violations
  "For `:filing/submit`, INDEPENDENTLY verify the engagement does NOT
  claim a COFA procurement-bidding preference. COFA confers
  trade/currency/mobility benefits only -- `facts/cofa-grants-bidding-
  preference?` always returns `false`, encoding that NO bidding
  preference for US-affiliated bidders exists, so this check rejects
  the claim regardless of `:bidder-nationality` (the boundary
  invariant under test in governor_contract_test.clj)."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when (or (true? (:claims-cofa-preference? e))
                (facts/cofa-grants-bidding-preference? (:bidder-nationality e)))
        [{:rule :cofa-preference-fabricated
          :detail (str subject " はCOFAによる入札優遇(bidder-nationality="
                      (:bidder-nationality e) ")を主張しているが、"
                      "COFAは貿易/通貨/移動の便益のみを付与し入札優遇は一切付与しない -- 提出提案は進められない")}]))))

(defn- engagement-fee-mismatch-violations
  "For `:filing/submit`, INDEPENDENTLY recompute whether the
  engagement's own claimed fee equals base + months x rate."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (let [e (store/engagement st subject)]
      (when-not (registry/engagement-fee-matches-claim? e)
        [{:rule :engagement-fee-mismatch
          :detail (str subject " の申告手数料(" (:claimed-fee e)
                      ")が独立再計算値(" (registry/compute-engagement-fee e) ")と一致しない")}]))))

(defn- already-drafted-violations
  "For `:filing/draft`, refuses to draft the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/draft)
    (when (store/engagement-already-drafted? st subject)
      [{:rule :already-drafted
        :detail (str subject " は既にドラフト済み")}])))

(defn- already-submitted-violations
  "For `:filing/submit`, refuses to submit the SAME engagement twice."
  [{:keys [op subject]} st]
  (when (= op :filing/submit)
    (when (store/engagement-already-submitted? st subject)
      [{:rule :already-submitted
        :detail (str subject " は既に提出済み")}])))

(defn check
  "Censors a MarketEntry-LLM proposal against the governor rules.
  Returns {:ok? bool :violations [..] :confidence c :escalate? bool
  :high-stakes? bool :hard? bool}."
  [request _context proposal st]
  (let [hard (into []
                   (concat (spec-basis-violations request proposal)
                           (evidence-incomplete-violations request st)
                           (domestic-track-missing-violations request st)
                           (fibl-missing-violations request st)
                           (procurement-basis-stale-violations request st)
                           (gross-revenue-tax-unacked-violations request st)
                           (fabricated-cofa-preference-violations request st)
                           (engagement-fee-mismatch-violations request st)
                           (already-drafted-violations request st)
                           (already-submitted-violations request st)))
        conf (:confidence proposal 0.0)
        low? (< conf confidence-floor)
        stakes? (boolean (high-stakes (:stake proposal)))
        hard? (boolean (seq hard))]
    {:ok?          (and (not hard?) (not low?) (not stakes?))
     :violations   hard
     :confidence   conf
     :hard?        hard?
     :escalate?    (and (not hard?) (or low? stakes?))
     :high-stakes? stakes?}))

(defn hold-fact
  "The audit fact written when a proposal is rejected (HOLD)."
  [request context verdict]
  {:t          :governor-hold
   :op         (:op request)
   :actor      (:actor-id context)
   :subject    (:subject request)
   :disposition :hold
   :basis      (mapv :rule (:violations verdict))
   :violations (:violations verdict)
   :confidence (:confidence verdict)})
