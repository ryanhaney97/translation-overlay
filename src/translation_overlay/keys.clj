(ns translation-overlay.keys
  (:import [java.lang Character]
           [org.jnativehook.keyboard NativeKeyEvent]))

(defn make-key [letter]
  (let [key-name (str "NativeKeyEvent/VC_" (Character/toUpperCase letter))]
    (load-string key-name)))

(def letter-list (map char (range (int \a) (inc (int \z)))))

(def native-keys (zipmap (map keyword (map str letter-list)) (map make-key letter-list)))

(defn keycode [native-key]
  (get native-keys native-key))
