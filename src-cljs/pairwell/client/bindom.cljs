(ns pairwell.client.bindom
  (:require [taoensso.encore :refer [errorf]]))


(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn setter
  "Creates an event handler that puts the target value into app-state."
  [app-state path]
  (fn a-setter [e]
    (let [v (.-value (.-target e))]
      (if (seq v)
        (swap! app-state assoc-in path v)
        (swap! app-state dissoc-in path)))))

(defn form
  "Creates an event handler that extracts form control names and values
  as a map, and calls f on them.
  The handler swallows exceptions and returns false to prevent
  a POST request occuring."
  [f]
  (fn a-submit-handler [e]
    (try
      (let [target (.-target e)
            form-controls-collection (.-elements target)
            kvps (for [i (range (.-length form-controls-collection))
                       :let [control (.item form-controls-collection i)
                             k (.-name control)
                             v (.-value control)]
                       :when (seq k)]
                   [(keyword k) v])]
        (if (f (into {} kvps))
          (.reset target)))
      (catch :default ex
        (errorf (str ex))))
    false))
