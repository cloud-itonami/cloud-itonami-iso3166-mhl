(ns statute.facts
  "General-law compliance catalog for the Marshall Islands (MHL) --
  extends this repo's existing `marketentry.facts` (public-procurement
  market-entry only, narrow scope) with a second, orthogonal catalog of
  statutes a company operating in this jurisdiction must generally
  track for compliance. Mirrors cloud-itonami-iso3166-jpn/-deu/-bgr/
  -fsm/-plw/-slb/-kir's `statute.facts` (ADR-2607141700,
  cloud-itonami-compliance-fact-federation).

  Every entry cites an OFFICIAL government-hosted document -- the
  Nitijela of the Republic of the Marshall Islands' own live 'Acts of
  Nitijela' legislation database (`rmiparliament.org`), which this
  iteration fetched and read directly with no Cloudflare/bot-detection
  wall encountered (unlike several Pacific siblings' experience with
  `paclii.org`, which required routing through a web.archive.org
  snapshot instead) -- never fabricated.

  READ THIS FIRST if you have not already: `marketentry.facts` (this
  repo) documents MHL's genuinely unusual TWO-TRACK registration
  structure and explicitly warns not to assert \"MICA\" as the current
  official name of the corporate/ships registry administrator. This
  namespace does NOT contradict that: it is the same statute, read
  further, not a second competing framing.

  - Company/business-associations law: MHL's own corporate-law
    instrument is Title 52, ASSOCIATIONS LAW, Part I -- the BUSINESS
    CORPORATIONS ACT (own short title, §1; P.L. 1990-91, commencement
    13 September 1990, per rmiparliament.org's own official
    consolidated text, version 9, amended through P.L. 2025-29 -- the
    Nitijela's own database, so more current than the courtesy copy
    `marketentry.facts` cites at register-iri.com, which is itself
    explicitly marked \"not the official government codification ...
    provided as a convenience by the publisher for ease of reference
    only\"; this iteration cross-read both and the section numbering
    and §1/§2 text match verbatim). This is the SAME Act that operates
    BOTH tracks `marketentry.facts` already distinguishes -- it is not
    an offshore-only instrument: own §4 'Registrars of Corporations;
    establishment and duties' (1) 'There are herewith established two
    (2) Registrars of Corporations: a Registrar of Corporations
    responsible for resident domestic and authorized foreign
    corporations, and a Registrar of Corporations responsible for
    non-resident corporations, partnerships, limited partnerships,
    limited liability companies, unincorporated associations, foreign
    maritime entities and other entities ... which shall be appointed
    by the Cabinet,' and (3) 'The Registrar of Corporations responsible
    for resident domestic and qualified foreign corporations shall be
    appointed by the Cabinet. The Registrar of Corporations responsible
    for non-resident corporations, foreign maritime entities and the
    name index shall be the Trust Company.' Own §2(p) defines: '\"Trust
    Company\" means The Trust Company of the Marshall Islands, Inc.'
    This CONFIRMS, from the Act's own text, the correct entity name for
    the non-resident/offshore track's registrar -- 'The Trust Company
    of the Marshall Islands, Inc.', consistent with (and more precise
    than) `marketentry.facts`'s hedged 'IRI (International Registries,
    Inc.), administering the RMI Corporate and Ships Registries'
    framing (IRI operates register-iri.com and publishes this Act as
    'published by ... the Republic of the Marshall Islands Registrar of
    Non-resident Domestic Corporations ... The Trust Company of the
    Marshall Islands, Inc.'). Neither reading supports \"MICA\" as a
    current name; this namespace does not reintroduce it.
  - Labor law: MHL Title 16 is 'LABOR AND INDUSTRIAL RELATIONS' and has
    several chapters; this iteration cites TWO, each independently
    fetched and read directly from rmiparliament.org's own legislation
    database:
    1. Chapter 4, the MINIMUM WAGE ACT 1986 (own short title, §401;
       P.L. 1986-15, commencement 6 March 1986; amended by P.L.
       1989-38, 1995-121, 1996-11, 2016-12, 2018-77 and, most recently,
       P.L. 2024-13) -- a general wage floor for EVERY employee of the
       Government of the Marshall Islands or any private employer (own
       §403(1), as amended), currently a scheduled increase: US$4.00/
       hour effective 1 October 2024, rising to $4.50 (1 Oct 2025),
       $5.00 (1 Oct 2026) and $5.25 (1 Oct 2027). Enforcement is before
       the High Court (own §404); the Secretary of Finance may request
       suspension of a violator's local business license, and the
       Minister of Resources and Development promulgates regulations
       for the trainee/apprentice exemption (own §405).
    2. Chapter 1, the LABOR (NON-RESIDENT WORKERS) ACT 2018 (P.L.
       2018-68, commencement 15 March 2018, further amended per P.L.
       2025-42; repealed and replaced the 2006 Act of the same name and
       moved the Division of Labor from the Ministry of Foreign Affairs
       & Trade to the Ministry of Justice, Immigration & Labor per its
       own 'AN ACT' preamble) -- own Part II establishes the Labor
       Division WITHIN THE MINISTRY OF JUSTICE (§104(1)) headed by a
       Director (§104(2)/§106), the general labor-administration
       authority for this jurisdiction, plus the work-permit/visa/quota/
       deportation regime for non-resident workers. Own §103 'Rights of
       citizens of the Republic of the Marshall Islands protected': '(1)
       For the purposes of this Chapter, every citizen of the Republic
       has, by virtue of that citizenship, the right to be employed in
       the Republic at any time. (2) Nothing in this Chapter shall
       abrogate the right declared in subsection (1) ...' -- an
       unconditional citizen employment right, a genuinely different
       shape from FSM/PLW's citizen-worker-PREFERENCE framing, reported
       honestly rather than forced into that shape. Honest textual
       quirk preserved, not smoothed over: the Act's own title page
       reads 'LABOR (NON-RESIDENT WORKERS) ACT, 2018' but its own §101
       short-title clause instead reads 'This Chapter may be cited as
       the Labor (Non-Resident Workers) Act, 2017' -- both years appear
       in the Act's own current consolidated text.
  - Tax law: MHL Title 48 is 'TAXATION'; Chapter 1 is the INCOME TAX ACT
    1989 (own short title, §101; P.L. 1989-50, commencement 14 April
    1989; amended by many subsequent P.L.s through P.L. 2025-34 per the
    Act's own title page). This grounds, with an actual citation,
    `marketentry.facts`'s own previously-uncited `:tax-basis` claim:
    own Part III 'TAX ON GROSS REVENUE' (§109 'Tax on Gross Revenues')
    confirms the gross-revenue (not net-income) character of the
    general business tax, administered by the Secretary of Finance /
    Department of Finance (own §125 'Assessment by Secretary of
    Finance'). Own Part VI 'NON-RESIDENT INCOME TAX' (§117, §120A) is a
    NARROWER, SEPARATE withholding tax on fees paid BY an RMI client TO
    a non-resident person for services rendered -- this iteration did
    NOT locate a blanket exemption clause naming non-resident/offshore
    (NRDC) entities specifically; the apparent reason an NRDC that
    conducts no business within RMI owes no MHL gross-revenue tax is
    territorial scope (Part III taxes business \"carried on ... in the
    Republic,\" and the Associations Law's own §2(q) already carves out
    an NRDC's purely administrative/registered-agent presence from
    counting as \"doing business in the Republic\"), not a located
    explicit non-resident corporate-tax exemption -- an honest gap,
    not asserted as independently confirmed.
  - CURRENCY CAVEAT: every rmiparliament.org PDF cited here is that
    site's own current consolidated-version file (fetched directly this
    session); the Business Corporations Act citation additionally
    cross-reads register-iri.com's courtesy copy (through the 41st
    Constitutional Regular Session, 2020) purely as corroboration --
    the rmiparliament.org version (through P.L. 2025-29) is newer and is
    the one cited as `:statute/url`.

  A law not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "iso3 -> vector of statute entries. `:statute/url` + `:statute/law-number`
  are the citation the governor requires before any compliance-fact
  proposal referencing this law can commit. MHL's catalog has 4 entries
  -- company law, two labor-law statutes (general minimum wage +
  non-resident-worker/Labor-Division administration), and income tax --
  each independently confirmed this iteration from rmiparliament.org,
  the Nitijela of the Republic of the Marshall Islands' own official
  legislation database."
  {"MHL"
   [{:statute/id "mhl.title52-ch1-business-corporations-act-1990"
     :statute/title "Business Corporations Act 1990, Marshall Islands Revised Code (MIRC) Title 52 (Associations Law), Chapter 1"
     :statute/jurisdiction "MHL"
     :statute/kind :law
     :statute/law-number "P.L. 1990-91 (own short title, §1: 'This Act shall be known and may be cited as the \"Associations Law.\" Part I of this title shall be known as the Business Corporations Act.'); amended by P.L. 1990-93, 1991-129, 1997-34, 1997-52, 1998-73, 2000-18, 2005-27, 2006-52, 2009-15, 2010-35, 2014-31, 2015-40, 2017-39, 2017-52, 2018-67, 2018-100, 2019-105, 2020-27 and 2025-29 (own title-page amendment list, rmiparliament.org's official consolidated version 9). Own §4 establishes TWO Registrars of Corporations -- a Cabinet-appointed Registrar for resident domestic/authorized foreign corporations, and 'the Trust Company' (own §2(p): The Trust Company of the Marshall Islands, Inc.) for non-resident corporations/partnerships/LLCs/foreign maritime entities -- the same dual-track split `marketentry.facts` documents structurally, now grounded in the Act's own text."
     :statute/url "https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/1990/1990-0091/1990-0091_9.pdf"
     :statute/url-provenance :official-rmi-nitijela-legislation-database
     :statute/enacted-date "1990-09-13"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:corporate-governance :incorporation}}
    {:statute/id "mhl.title16-ch4-minimum-wage-act-1986"
     :statute/title "Minimum Wage Act 1986, Marshall Islands Revised Code (MIRC) Title 16 (Labor and Industrial Relations), Chapter 4"
     :statute/jurisdiction "MHL"
     :statute/kind :law
     :statute/law-number "P.L. 1986-15 (own short title, §401); amended by P.L. 1989-38, 1995-121, 1996-11, 2016-12, 2018-77 and 2024-13 (own title-page amendment list). Own §403(1), as amended by P.L. 2024-13: minimum wage of US$4.00/hour effective 1 October 2024, rising to $4.50 (1 Oct 2025), $5.00 (1 Oct 2026) and $5.25 (1 Oct 2027), for every employee of the Government of the Marshall Islands or any private employer."
     :statute/url "https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/1986/1986-0015/1986-0015_4.pdf"
     :statute/url-provenance :official-rmi-nitijela-legislation-database
     :statute/enacted-date "1986-03-06"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:labor}}
    {:statute/id "mhl.title16-ch1-labor-non-resident-workers-act-2018"
     :statute/title "Labor (Non-Resident Workers) Act 2018, Marshall Islands Revised Code (MIRC) Title 16 (Labor and Industrial Relations), Chapter 1"
     :statute/jurisdiction "MHL"
     :statute/kind :law
     :statute/law-number "P.L. 2018-68, commencement 15 March 2018 (own title page), further amended per P.L. 2025-42 (own title-page note); repealed and replaced the Labor (Non-Resident Workers) Act 2006. Own §101 short-title clause reads 'Labor (Non-Resident Workers) Act, 2017' -- a discrepancy from the title page's own '2018' preserved honestly, not resolved by guessing which controls. Own Part II (§§104-107) establishes the Labor Division within the Ministry of Justice, headed by a Director, as the general labor-administration authority; own §103 declares an unconditional right of every citizen of the Republic to be employed in the Republic at any time."
     :statute/url "https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/2018/2018-0068/2018-0068_2.pdf"
     :statute/url-provenance :official-rmi-nitijela-legislation-database
     :statute/enacted-date "2018-03-15"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:labor}}
    {:statute/id "mhl.title48-ch1-income-tax-act-1989"
     :statute/title "Income Tax Act 1989, Marshall Islands Revised Code (MIRC) Title 48 (Taxation), Chapter 1"
     :statute/jurisdiction "MHL"
     :statute/kind :law
     :statute/law-number "P.L. 1989-50, commencement 14 April 1989 (own title page); amended by many subsequent P.L.s through P.L. 2025-34 (own title-page amendment list). Own Part III (§109, 'Tax on Gross Revenues') imposes a gross-revenue tax on business carried on in the Republic -- grounds `marketentry.facts`'s own :tax-basis claim with an actual citation. Own Part VI (§117, §120A) is a separate, narrower non-resident withholding tax on fees for services rendered by a non-resident person to an RMI client -- this iteration did not locate a distinct blanket corporate-tax exemption clause naming non-resident/offshore (NRDC) entities specifically; see the namespace docstring's honest-gap note before citing one."
     :statute/url "https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/1989/1989-0050/1989-0050_5.pdf"
     :statute/url-provenance :official-rmi-nitijela-legislation-database
     :statute/enacted-date "1989-04-14"
     :statute/retrieved-at "2026-07-23"
     :statute/topic #{:tax}}]})

(defn spec-basis
  "The jurisdiction's statute vector, or nil -- nil means NO spec-basis
  for that jurisdiction yet."
  [iso3]
  (get catalog iso3))

(defn coverage
  "Honest coverage report, same shape/discipline as `marketentry.facts/coverage`:
  never report a missing jurisdiction as covered."
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-mhl statute.facts Wave 0 (ADR-2607141700): "
                 (count (get catalog "MHL")) " MHL statute(s) seeded with an "
                 "official rmiparliament.org citation (a distinct blanket "
                 "non-resident/offshore corporate-tax exemption clause was not "
                 "located this iteration -- an honest gap, not an omission by "
                 "design; see the namespace docstring). Extend "
                 "`statute.facts/catalog`, never fabricate a law-id or URL.")})))

(defn by-topic
  "Statutes for `iso3` tagged with `topic` (e.g. :labor, :data-protection)."
  [iso3 topic]
  (filterv #(contains? (:statute/topic %) topic) (spec-basis iso3)))
