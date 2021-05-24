(defproject wakfu-shell "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [mvxcvi/clj-cbor "1.1.0"]]
  :main ^:skip-aot wakfu-shell.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :source-paths ["src"])
