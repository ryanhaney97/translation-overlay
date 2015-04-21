(ns translation-overlay.overlay
  (:require [translation-overlay.keys :refer [keycode]])
  (:import [java.lang Runnable]
           [java.util.logging Logger Level]
           [javax.swing JFrame JLabel SwingUtilities WindowConstants]
           [java.awt Color Toolkit Dimension]
           [org.jnativehook GlobalScreen NativeHookException SwingDispatchService]
           [org.jnativehook.keyboard NativeKeyEvent NativeKeyListener]))

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

(defn make-label [message x y]
  (let [label (JLabel. message)]
    (.setLocation label x y)
    (.setForeground label Color/GREEN)
    (.setSize label (.getPreferredSize label))
    label))

(def some-label (doto
                  (make-label "On!" 0 0)
                  (.setVisible false)))

(defn toggle-label [key-event]
  (let [key-code-event (.getKeyCode key-event)
        key-c (keycode :c)]
    (if (= key-code-event key-c)
      (.setVisible some-label (not (.isVisible some-label))))))

(defn initialize-natives []
  (try
    (GlobalScreen/registerNativeHook)
    (add-key-listener toggle-label identity identity)
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
    (.add frame some-label)
    (.setVisible frame true)))
