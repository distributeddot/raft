(ns distributed.raft.follower
  (:require [ruiyun.tools.timer :refer [timer run-task! cancel!]]
            [distributed.raft.protocol :refer [RaftState init]]
            [distributed.raft.rpc :as rpc]
            [distributed.raft.state-factory :as factory]))

(defn cancel-heartbeat-timer
  [global-state]
  (when (:heartbeat-timer @global-state)
    (println "Cancelling heartbeat timer")
    (cancel! (:heartbeat-timer @global-state))
    (swap! global-state assoc :heartbeat-timer nil)))

(defn start-heartbeat-timer
  [global-state {:keys [heartbeat-time-ms] :as config}]
  (cancel-heartbeat-timer global-state)
  (let [hb-timer (timer "heartbeat-timer")]
    (run-task! #(do
                  (println "heartbeat expired")
                  (cancel-heartbeat-timer global-state)
                  (let [candidate (factory/make-candidate global-state config) ]
                    (swap! global-state assoc :current-state candidate)
                    (init candidate)))
               :period heartbeat-time-ms :dealy heartbeat-time-ms :by hb-timer)
    (swap! global-state assoc :heartbeat-timer hb-timer)))

(defrecord Follower 
           [global-state config]
  RaftState
  (state 
   [_] 
   :follower)

  (init
    [_]
    (start-heartbeat-timer global-state config))

  (handle-append-entries
   [_ request respond-to]
   (let [{:keys [current-term heartbeat-timer]} @global-state
         term (.getTerm request)
         entries (.getAllEntries request)]
     (cond
       (< term current-term) (rpc/send-grpc-response respond-to (rpc/build-append-response {:term current-term :success false}))
       (empty? entries) (do (start-heartbeat-timer global-state config)
                            (rpc/build-append-response {:term current-term :success true}))
       :else (println "Cant deal with real entries right now"))))

  (handle-vote-request 
    [_ request respond-to]))


#_(start-heartbeat-timer 1000)
#_(cancel-heartbeat-timer)