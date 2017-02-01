(defproject dribbble-tool "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [http-kit "2.2.0"]
                 [muse "0.4.3-alpha2"]
                 [cats "0.4.0"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot dribbble-tool.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
