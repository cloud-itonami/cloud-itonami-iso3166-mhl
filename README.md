# cloud-itonami-iso3166-mhl

**`:implemented`** for **MHL** (Marshall Islands). Flagship
`domestic-track-missing` (dual-track registry disambiguation).

```
clojure -M:dev:test
```

AGPL-3.0-or-later.

## Dual-track registration -- the single most important disambiguation

MHL has TWO independent registration tracks, and this actor's flagship
check exists because conflating them is the most common real-world
mistake a market-entry advisor could make:

- **Offshore**: the RMI Corporate and Ships Registries. Statutorily
  the Office of the Registrar of Corporations, but administered in
  practice by **IRI (International Registries, Inc.)**, a private
  US-incorporated company (public search: register.iri.com). Covers
  Non-Resident Domestic Corporations (NRDCs), LLCs, limited
  partnerships, Foreign Maritime Entities, and the ships registry.
  This track is for entities that do **not** conduct business within
  RMI.
- **Domestic**: a general business license issued by the **Ministry
  of Finance** for any entity or individual conducting business
  **within** RMI (locally-facing activity, including bidding on RMI
  public contracts), typically issued within ~7 days.

A foreign entity that wants to actually operate or bid on a contract
**inside** RMI needs the **domestic** Ministry-of-Finance business
license -- an **offshore** IRI registration alone does **not** satisfy
that. `marketentry.governor`'s `domestic-track-missing` check
independently verifies this: an engagement that declares
`:bids-domestically? true` but only has `:has-offshore-registration?
true` (and NOT `:has-domestic-license? true`) is HELD, unoverridable
by a human approver.

This repository deliberately does **not** assert "MICA" as the
current official name of the corporate/ships registry administrator
-- evidence points to IRI, with "MIACA" also appearing as a
related/legacy name; the exact current official branding is
unresolved, so `marketentry.facts` uses the safe framing "IRI
(International Registries, Inc.), administering the RMI Corporate and
Ships Registries" throughout.

## Five checks, each with a falsifiable fixture

Beyond the template's generic spec-basis / evidence-incomplete /
engagement-fee-mismatch / confidence-actuation-gate /
double-draft / double-submit machinery (shared with every sibling
actor in this fleet), this vertical adds five checks, each grounded in
a specific verified fact and each exercised by its own test in
`test/marketentry/governor_contract_test.clj`:

1. **`domestic-track-missing`** -- see above. Grounded in the
   dual-track registry structure (register-iri.com /
   rmigovernment.org).
2. **`fibl-missing`** -- when an engagement declares
   `:involves-foreign-investment? true`, both a Foreign Investment
   Business License (FIBL, Registrar of Foreign Investment, Office of
   the Attorney General, Foreign Investment Business License
   (Amendment) Act, 2000 -- P.L. 2000-5) AND acknowledgement of the
   Cabinet-approved National Reserved List must be on file. This gate
   is structural only -- it never enumerates a specific Reserved List
   sector, since only the list's existence/general scope is verified,
   not its specific line items.
3. **`procurement-basis-stale`** -- a filing must cite the *current*
   public-procurement legal basis, the **Procurement Code Act, 2023
   (P.L. 2023-62)**, effective 10 April 2023. Citing the **repealed**
   1988 Procurement Code (formerly 44 MIRC 1) is HELD. (Note: this is
   unrelated to the UK's similarly-named "Procurement Act 2023" -- a
   common web-search trap for this exact phrase.)
4. **`gross-revenue-tax-unacknowledged`** -- a domestically-operating
   engagement must acknowledge that it is subject to RMI's
   gross-revenue-tax basis (not net-income tax), administered by the
   Ministry of Finance. No specific rate figure is ever asserted by
   this repository -- the applicable rate needs revalidation against a
   current primary source before it could ever be cited.
5. **`cofa-preference-fabricated`** -- the Compact of Free Association
   (COFA Act of 1985, effective 21 Oct 1986) confers customs/duty-free
   treatment for RMI-origin goods entering the US, MFN-equivalent
   treatment for US goods entering RMI, and USD as RMI's legal tender
   -- **trade/currency/mobility facts only**. COFA creates **no**
   procurement-bidding preference for US-affiliated bidders. This was
   explicitly investigated and found unverified/likely nonexistent.
   `marketentry.facts/cofa-grants-bidding-preference?` always returns
   `false`, and the governor rejects any engagement that claims a COFA
   bidding preference, **regardless of the claimant's stated
   nationality** -- encoding this verified negative here, rather than
   letting an LLM advisor improvise a fabricated COFA preference, is
   itself the honest, correctly-scoped fact.

## Actuation

Same discipline as every sibling actor: `:filing/draft` /
`:filing/submit` are the two real-world acts this actor performs
(preparing a portal registration package; actually submitting a portal
registration). Both are members of `write-ops` (governor-gated) but
**never** members of any rollout phase's `:auto` set, including phase
3 -- a permanent structural fact, not a rollout milestone still to
come. `marketentry.governor`'s `:actuation/draft-filing` /
`:actuation/submit-filing` high-stakes gate enforces the same
invariant independently. Two layers, not one, agree that a human
market-entry operator always makes the actuation call.

## Sources actually fetched and read

- https://www.register-iri.com/corporate/ (IRI-administered
  offshore corporate/ships registry, dual-track structure)
- https://www.rmiparliament.org/cms/library/public-laws-by-years/19-2000.html
  (Foreign Investment Business License (Amendment) Act, 2000 --
  P.L. 2000-5)
- https://rmiparliament.org/cms/library/public-laws-by-years/51-public-laws-by-years-2023.html
  (Procurement Code Act, 2023 -- P.L. 2023-62)

## Statute catalog

Alongside `marketentry.facts` (public-procurement market-entry only,
narrow scope), this repo carries a **general-law compliance catalog**
(ADR-2607141700, `cloud-itonami-compliance-fact-federation`) -- a
second, orthogonal set of statutes a company operating in the Marshall
Islands must generally track for compliance:

- `src/statute/facts.cljc` -- company/business-associations law (Title
  52 Associations Law, Business Corporations Act, P.L. 1990-91), two
  labor-law statutes (Title 16: the Minimum Wage Act 1986, and the
  Labor (Non-Resident Workers) Act 2018, which establishes the Labor
  Division/Director of Labor administering authority), and income tax
  law (Title 48, Income Tax Act 1989). Every entry cites an official
  `rmiparliament.org` (Nitijela of the Republic of the Marshall
  Islands) legislation-database PDF, fetched and read directly this
  iteration -- no Cloudflare/bot-detection wall was encountered on this
  source.

This namespace does **not** contradict `marketentry.facts`'s dual-track
framing or its "do not assert MICA" caution -- it is the same Business
Corporations Act read further: the Act's own §4 establishes two
Registrars of Corporations, and its own §2(p) names the non-resident
registrar "The Trust Company of the Marshall Islands, Inc.," which is
consistent with (and more precise than) `marketentry.facts`'s hedged
"IRI (International Registries, Inc.)" framing. See the namespace
docstring in `src/statute/facts.cljc` for the full research trail,
including the honest gap on MHL's income-tax law: this iteration
confirmed the general gross-revenue-tax citation `marketentry.facts`
already asserted but did not locate a distinct blanket non-resident/
offshore corporate-tax exemption clause.

### Sources actually fetched and read

- https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/1990/1990-0091/1990-0091_9.pdf
  (Business Corporations Act 1990, Title 52 Ch.1 -- official Nitijela
  consolidated text, version 9)
- https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/1986/1986-0015/1986-0015_4.pdf
  (Minimum Wage Act 1986, Title 16 Ch.4)
- https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/2018/2018-0068/2018-0068_2.pdf
  (Labor (Non-Resident Workers) Act 2018, Title 16 Ch.1)
- https://rmiparliament.org/cms/images/LEGISLATION/PRINCIPAL/1989/1989-0050/1989-0050_5.pdf
  (Income Tax Act 1989, Title 48 Ch.1)
- https://www.register-iri.com/corporate/legal/associations-law/ and its
  linked courtesy-copy PDF (cross-read only, for corroboration -- the
  rmiparliament.org version above is the one cited as `:statute/url`,
  being newer and the official government database rather than a
  publisher's courtesy copy)

## Culture catalog

Alongside the market-entry / statute catalogs, this repo carries a
**country-level regional-culture catalog** (ADR-2607171400 addendum 2,
`cloud-itonami-municipality-culture-catalog` Wave 1, in
`com-junkawasaki/root`) — national dishes, protected products, beverages,
crafts, festivals and heritage sites for the Marshall Islands:

- `src/culture/facts.cljc` — the catalog, source of truth (keyed by
  uppercase ISO3, mirroring `statute.facts`).
- `schema/culture.edn` — DataScript schema.
- `data/culture-tx.edn` — derived DataScript tx-data (regenerated from
  the catalog, never hand-edited).

City-level counterparts live in the `cloud-itonami-municipality-*` repos.
Same provenance discipline as the compliance catalogs: every entry cites a
source URL that was actually fetched and read on `:culture/retrieved-at`;
summaries state only what the cited source confirms. An item not in
`culture.facts/catalog` has no spec-basis — never fabricate one.
