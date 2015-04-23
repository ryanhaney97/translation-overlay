(defproject translation-overlay "0.1.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" "file:jnativehook-repo"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jhooks/jhooks "2.0.1"]]
  :profiles {:dev {:resource-paths ["resources"]}}
  :aot [translation-overlay.core]
  :main translation-overlay.core)
