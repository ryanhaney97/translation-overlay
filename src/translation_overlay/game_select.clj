(ns translation-overlay.game-select
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [javax.swing JDialog JButton JComboBox WindowConstants]
           [java.awt.event ActionListener]
           [java.awt BorderLayout Dimension Toolkit]))

(def exit-dialog (atom false))
(def message (atom ""))

(defn get-paths []
  (let [path (file-seq (clojure.java.io/file "../translations"))]
    (if (.exists (first path))
      path
      (file-seq (clojure.java.io/file "translations/")))))

(defn get-folders []
  (let [paths (get-paths)
        filtered (filter (complement (partial re-matches #".*\.[A-z]*"))  (map str paths))]
    (filter (complement empty?) (map #(string/replace %1 #"\/|games|\.|translations|\\" "") filtered))))

(defn make-button [text func]
  (let [button (JButton. text)
        action-listener (proxy [ActionListener] []
                          (actionPerformed [e]
                                           (func)))]
    (doto button
      (.addActionListener action-listener))))

(defn end-dialog [dialog combo-box]
  (.setVisible dialog false)
  (reset! message (str (.getSelectedItem combo-box)))
  (swap! exit-dialog not))

(defn wait-for-close []
  (if @exit-dialog
    @message
    (recur)))

(defn make-dialog []
  (let [dialog (JDialog. (cast JDialog nil))
        combo-box (JComboBox. (to-array (get-folders)))
        screen-size (.getScreenSize (Toolkit/getDefaultToolkit))]
    (doto dialog
      (.setSize 500 100)
      (.setLocation (- (/ (int (.getWidth screen-size)) 2) 250) (- (/ (int (.getHeight screen-size)) 2) 50))
      (.setLayout (BorderLayout.))
      (.add combo-box BorderLayout/CENTER)
      (.add (make-button "Select Game" (partial end-dialog dialog combo-box)) BorderLayout/SOUTH)
      (.setDefaultCloseOperation WindowConstants/DISPOSE_ON_CLOSE)
      (.setVisible true))
    (wait-for-close)))
