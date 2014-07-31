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

(defn set-builder
  [acc [k v]]
  (update-in acc [k] conj-set v))

(defn all-activities
  "Creates a map of activity to a set of users."
  [users]
  (let [activity-user (for [[username {:keys [activities]}] users
                            activity activities]
                        [activity username])]
    (reduce set-builder {} activity-user)))

(defn classify-activity
  "Splits activities into categories."
  [people-sets me pair people]
  (donc
   :paired (and (people me) pair (people pair))
   :invited (and (people me) (some (:inviting people-sets) (disj people me)))
   :shared (and (people me) (some (:available people-sets) (disj people me)))
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
        person_type (fn a-person-classifier [person]
                      (classify-person users me confirmed person))
        people-sets (modify-vals (group-by person_type uids) set)
        paired (and confirmed (= me (get-in users [confirmed :confirmed])))
        participation (fn an-activity-classifier [[activity people]]
                        (classify-activity people-sets me (when paired confirmed) people))
        categorized (group-by participation activity->people)
        maps (modify-vals categorized #(into {} %))
        with-contact (if paired
                       (assoc maps
                         :contact {confirmed (get-in users [confirmed :contact-me])})
                       maps)]
    (assoc with-contact :people people-sets)))

(defn states->views
  "Builds all user views out of all user states. Returns a map of user->view."
  [users uids]
  (let [activity->people (all-activities users)
        views (for [me (remove nil? uids)]
                [me (view users uids activity->people me)])]
    (into {} views)))
