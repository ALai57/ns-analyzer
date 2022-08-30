# NS-Analyzer

A static namespace analyzer/visualization tool for "seeing" a Clojure project's
architecture.

Useful for visualizing how your namespaces depend on one another, and grouping
them into meaningful units.

Requires Graphviz/Dot for rendering (since it uses
[Rhizome](https://github.com/ztellman/rhizome) under the hood)

![Example output](example-graph.svg)


Point it at the right place in your codebase, then use regular expressions to define what namespaces belong to which "cluster".

``` clojure
(require '[ns-analyzer.core :refer :all])

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
                                                            (pretty-print-ns))})
```
