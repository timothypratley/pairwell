(ns pairwell.views)


(def conj-set (fnil conj #{}))

(defmacro donc
  "Like cond but with arguments reversed for better formatting."
  [& clauses]
  `(clojure.core/cond ~@(mapcat reverse (partition-all 2 clauses))))

(defn modify-vals
  "Returns a map where every val has been modified by f."
  [m f]
  (into (empty m) (for [[k v] m]
                    [k (f v)])))

(defn all-activities
  "Creates a map of activity to a set of users."
  [users]
  (let [activity-user (for [[username {:keys [activities]}] users
                            activity activities]
                        [activity username])]
    (reduce (fn a-set-builder [acc [k v]]
              (update-in acc [k] conj-set v))
            {}
            activity-user)))

(defn classify-activity
  "Splits activities into categories."
  [me pair people]
  (donc
   :paired (and (people me) pair (people pair))
   :joined (people me)
   :available :default))

(defn classify-person
  "Returns a person category."
  [users me confirmed person]
  (let [invited (and confirmed (= person confirmed))
        third (get-in users [person :confirmed])]
    (donc
     :me (= person me)
     :my-pair (and invited (= me (get-in users [confirmed :confirmed])))
     :taken (and third (= person (get-in users [third :confirmed])))
     :hunted (and invited third)
     :invited (and confirmed (= person confirmed))
     :inviting (and third (= me third))
     :available :default)))

(defn view
  "A map of paired, joined, and available activities."
  [users uids activity->people me]
  (let [confirmed (get-in users [me :confirmed])
        paired (and confirmed (= me (get-in users [confirmed :confirmed])))
        cata (fn an-activity-classifier [[activity people]]
               (classify-activity me (when paired confirmed) people))
        categorized (group-by cata activity->people)
        with-contact (if paired
                       (assoc categorized
                         :contact {confirmed (get-in users [confirmed :contact])})
                       categorized)
        catp (fn a-person-classifier [person]
               (classify-person users me confirmed person))
        people (modify-vals (group-by catp uids) set)]
    (assoc with-contact :people people)))

(defn states->views
  "Builds all user views out of all user states. Returns a map of user->view."
  [users uids]
  (let [activity->people (all-activities users)
        views (for [me (remove nil? uids)]
                [me (view users uids activity->people me)])]
    (into {} views)))
