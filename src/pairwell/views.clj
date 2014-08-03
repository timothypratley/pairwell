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

(defn update-set
  "Given a map of sets, conjs a value to the set identified by a key."
  [acc [k v]]
  (update-in acc [k] conj-set v))

(defn participation
  "Creates a map of activity title to a set of usernames with interest in that activty."
  [users]
  (let [activity-user (for [[username {:keys [activities]}] users
                            activity activities]
                        [activity username])]
    (reduce update-set {} activity-user)))

(defn classify-activity
  "Labels activities by the interest expressed by me and other people."
  [{:keys [inviting available]} me pair people]
  (donc
   :paired (and (people me) pair (people pair))
   :invited (and (people me) inviting (some inviting (disj people me)))
   :shared (and (people me) available (some available (disj people me)))
   :joined (people me)
   :available :default))

(defn classify-person
  "Labels people by their relationship with me."
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
  "Aggregates over all user states to calculate what a single user should see.
  Returns a map classifying activities and people by their relationship with the user.
  {:activities {:paired ps, :joined js, :available as},
   :people {:available #{}},
   :contact {person contact-by}"
  [users activity->people me]
  (let [confirmed (get-in users [me :confirmed])
        relationship (fn a-person-classifier [person]
                       (classify-person users me confirmed person))
        relationships (modify-vals (group-by relationship (keys users)) set)
        paired (and confirmed (= me (get-in users [confirmed :confirmed])))
        pair (when paired confirmed)
        interest-level (fn an-activity-classifier [[activity people]]
                         (classify-activity relationships me pair people))
        act-kvps (group-by interest-level activity->people)
        activities (modify-vals act-kvps #(into {} %))
        contact (when paired {confirmed (get-in users [confirmed :contact])})]
    {:activities activities
     :people relationships
     :contact contact}))

;; TODO: do I really need uids at all?
(defn states->views
  "Builds all user views out of all user states. Returns a map of user->view."
  [users uids]
  (let [activity->people (participation users)
        uids (remove nil? uids)
        views (for [me uids]
                [me (view users activity->people me)])]
    (into {} views)))
