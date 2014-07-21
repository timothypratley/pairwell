(ns pairwell.client.audio)


(def clips {:pings {:sprite {:a [0 1750]
                             :b [2900 1000]
                             :c [5350 2250]
                             :d [8750 1550]
                             :e [10380 10000]}}
            :syn {}
            :throb {}
            :slumberjack {:buffer true}})

(def howlers
  (into {}
        (for [[k v] clips]
          [k (js/Howl. (clj->js (merge {:urls [(str "audio/" (name k) ".mp3")
                                               (str "audio/" (name k) ".ogg")]}
                                       v)))])))

(def dj
  (into {}
        (apply concat
          (for [[k v] clips]
            (cons [k (fn a-clip-player []
                       (.play (howlers k)))]
                  (for [[sk] (:sprite v)]
                    [sk (fn a-sprite-player []
                          (.play (howlers k) (name sk)))]))))))

((dj :c))

(defn stop []
  (doseq [h (vals howlers)]
    (.stop h)))

(defn fade-out []
  (doseq [h (vals howlers)]
    (.fadeOut h 0 1000
           (fn []
             (.volume h 1)))))

;TODO: precalc state names
(defn transitions [a b]
  (letfn [(changed [k]
            (not= (a k) (b k)))
          (added [k]
            (< (count (a k)) (count (b k))))
          (removed [k]
            (> (count (a k)) (count (b k))))
          (interest [x]
            (set (mapcat :interest (get-in x [:model :cards]))))
          (new-interest []
            (seq (clojure.set/difference (interest b) (interest a))))
          (lost-interest []
            (seq (clojure.set/difference (interest a) (interest b))))
          (confirmed []
            (seq (mapcat :contact (get-in x [:model :available]))))]
    (when (changed :page)
      (if (= :about (b :page))
        ((dj :slumberjack))
        (when (= :about (a :page))
          (fade-out))))
    (when (changed :username)
      (if (b :username)
        ((dj :e))
        ((dj :d))))
    (when (added :cards)
      ((dj :d)))
    (when (removed :cards)
      ((dj :a)))
    (when (new-interest)
      ((dj :syn)))
    (when (lost-interest)
      ((dj :b)))
    (when (confirmed)
      ((dj :throb)))))
