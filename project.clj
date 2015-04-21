(defproject translation-overlay "0.0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" "file:jnativehook-repo"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jhooks/jhooks "2.0.1"]]
  :aot [translation-overlay.core]
  :main translation-overlay.core)
