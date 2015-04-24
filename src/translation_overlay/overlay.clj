(ns translation-overlay.overlay
  (:require [translation-overlay.keys :refer [keycode]]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [java.lang Runnable String]
           [java.util.logging Logger Level]
           [javax.swing JFrame JLabel SwingUtilities WindowConstants]
           [java.awt Color Toolkit Dimension]
           [org.jnativehook GlobalScreen NativeHookException SwingDispatchService]
           [org.jnativehook.keyboard NativeKeyEvent NativeKeyListener]))

(def dialogue (atom []))
(def properties (atom {}))

(defn read-data [source]
  (edn/read-string (slurp (io/resource (str source ".edn")))))

(defn read-file [source destination]
  (reset! destination (read-data source)))

(read-file "dialogue" dialogue)
(read-file "properties" properties)

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
  (if (.isVisible dialogue-box)
    (do
      (swap! dialogue rest)
      (if (= (first @dialogue) :hide)
        (do
          (.setVisible dialogue-box false)
          (swap! dialogue rest)))
      (change-dialogue-text (first @dialogue)))))

(defn forward []
  (if (.isVisible dialogue-box)
    (do
      (swap! dialogue rest)
      (if (not (string? (first @dialogue)))
        (swap! dialogue rest))
      (change-dialogue-text (first @dialogue)))))

(defn backward []
  (if (.isVisible dialogue-box)
    (let [full-dialogue (read-data "dialogue")
          position (- (count full-dialogue) (count @dialogue))]
      (if (not (string? (nth full-dialogue (dec position))))
        (reset! dialogue (drop (- position 2) full-dialogue))
        (reset! dialogue (drop (dec position) full-dialogue)))
      (change-dialogue-text (first @dialogue)))))

(defn on-key-pressed [key-event]
  (let [key-code-event (.getKeyCode key-event)]
    (condp = key-code-event
      (keycode :c) (swap-visible)
      (keycode :z) (proceed-dialogue)
      (keycode :s) (forward)
      (keycode :a) (backward)
      (keycode :r) (refresh-properties)
      nil)))

(defn initialize-natives []
  (try
    (GlobalScreen/registerNativeHook)
    (add-key-listener on-key-pressed identity identity)
    (catch Exception e
      (do (println (str "There was a problem registering the native hook: " (.getMessage e)))))))

(defn initialize-window []
  (GlobalScreen/setEventDispatcher (SwingDispatchService.))
  (initialize-natives)
  (let [frame (JFrame. "")
        content (.getContentPane frame)
        screen-size (.getScreenSize (Toolkit/getDefaultToolkit))]
    (.setUndecorated frame true)
    (.setBackground frame (Color. 0 0 0 0))
    (.setAlwaysOnTop frame true)
    (.setSize frame 640 480)
    (.setLocation frame (- (/ (int (.getWidth screen-size)) 2) 320) (- (/ (int (.getHeight screen-size)) 2) 240))
    (.setLayout content nil)
    (.setDefaultCloseOperation frame WindowConstants/DISPOSE_ON_CLOSE)
    (.add frame dialogue-box)
    (.setVisible frame true)))
