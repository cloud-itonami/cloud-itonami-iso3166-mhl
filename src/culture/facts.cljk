(ns culture.facts
  "Country-level regional-culture catalog for the Marshall Islands (MHL)
  -- national dishes, protected products, beverages, crafts, festivals
  and heritage sites, per ADR-2607171400 addendum 2
  (cloud-itonami-municipality-culture-catalog Wave 1, in
  com-junkawasaki/root). Sibling namespace to `marketentry.facts` /
  `statute.facts` (ADR-2607141700); city-level counterparts live in the
  cloud-itonami-municipality-* repos.

  Catalog is keyed by UPPERCASE ISO3 (mirrors `statute.facts`); entries
  carry no :culture/municipality (that attribute is city-level only).

  Every entry cites a source URL that was actually fetched and read on
  :culture/retrieved-at -- never fabricated. Summaries state only what the
  cited source confirms. An item not in this table has NO spec-basis, full
  stop; extend `catalog`, do not invent an id/url.

  Marshall Islands is a thinly-documented micro-state; verification
  during Wave 1 research dropped plausible candidates that could not be
  confirmed: copra, though historically significant to Pacific-island
  economies generally, is not mentioned in connection with the Marshall
  Islands specifically on Wikipedia's Copra article (only Papua New
  Guinea, Indonesia, Solomon Islands and Vanuatu are named), so it was
  dropped rather than assumed; jaki-ed traditional mat weaving is a
  real and well-attested Marshallese craft (per Smarthistory and
  Pacific-heritage sources) but has no dedicated Wikipedia article, so
  it was dropped rather than cited off the reference implementation's
  Wikipedia-first sourcing pattern.")

(def catalog
  "iso3 -> vector of culture entries."
  {"MHL"
   [{:culture/id "mhl.dish.bwiro"
     :culture/name "Bwiro"
     :culture/country "MHL"
     :culture/kind :dish
     :culture/summary "Traditional Marshallese fermented breadfruit paste, wrapped in banana leaves and cooked in an underground oven, that can be preserved for many months without spoiling."
     :culture/url "https://en.wikipedia.org/wiki/Marshallese_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "mhl.dish.mokwan"
     :culture/name "Mokwan"
     :culture/country "MHL"
     :culture/kind :dish
     :culture/summary "Preserved dried pandanus paste continuing to be produced by people on the northern atoll Ratak Chain islands of the Marshall Islands."
     :culture/url "https://en.wikipedia.org/wiki/Marshallese_cuisine"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "mhl.dish.chukuchuk"
     :culture/name "Chukuchuk"
     :culture/country "MHL"
     :culture/kind :dish
     :culture/summary "Marshallese rice and coconut dish -- a ball shape made of calrose rice and shredded coconut flesh."
     :culture/url "https://en.wikipedia.org/wiki/Chukuchuk"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "mhl.craft.stick-chart"
     :culture/name "Stick chart"
     :culture/country "MHL"
     :culture/kind :craft
     :culture/summary "Traditional Marshallese navigation aid made and used to navigate the Pacific Ocean by canoe off the coast of the Marshall Islands, representing ocean swell patterns and island locations using wood, fiber and shells."
     :culture/url "https://en.wikipedia.org/wiki/Stick_chart"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}
    {:culture/id "mhl.heritage.bikini-atoll"
     :culture/name "Bikini Atoll"
     :culture/country "MHL"
     :culture/kind :heritage
     :culture/summary "UNESCO World Heritage Site designated on 3 August 2010, bearing direct tangible evidence of nuclear testing that symbolises the dawn of the nuclear age."
     :culture/url "https://en.wikipedia.org/wiki/Bikini_Atoll"
     :culture/url-provenance :wikipedia-en
     :culture/retrieved-at "2026-07-17"}]})

(defn spec-basis [iso3] (get catalog iso3))

(defn coverage
  ([] (coverage (keys catalog)))
  ([iso3s]
   (let [have (filter catalog iso3s)
         missing (remove catalog iso3s)]
     {:requested (count iso3s)
      :covered (count have)
      :covered-jurisdictions (vec (sort have))
      :missing-jurisdictions (vec (sort missing))
      :note (str "cloud-itonami-iso3166-mhl culture catalog "
                 "(ADR-2607171400 addendum 2, Wave 1): " (count (get catalog "MHL"))
                 " MHL entries, each with a fetched-and-read citation. "
                 "Extend `culture.facts/catalog`, never fabricate an id/url.")})))

(defn by-kind [iso3 kind]
  (filterv #(= (:culture/kind %) kind) (spec-basis iso3)))
