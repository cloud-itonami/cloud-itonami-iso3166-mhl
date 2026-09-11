(ns marketentry.sim
  "Demo driver -- `clojure -M:dev:run`. Walks a clean engagement
  through intake -> jurisdiction assessment -> filing draft
  (escalate/approve/commit) -> filing submit (escalate/approve/
  commit), then shows HARD-hold scenarios covering all five of this
  vertical's new checks (domestic-track-missing, fibl-missing,
  procurement-basis-stale, gross-revenue-tax-unacknowledged,
  cofa-preference-fabricated)."
  (:require [langgraph.graph :as g]
            [marketentry.store :as store]
            [marketentry.operation :as op]))

(def operator {:actor-id "op-1" :actor-role :market-entry-operator :phase 3})

(defn- exec-op [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "op-1"}} {:thread-id tid :resume? true}))

(defn- setup!
  "assess -> approve -> draft -> approve, so a `:filing/submit` check
  can be exercised in isolation."
  [actor tid-prefix subject]
  (exec-op actor (str tid-prefix "-assess") {:op :jurisdiction/assess :subject subject} operator)
  (approve! actor (str tid-prefix "-assess"))
  (exec-op actor (str tid-prefix "-draft") {:op :filing/draft :subject subject} operator)
  (approve! actor (str tid-prefix "-draft")))

(defn -main [& _]
  (let [db (store/seed-db)
        actor (op/build db)]
    (println "== engagement/intake eng-1 (MHL, clean) ==")
    (println (exec-op actor "t1" {:op :engagement/intake :subject "eng-1"
                                  :patch {:id "eng-1" :operator "Kita Systems MH"}} operator))

    (println "== jurisdiction/assess eng-1 (escalates -- human approves) ==")
    (println (exec-op actor "t2" {:op :jurisdiction/assess :subject "eng-1"} operator))
    (println (approve! actor "t2"))

    (println "== filing/draft eng-1 (always escalates -- actuation/draft-filing) ==")
    (let [r (exec-op actor "t3" {:op :filing/draft :subject "eng-1"} operator)]
      (println r)
      (println "-- human market-entry operator approves --")
      (println (approve! actor "t3")))

    (println "== filing/submit eng-1 (always escalates -- actuation/submit-filing) ==")
    (let [r (exec-op actor "t4" {:op :filing/submit :subject "eng-1"} operator)]
      (println r)
      (println "-- human market-entry operator approves --")
      (println (approve! actor "t4")))

    (println "== jurisdiction/assess eng-2 (no spec-basis -> HARD hold) ==")
    (println (exec-op actor "t5" {:op :jurisdiction/assess :subject "eng-2" :no-spec? true} operator))

    (println "== eng-3: fee-mismatch setup + filing/submit -> HARD hold ==")
    (setup! actor "t6" "eng-3")
    (println (exec-op actor "t7" {:op :filing/submit :subject "eng-3"} operator))

    (println "== eng-4: FLAGSHIP domestic-track-missing (offshore IRI only, no domestic license) ==")
    (setup! actor "t8" "eng-4")
    (println (exec-op actor "t9" {:op :filing/submit :subject "eng-4"} operator))

    (println "== eng-5: fibl-missing (foreign investment, no FIBL) ==")
    (setup! actor "t10" "eng-5")
    (println (exec-op actor "t11" {:op :filing/submit :subject "eng-5"} operator))

    (println "== eng-6: procurement-basis-stale (cites repealed 1988 Code) ==")
    (setup! actor "t12" "eng-6")
    (println (exec-op actor "t13" {:op :filing/submit :subject "eng-6"} operator))

    (println "== eng-7: gross-revenue-tax-unacknowledged ==")
    (setup! actor "t14" "eng-7")
    (println (exec-op actor "t15" {:op :filing/submit :subject "eng-7"} operator))

    (println "== eng-8: cofa-preference-fabricated (claims a nonexistent COFA bidding preference) ==")
    (setup! actor "t16" "eng-8")
    (println (exec-op actor "t17" {:op :filing/submit :subject "eng-8"} operator))

    (println "== filing/draft eng-1 AGAIN (double-draft -> HARD hold) ==")
    (println (exec-op actor "t18" {:op :filing/draft :subject "eng-1"} operator))

    (println "== filing/submit eng-1 AGAIN (double-submit -> HARD hold) ==")
    (println (exec-op actor "t19" {:op :filing/submit :subject "eng-1"} operator))

    (println "== audit ledger ==")
    (doseq [f (store/ledger db)] (println f))

    (println "== draft records ==")
    (doseq [r (store/draft-history db)] (println r))

    (println "== submit records ==")
    (doseq [r (store/submit-history db)] (println r))))
