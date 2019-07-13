(ns distributed.raft.leader
(:require [clojure.set :as set]
            [clojure.string :as str]
            [distributed.raft.protocol :refer [RaftState ->clj]]
            [distributed.raft.state-factory :as factory]
            [distributed.raft.rpc :as rpc]))


(defrecord Leader [global-state config]
RaftState
  (state[_] :leader)
  (init [_])
   (handle-append-entries [_ request respond-to])
  (handle-vote-request [_ request respond-to])
  )





