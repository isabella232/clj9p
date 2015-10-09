(ns cognitect.clj9p.9p)

;; Auxiliary functions extracted from `distributed`s util
;; -------------------------------------------------------
(def ^:dynamic *deep-merge-fn* last)

(defn deep-merge
  "Merge any number of values. When `vals` are maps, this performs a recursive
  merge. When `vals` are not maps, `*deep-merge-fn*` is used to choose a
  winner.

  By default, `*deep-merge-fn*` is bound to `last`, which means the last value
  of `vals` will be chosen.

  You can specify your own merge strategy by binding `*deep-merge-fn*` to
  another function."
  [& vals]
  (if (every? map? (keep identity vals))
    (apply merge-with deep-merge vals)
    (*deep-merge-fn* vals)))

;; The following protocols are used when construcion a Namespace to
;; serve/expose.
;; ------------------------------------

;(defprotocol Entry
;  (open [t])
;  (read [t])
;  (write [t body])
;  (qid [t])
;  (children [t]))

;; TODO: Can `append` and `remove` somehow be mapped to clojure.core functions instead?
;(defprotocol Directory
;  (append [t entry])
;  (remove [t entry]))

;; The following protocols are used when connecting, mounting, and using remote
;; namespaces.
;; -------------------------

(defprotocol Mountable ;; Filesystem endpoints for clients
  (-mount [t from-server-chan]))
;(defprotocol Attachable ;; filesystem
;  (attach [t]))
;(defprotocol Filesystem
;  (root [t])
;  (make-fs [t]))

(defprotocol Remote
  (get-remote-id [t]))

;; Auxiliary functions
;; ---------------------

