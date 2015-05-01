(defproject translation-overlay "0.3.2"
  :description "Used to translate games by placing an overlay on top of them (kind of like subtitles)."
  :url "https://github.com/yoshiquest/translation-overlay"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"local" "file:jnativehook-repo"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [jhooks/jhooks "2.0.1"]]
  :resource-paths ["translations"]
  :aot [translation-overlay.core]
  :main translation-overlay.core)
