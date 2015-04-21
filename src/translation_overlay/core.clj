(ns translation-overlay.core
  (:require [translation-overlay.overlay :refer [make-runnable initialize-window]])
  (:import [javax.swing SwingUtilities])
  (:gen-class))

(defn -main []
  (SwingUtilities/invokeLater (make-runnable initialize-window)))
