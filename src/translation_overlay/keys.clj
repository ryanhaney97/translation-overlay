(ns translation-overlay.keys
  (:import [java.lang Character]
           [org.jnativehook.keyboard NativeKeyEvent]))

(defn make-key [letter]
  (let [key-name (str "NativeKeyEvent/VC_" (Character/toUpperCase letter))]
    (load-string key-name)))

(def letter-list (map char (range (int \a) (inc (int \z)))))

(def number-list (map char (range (int \0) (inc (int \9)))))

(def native-keys (merge (zipmap (map keyword (map str letter-list)) (map make-key letter-list))
                        (zipmap (map keyword (map str number-list)) (map make-key number-list))
                        {:open-bracket NativeKeyEvent/VC_OPEN_BRACKET
                         :close-bracket NativeKeyEvent/VC_CLOSE_BRACKET}))

(defn keycode [native-key]
  (get native-keys native-key))
