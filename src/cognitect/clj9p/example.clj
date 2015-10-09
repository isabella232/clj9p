(ns cognitect.clj9p.example
  (:require [cognitect.clj9p.server :as server]
            [cognitect.clj9p.proto :as proto]))

;; NOTES:
;; ----------
;; You should prefer to send transit-msgpack instead of String data

(def serv (server/server {:app {:greeting "Hello World!"}
                          :ops {:stat server/stat-faker
                                :walk server/path-walker
                                :read server/interop-dirreader}
                          :fs {{:type proto/QTDIR :version 0
                                :path "/interjections"} {}
                               {:type proto/QTFILE :version 0
                                :path "/interjections/hello"} {:read (fn [context qid]
                                                                       (let [greeting (get-in context [:server-state :app :greeting])]
                                                                         (server/make-resp context {:type :rread
                                                                                                    :data greeting})))
                                                               :write (fn [context qid]
                                                                        (let [current-greeting (get-in context [:server-state :app :greeting])
                                                                              {:keys [data offset]} (:input-fcall context)
                                                                              greeting-data (if (string? data) data (String. data "UTF-8"))
                                                                              new-greeting (if (zero? offset)
                                                                                             greeting-data
                                                                                             (str (subs current-greeting 0 offset) greeting-data))]
                                                                          (-> context
                                                                              (assoc-in [:server-state :app :greeting] new-greeting)
                                                                              (server/make-resp {:type :rwrite
                                                                                                 :count (count new-greeting)}))))}
                               {:type proto/QTFILE :version 0
                                :path "/interjections/goodbye"} {:read (fn [context qid]
                                                                         (server/make-resp context {:type :rread
                                                                                                    :data "Goodbye!"}))}}}))

(def tcp-serv (server/tcp-server {:flush-every 0
                                  :backlog 100
                                  :reuseaddr true
                                  :port 9090
                                  :host "127.0.0.1"
                                  :join? false}
                                 serv))

(comment
  (require '[cognitect.net.netty.server :as netty])
  (netty/start tcp-serv)

  (require '[cognitect.clj9p.client :as clj9p] :reload)
  (def cl (clj9p/client))
  (clj9p/mount cl {"/nodes" [(clj9p/tcp-connect {:host "127.0.0.1" :port 9090})]})
  (map :name (clj9p/ls cl "/nodes/interjections"))
  )
