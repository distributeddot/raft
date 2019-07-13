(ns distributed.raft.state-factory)

(declare ->Follower)
(declare ->Candidate)
(declare ->Leader)

(defn make-follower
  [global-state config]
  (->Follower {:global-state global-state :config config}))

(defn make-candidate
  [global-state config]
  (->Candidate {:global-state global-state :config config}))

(defn make-leader
  [global-state config]
  (->Leader {:global-state global-state :config config}))


(defn change-state [statename global-state config]
  (let [state (case statename
                :Leader (make-leader global-state config)
                :Candidate (make-candidate global-state config)
                :Follower (make-follower global-state config)
                )]
    (swap! global-state assoc :current-state state)
    (init state)
    state
    )
  )

