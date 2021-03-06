(ns skuld.politics
  "Periodically initiates the election cycle."
  (:require [skuld.vnode :as vnode])
  (:use clojure.tools.logging))

(defn service
  "Creates a new politics service."
  [vnodes]
  (let [running (promise)]
    (future
      (loop []
        (try
          (when-let [vnodes (-> vnodes deref vals)]
            (->> vnodes 
                 shuffle
                 (pmap
                   (fn [vnode]
                     (try
                       (Thread/sleep (rand-int 100))
                       (vnode/elect! vnode)
                       (catch Throwable t
                         (warn t "electing" (:partition vnode))))))
               dorun))
          (catch Throwable t
            (warn t "in election cycle")))
        (when (deref running 10000 true)
          (recur))))
    running))

(defn shutdown!
  "Shuts down a politics service."
  [politics]
  (deliver politics false))
