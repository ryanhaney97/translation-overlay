(ns translation-overlay.overlay
  (:require [translation-overlay.keys :refer [keycode]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as cset])
  (:import [java.lang Runnable String]
           [java.util.logging Logger Level]
           [javax.swing JFrame JLabel SwingUtilities WindowConstants]
           [java.awt Color Toolkit Dimension BasicStroke Graphics Graphics2D]
           [org.jnativehook GlobalScreen NativeHookException SwingDispatchService]
           [org.jnativehook.keyboard NativeKeyEvent NativeKeyListener]))

(def dialogue (atom []))
(def properties (atom {}))
(def key-bindings (atom {}))
(def key-pressed? (atom {}))

(defn read-data [source]
  (edn/read-string (slurp (io/resource (str source ".edn")))))

(defn read-file [source destination]
  (reset! destination (read-data source)))

(read-file "properties" properties)
(read-file (first (:sources @properties)) dialogue)
(read-file "key-bindings" key-bindings)

(defn load-keys []
  (let [bound-keys (concat (vals (dissoc @key-bindings :waypoints)) (keys (:waypoints @key-bindings)))]
    (dorun (map #(swap! key-pressed? assoc %1 false) (map keycode bound-keys)))))

(load-keys)

(defn pass-keywords []
  (if (keyword? (first @dialogue))
    (do
      (swap! dialogue rest)
      (pass-keywords))))

(pass-keywords)

(def current-sources (atom (:sources @properties)))

(defn make-runnable [func]
  (proxy [Runnable] []
    (run []
         (func))))

(defn add-key-listener [key-pressed key-released key-typed]
  (let [listener (proxy [NativeKeyListener] []
                   (nativeKeyPressed [event]
                                     (key-pressed event))
                   (nativeKeyReleased [event]
                                      (key-released event))
                   (nativeKeyTyped [event]
                                   (key-typed event)))]
    (GlobalScreen/addNativeKeyListener listener)
    listener))

(defn key->color [k]
  (if (keyword? k)
    (load-string (str "Color/" (name k)))
    (load-string (str "(Color. " k ")"))))

(defn make-paragraph [message]
  (str "<html><p>" message "</p></html>"))

(def dialogue-box (let [dialogue-properties (:dialogue @properties)]
                    (doto
                      (JLabel. (make-paragraph (first @dialogue)))
                      (.setLocation (:x dialogue-properties) (:y dialogue-properties))
                      (.setForeground (key->color (:color dialogue-properties)))
                      (.setSize (:width dialogue-properties) (:height dialogue-properties))
                      (.setVisible false))))

(defn refresh-dialogue []
  (let [dialogue-properties (:dialogue @properties)]
    (doto
      dialogue-box
      (.setLocation (:x dialogue-properties) (:y dialogue-properties))
      (.setSize (:width dialogue-properties) (:height dialogue-properties)))))

(defn refresh-properties []
  (read-file "properties" properties)
  (refresh-dialogue))

(defn change-dialogue-text [message]
  (.setText dialogue-box (make-paragraph message)))

(defn swap-visible []
  (.setVisible dialogue-box (not (.isVisible dialogue-box))))

(defn proceed-dialogue []
  (if (and (.isVisible dialogue-box) (not= 1 (count @dialogue)))
    (do
      (swap! dialogue rest)
      (if (not (string? (first @dialogue)))
        (condp = (first @dialogue)
          :hide (do
                  (proceed-dialogue)
                  (.setVisible dialogue-box false))
          :end (do
                 (read-file (first (:sources @properties)) dialogue)
                 (pass-keywords))
          (proceed-dialogue))
        nil)
      (change-dialogue-text (first @dialogue)))
    (if (= 1 (count @dialogue))
      (do
        (read-file (first (:sources @properties)) dialogue)
        (pass-keywords)))))

(defn forward []
  (if (.isVisible dialogue-box)
    (do
      (swap! dialogue rest)
      (if (not (string? (first @dialogue)))
        (forward))
      (change-dialogue-text (first @dialogue)))))

(defn backward []
  (if (.isVisible dialogue-box)
    (let [full-dialogue (read-data (first (:sources @properties)))
          position (- (count full-dialogue) (count @dialogue))]
      (if (not= position 0)
        (do
          (reset! dialogue (drop (dec position) full-dialogue))
          (if (not (string? (nth full-dialogue (dec position))))
            (backward))
          (change-dialogue-text (first @dialogue)))
        (pass-keywords)))))

(defn selection-forwards []
  (if (empty? (rest @current-sources))
    (reset! current-sources (:sources @properties))
    (do
      (swap! current-sources rest)))
  (read-file (first @current-sources) dialogue)
  (pass-keywords)
  (change-dialogue-text (first @dialogue)))

(defn selection-backwards []
  (let [full-sources (:sources @properties)
        position (- (count full-sources) (count @current-sources))]
    (if (= position 0)
      (reset! current-sources [(last (:sources @properties))])
      (reset! current-sources (drop (dec position) full-sources))))
  (read-file (first @current-sources) dialogue)
  (pass-keywords)
  (change-dialogue-text (first @dialogue)))

(defn go-to [position]
  (let [full-dialogue (read-data (first @current-sources))
        index (.indexOf full-dialogue position)]
    (if (not= index -1)
      (do
        (reset! dialogue (drop (inc index) full-dialogue))
        (change-dialogue-text (first @dialogue))))))

(defn update-map [m f]
  (reduce-kv (fn [m k v]
               (assoc m k (f v))) {} m))

(defn handle-waypoints [key-code-event]
  (let [translated (clojure.set/map-invert (update-map (cset/map-invert (:waypoints @key-bindings)) keycode))]
    (if (get translated key-code-event)
      (go-to (get translated key-code-event))
      nil)))

(defn on-key-pressed [key-event]
  (let [key-code-event (.getKeyCode key-event)]
    (if (not (get @key-pressed? key-code-event))
      (do
        (swap! key-pressed? assoc key-code-event true)
        (condp = key-code-event
          (keycode (:toggle-visible @key-bindings)) (swap-visible)
          (keycode (:proceed-dialogue @key-bindings)) (proceed-dialogue)
          (keycode (:forward @key-bindings)) (forward)
          (keycode (:backward @key-bindings)) (backward)
          (keycode (:selection-forwards @key-bindings)) (selection-forwards)
          (keycode (:selection-backwards @key-bindings)) (selection-backwards)
          (handle-waypoints key-code-event))))))

(defn on-key-released [key-event]
  (if (get @key-pressed? (.getKeyCode key-event))
    (swap! key-pressed? assoc (.getKeyCode key-event) false)))

(defn initialize-natives []
  (try
    (GlobalScreen/registerNativeHook)
    (add-key-listener on-key-pressed on-key-released identity)
    (catch Exception e
      (do (println (str "There was a problem registering the native hook: " (.getMessage e)))))))

(defn initialize-window []
  (GlobalScreen/setEventDispatcher (SwingDispatchService.))
  (initialize-natives)
  (let [frame (JFrame. "")
        content (.getContentPane frame)
        screen-size (.getScreenSize (Toolkit/getDefaultToolkit))]
    (doto frame
      (.setUndecorated true)
      (.setBackground (Color. 0 0 0 0))
      (.setAlwaysOnTop true)
      (.setSize 640 480)
      (.setLocation (- (/ (int (.getWidth screen-size)) 2) 320) (- (/ (int (.getHeight screen-size)) 2) 240))
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE))
    (.setLayout content nil)
    (.add frame dialogue-box)
    (.setVisible frame true)))
