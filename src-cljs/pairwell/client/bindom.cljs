(ns pairwell.client.bindom
  (:require [taoensso.encore :refer [logf]]))


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
  "Creates an event handler that extracts form control values
  and conjs them onto a queue in app-state located at path.
  The handler swallows exceptions and returns false to prevent
  a POST request occuring."
  [app-state path]
  (fn a-submit-handler [e]
    (try
      (let [form-controls-collection (.-elements (.-target e))
            kvps (for [i (range (.-length form-controls-collection))
                       :let [control (.item form-controls-collection i)]
                       :when (not= "submit" (.-type control))]
                   [(.-name control) (.-value control)])]
        (swap! app-state update-in path (fnil conj []) kvps))
      (catch :default ex
        (logf (pr-str ex))))
    false))
