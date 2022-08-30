(ns ns-analyzer.core
  (:require [clojure.string :as str]
            [clojure.java.classpath :as cp]
            [clojure.java.io :as io]
            [clojure.tools.namespace.dependency :as dep]
            [clojure.tools.namespace.dir :as dir]
            [clojure.tools.namespace.find :as fnd]
            [clojure.tools.namespace.track :as track]
            [rhizome.viz :as viz]
            [rhizome.dot :as dot]))

(defn get-dep-graph
  ([]
   (get-dep-graph "./"))
  ([root]
   (-> (track/tracker)
       (dir/scan-dirs [(io/file root)] {:platform fnd/clj :add-all? true})
       (::track/deps))))

(defn shrink-graph
  [pred dep-graph]
  (->> dep-graph
       (filter pred)
       (reduce dep/remove-all dep-graph)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; For graphing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn find-cluster
  "Looks up which cluster a given node belongs to.

  `clusters` should be a vector of 2-tuples of the form:
  [ns-regex cluster-id]

  Iterates over the list - chcking if the node's namespace matches the regex. If
  yes, returns the associated cluster-id.

  `cluster` example (vector of vectors):
  [[#\".*http-api.*\"    :http-api]
   [#\".*entities.*\"    :core-data-structures]
   [#\".*persistence.*\" :persistence]
   [#\".*api.*\"     :core-api]
   [#\".*utils.*\"   :utilities]
   [#\".*auth.*\"    :auth]]

  `node` example (ns symbol):
  'andrewslai.utils.filesystem.core"
  [clusters node]
  (reduce (fn [s [regexp v]]
            (if (re-matches regexp s)
              (reduced v)
              s))
          (name node)
          clusters))

(defn pretty-print-ns [s] (str/replace s #"\." "\n."))

(comment

  (def dep-graph
    (get-dep-graph "../andrewslai/src/andrewslai/clj"))

  (def clusters
    [[#".*http-api.*"    :http-api]
     [#".*entities.*"    :core-data-structures]
     [#".*persistence.*" :persistence]
     [#".*\.api\..*"     :core-api]
     [#".*\.utils\..*"   :utilities]
     [#".*\.auth\..*"    :auth]])

  (viz/view-graph (keys (:dependencies dep-graph)) (:dependencies dep-graph)
                  :options             {"nodesep" "0.1"}
                  :node->cluster       (partial find-cluster clusters)
                  :cluster->descriptor (fn [node] {:label node, :bgcolor "#03c2fc"})
                  :node->descriptor    (fn [node] {:label (-> node
                                                              (str/replace #"ns-analyzer." "")
                                                              (pretty-print-ns))}))

  (spit "/Users/alai/fun.svg"
        (viz/graph->svg (keys (:dependencies dep-graph)) (:dependencies dep-graph)
                        :options             {"nodesep" "0.1"}
                        :node->cluster       (partial find-cluster clusters)
                        :cluster->descriptor (fn [node] {:label node, :bgcolor "#03c2fc"})
                        :node->descriptor    (fn [node] {:label (-> node
                                                                    (str/replace #"ns-analyzer." "")
                                                                    (pretty-print-ns))})))
  )
