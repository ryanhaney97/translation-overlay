(ns translation-overlay.core
  (:require [translation-overlay.overlay :refer [make-runnable initialize-window]]
            [translation-overlay.game-select :refer [make-dialog]])
  (:import [javax.swing SwingUtilities]))

(defn -main []
  (let [game (make-dialog)]
    (SwingUtilities/invokeLater (make-runnable (partial initialize-window game)))))
