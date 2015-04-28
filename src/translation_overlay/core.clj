(ns translation-overlay.core
  (:require [translation-overlay.overlay :refer [make-runnable initialize-window]])
  (:import [javax.swing SwingUtilities]))

(defn -main []
  (SwingUtilities/invokeLater (make-runnable initialize-window)))
