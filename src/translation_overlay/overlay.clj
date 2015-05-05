(ns translation-overlay.overlay
  (:require [translation-overlay.keys :refer [keycode]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.set :as cset]
            [clojure.string :as string])
  (:import [java.lang Runnable String System]
           [java.util.logging Logger Level]
           [javax.swing JFrame JTextArea SwingUtilities WindowConstants]
           [java.awt Color Toolkit Dimension BasicStroke Graphics Graphics2D]
           [java.awt.event WindowListener]
           [org.jnativehook GlobalScreen NativeHookException SwingDispatchService]
           [org.jnativehook.keyboard NativeKeyEvent NativeKeyListener]))

(def dialogue (atom []))
(def properties (atom {}))
(def key-bindings (atom {}))
(def key-pressed? (atom {}))
(def game (atom ""))
(declare dialogue-box)

(defn read-data [source]
  (edn/read-string (slurp (io/resource (str "resources/" @game "/" source ".edn")))))

(defn read-file [source destination]
  (reset! destination (read-data source)))

(defn load-keys []
  (let [bound-keys (concat (vals (dissoc @key-bindings :waypoints)) (keys (:waypoints @key-bindings)))]
    (dorun (map #(swap! key-pressed? assoc %1 false) (map keycode bound-keys)))))

(defn pass-keywords []
  (if (keyword? (first @dialogue))
    (do
      (swap! dialogue rest)
      (pass-keywords))))

(def current-sources (atom nil))

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

(defn make-window-listener [opened closed closing iconified deiconified activated deactivated]
  (proxy [WindowListener] []
    (windowOpened [event]
                  (opened event))
    (windowClosed [event]
                  (closed event))
    (windowClosing [event]
                   (closing event))
    (windowIconified [event]
                     (iconified event))
    (windowDeiconified [event]
                       (deiconified event))
    (windowActivated [event]
                     (opened event))
    (windowDeactivated [event]
                       (closed event))))

(defn key->color [k]
  (import java.awt.Color)
  (if (keyword? k)
    (load-string (str "Color/" (name k)))
    (load-string (str "(Color. " k ")"))))

(defn remove-newlines [string]
  (string/replace string #"\n" ""))

(defn init-dialogue-box []
  (let [dialogue-properties (:dialogue @properties)
        result
        (doto
          (JTextArea. (remove-newlines (first @dialogue)))
          (.setLocation (:x dialogue-properties) (:y dialogue-properties))
          (.setForeground (key->color (:text-color dialogue-properties)))
          (.setBackground (key->color (:background-color dialogue-properties)))
          (.setLineWrap true)
          (.setWrapStyleWord true)
          (.setSize (:width dialogue-properties) (:height dialogue-properties))
          (.setVisible false))]
    (def dialogue-box result)))

(defn refresh-dialogue []
  (let [dialogue-properties (:dialogue @properties)]
    (doto
      dialogue-box
      (.setForeground (key->color (:text-color dialogue-properties)))
      (.setBackground (key->color (:background-color dialogue-properties)))
      (.setLocation (:x dialogue-properties) (:y dialogue-properties))
      (.setSize (:width dialogue-properties) (:height dialogue-properties)))))

(defn refresh-properties []
  (read-file "properties" properties)
  (refresh-dialogue))

(defn change-dialogue-text [message]
  (.setText dialogue-box (remove-newlines message)))

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

(defn end-session [& args]
  (GlobalScreen/unregisterNativeHook)
  (System/runFinalization)
  (System/exit 0))

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
          (keycode (:quit @key-bindings)) (end-session)
          (keycode :r) (refresh-properties)
          (handle-waypoints key-code-event))))))

(defn on-key-released [key-event]
  (if (get @key-pressed? (.getKeyCode key-event))
    (swap! key-pressed? assoc (.getKeyCode key-event) false)))

(defn disable-logger []
  (let [logger (Logger/getLogger (.getName (.getPackage (class GlobalScreen))))]
    (.setLevel logger Level/OFF)
    (dorun (map #(.setLevel %1 Level/OFF) (.getHandlers (Logger/getLogger ""))))))

(defn initialize-natives []
  (try
    (GlobalScreen/registerNativeHook)
    (add-key-listener on-key-pressed on-key-released identity)
    (dorun (disable-logger))
    (catch Exception e
      (do (println (str "There was a problem registering the native hook: " (.getMessage e)))))))

(defn init-data []
  (read-file "properties" properties)
  (read-file (first (:sources @properties)) dialogue)
  (read-file "key-bindings" key-bindings)
  (reset! current-sources (:sources @properties))
  (load-keys)
  (pass-keywords))

(defn initialize-window [g]
  (reset! game g)
  (init-data)
  (init-dialogue-box)
  (initialize-natives)
  (GlobalScreen/setEventDispatcher (SwingDispatchService.))
  (let [frame (JFrame. "")
        content (.getContentPane frame)
        screen-size (.getScreenSize (Toolkit/getDefaultToolkit))]
    (doto frame
      (.addWindowListener (make-window-listener identity end-session identity identity identity identity identity))
      (.setUndecorated true)
      (.setFocusable false)
      (.setFocusableWindowState false)
      (.setBackground (Color. 0 0 0 0))
      (.setAlwaysOnTop true)
      (.setSize 640 480)
      (.setLocation (- (/ (int (.getWidth screen-size)) 2) 320) (- (/ (int (.getHeight screen-size)) 2) 240))
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE))
    (.setLayout content nil)
    (.add frame dialogue-box)
    (.setVisible frame true)))
