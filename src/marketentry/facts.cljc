(ns marketentry.facts
  "Marshall Islands (MHL) market-entry catalog.

  MHL has a genuinely unusual structure among this fleet's
  jurisdictions: TWO independent registration tracks, not one, and an
  engagement must be routed to the correct track (or both) rather than
  assumed to need whichever track is more familiar:

    - `:offshore` -- the RMI Corporate and Ships Registries, statutorily
      the Office of the Registrar of Corporations but administered in
      practice by International Registries, Inc. (IRI), a private
      US-incorporated company (public search: register.iri.com). Covers
      Non-Resident Domestic Corporations (NRDCs), LLCs, limited
      partnerships, Foreign Maritime Entities and the ships registry.
      This track is for entities that do NOT conduct business within
      RMI.
    - `:domestic` -- a general domestic business license issued by the
      Ministry of Finance for any entity or individual conducting
      business WITHIN RMI (locally-facing activity, including bidding
      on RMI public contracts), typically issued within ~7 days.

  A foreign entity that wants to actually operate or bid on a contract
  INSIDE RMI needs the `:domestic` Ministry-of-Finance business
  license -- an `:offshore` IRI registration alone does NOT satisfy
  that. This distinction is `marketentry.governor`'s flagship new
  check for this vertical.

  DO NOT assert \"MICA\" as the current official name of the corporate/
  ships registry administrator -- evidence points to IRI (International
  Registries, Inc.), with \"MIACA\" also appearing as a related/legacy
  name; exact current official branding is unresolved, so this catalog
  uses the safe framing \"IRI (International Registries, Inc.),
  administering the RMI Corporate and Ships Registries\" throughout.

  Sources actually fetched and read:
    https://www.register-iri.com/corporate/
    https://www.rmiparliament.org/cms/library/public-laws-by-years/19-2000.html
    https://rmiparliament.org/cms/library/public-laws-by-years/51-public-laws-by-years-2023.html

  Like every sibling actor's catalog, an item not in `catalog` has NO
  spec-basis -- never fabricate one. Specific numeric procurement
  thresholds under P.L. 2023-62, specific Foreign Investment Reserved
  List sectors, and any specific gross-revenue-tax rate figure are
  deliberately NOT enumerated here -- their existence is verified but
  their specific values are not, so this catalog only asserts the
  structural facts that ARE verified.")

(def catalog
  {"MHL" {:name "Marshall Islands"
          :owner-authority "Ministry of Finance (domestic business license) / IRI (International Registries, Inc.), administering the RMI Corporate and Ships Registries (offshore)"
          :legal-basis "Procurement Code Act, 2023 (P.L. 2023-62), effective 10 April 2023 -- repealed the 1988 Procurement Code (formerly 44 MIRC 1)"
          :national-spec "Dual-track registration: domestic Ministry-of-Finance business license vs. IRI-administered offshore corporate/ships registry (register.iri.com)"
          :provenance "https://www.register-iri.com/corporate/"
          :required-evidence ["Domestic business license record (Ministry of Finance, for entities operating/bidding within RMI)"
                              "IRI corporate/ships registry record (for non-resident offshore entities, register.iri.com)"
                              "Foreign Investment Business License (FIBL) record, where foreign investment is involved"
                              "Authorized-representative record"]
          :rep-owner-authority "Ministry of Finance / contracting authorities under the Procurement Code Act, 2023"
          :rep-legal-basis "Domestic business license (Ministry of Finance) typically required for any entity conducting business within RMI, including bidding on public contracts"
          :rep-provenance "https://www.rmiparliament.org/cms/library/public-laws-by-years/19-2000.html"
          ;; This vertical's own registration-number concept is deliberately
          ;; NOT modeled as a single "corporate number" field the way other
          ;; jurisdictions in this fleet are -- MHL's dual-track structure
          ;; means there is no one registry-of-record number; see
          ;; `track-spec-basis` below instead.
          :fibl-owner-authority "Registrar of Foreign Investment, Office of the Attorney General"
          :fibl-legal-basis "Foreign Investment Business License (Amendment) Act, 2000 (P.L. 2000-5) -- established the Registrar of Foreign Investment and a Cabinet-approved National Reserved List of sectors closed/restricted to foreign investment"
          :fibl-provenance "https://www.rmiparliament.org/cms/library/public-laws-by-years/19-2000.html"
          ;; Canonical citation string -- MUST match exactly what a
          ;; `:cited-procurement-basis` engagement field is expected to
          ;; contain for `procurement-basis-current?` to accept it (see
          ;; `marketentry.store/demo-data` eng-1 and
          ;; test/marketentry/facts_test.clj). Effective-date/repeal
          ;; prose lives in the top-level `:legal-basis` field instead,
          ;; not here, so this stays a stable exact-match citation.
          :procurement-legal-basis "Procurement Code Act, 2023 (P.L. 2023-62)"
          :procurement-repealed-basis "1988 Procurement Code (44 MIRC 1) -- REPEALED, not current law"
          :procurement-provenance "https://rmiparliament.org/cms/library/public-laws-by-years/51-public-laws-by-years-2023.html"
          :tax-basis "Domestic businesses operating within RMI are subject to a gross-revenue tax (not net-income tax), administered by the Ministry of Finance -- no corporate income tax for non-resident entities registered under the international corporate/maritime registry that conduct no business within RMI. Specific rate figure not modeled here (flagged as needing revalidation if ever cited)."
          :cofa-note "COFA (Compact of Free Association Act of 1985, effective 21 Oct 1986) confers customs/duty-free treatment for RMI-origin goods entering the US, MFN-equivalent treatment for US goods entering RMI, and USD as RMI's official legal tender -- trade/currency/mobility facts only. COFA does NOT create any procurement-bidding preference for US persons/companies; see `cofa-grants-bidding-preference?`."}})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s) missing (remove catalog iso3s)]
     {:requested (count iso3s) :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note "R0 catalog seed"})))

(defn required-evidence-satisfied? [iso3 submitted]
  (when-let [{:keys [required-evidence]} (spec-basis iso3)]
    (= (count required-evidence) (count (filter (set submitted) required-evidence)))))

(defn evidence-checklist [iso3] (:required-evidence (spec-basis iso3) []))

(defn rep-spec-basis [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:rep-owner-authority sb)
      (select-keys sb [:rep-owner-authority :rep-legal-basis :rep-provenance]))))

(defn fibl-spec-basis
  "Foreign Investment Business License spec-basis -- Registrar of
  Foreign Investment, Office of the Attorney General, per the Foreign
  Investment Business License (Amendment) Act, 2000 (P.L. 2000-5)."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (when (:fibl-owner-authority sb)
      (select-keys sb [:fibl-owner-authority :fibl-legal-basis :fibl-provenance]))))

(defn procurement-spec-basis
  "Current public-procurement legal basis -- Procurement Code Act, 2023
  (P.L. 2023-62). Does NOT provide the repealed basis as a valid
  citation; see `procurement-basis-current?`."
  [iso3]
  (when-let [sb (spec-basis iso3)]
    (select-keys sb [:procurement-legal-basis :procurement-provenance])))

(defn procurement-basis-current?
  "Is `cited-basis` the CURRENT procurement legal basis for `iso3`
  (P.L. 2023-62), as opposed to the repealed 1988 Procurement Code (44
  MIRC 1)? An engagement citing the repealed code is not citing current
  law."
  [iso3 cited-basis]
  (when-let [sb (spec-basis iso3)]
    (= cited-basis (:procurement-legal-basis sb))))

(defn cofa-grants-bidding-preference?
  "Does COFA (Compact of Free Association) create a procurement-bidding
  preference for US-affiliated bidders in RMI? NO -- this was
  explicitly investigated and found unverified/likely nonexistent.
  COFA's confirmed business-relevant provisions are customs/duty-free
  treatment for RMI-origin goods entering the US, MFN-equivalent
  treatment for US goods entering RMI, and USD as RMI's legal tender --
  trade/currency/mobility facts, not a bidding preference. This
  function always returns `false`, regardless of bidder nationality,
  by design: encoding the verified negative here (rather than letting
  an LLM advisor improvise a fabricated COFA preference) is itself the
  honest, correctly-scoped fact."
  [_bidder-nationality]
  false)
