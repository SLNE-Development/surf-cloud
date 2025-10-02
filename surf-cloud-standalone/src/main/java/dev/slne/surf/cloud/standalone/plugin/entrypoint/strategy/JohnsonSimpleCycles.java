package dev.slne.surf.cloud.standalone.plugin.entrypoint.strategy;

import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.ArrayDeque;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Find all simple cycles of a directed graph using the Johnson's algorithm.
 *
 * <p>
 * See:<br> D.B.Johnson, Finding all the elementary circuits of a directed graph, SIAM J. Comput., 4
 * (1975), pp. 77-84.
 *
 * @param <V> the vertex type.
 * @author Nikolay Ognyanov
 */
@SuppressWarnings("all")
public class JohnsonSimpleCycles<V> {

  // The graph.
  private final Graph<V> graph;

  // The main state of the algorithm.
  private Consumer<ObjectList<V>> cycleConsumer = null;
  private BiConsumer<V, V> cycleVertexSuccessorConsumer = null; // Paper
  private V[] iToV = null;
  private Object2IntMap<V> vToI = null;
  private ObjectSet<V> blocked = null;
  private Object2ObjectMap<V, ObjectSet<V>> bSets = null;
  private ArrayDeque<V> stack = null;

  // The state of the embedded Tarjan SCC algorithm.
  private ObjectList<ObjectSet<V>> foundSCCs = null;
  private int index = 0;
  private Object2IntMap<V> vIndex = null;
  private Object2IntMap<V> vLowlink = null;
  private ArrayDeque<V> path = null;
  private ObjectSet<V> pathSet = null;

  /**
   * Create a simple cycle finder for the specified graph.
   *
   * @param graph - the DirectedGraph in which to find cycles.
   * @throws IllegalArgumentException if the graph argument is <code> null</code>.
   */
  public JohnsonSimpleCycles(Graph<V> graph) {
    Preconditions.checkState(graph.isDirected(), "Graph must be directed");
    this.graph = graph;
  }

  /**
   * Find the simple cycles of the graph.
   *
   * @return The list of all simple cycles. Possibly empty but never <code>null</code>.
   */
  public ObjectList<ObjectList<V>> findAndRemoveSimpleCycles() {
    ObjectList<ObjectList<V>> result = new ObjectArrayList<>();
    findSimpleCycles(result::add, (v, s) -> ((MutableGraph<V>) graph).removeEdge(v, s)); // Paper
    return result;
  }

  /**
   * Find the simple cycles of the graph.
   *
   * @param consumer Consumer that will be called with each cycle found.
   */
  public void findSimpleCycles(Consumer<ObjectList<V>> consumer,
      BiConsumer<V, V> vertexSuccessorConsumer) // Paper
  {
    if (graph == null) {
      throw new IllegalArgumentException("Null graph.");
    }
    cycleVertexSuccessorConsumer = vertexSuccessorConsumer; // Paper
    initState(consumer);

    int startIndex = 0;
    int size = graph.nodes().size();
    while (startIndex < size) {
      Object2IntMap.Entry<Graph<V>> minSCCGResult = findMinSCSG(startIndex);
      if (minSCCGResult != null) {
        startIndex = minSCCGResult.getIntValue();
        Graph<V> scg = minSCCGResult.getKey();
        V startV = toV(startIndex);
        for (V v : scg.successors(startV)) {
          blocked.remove(v);
          getBSet(v).clear();
        }
        findCyclesInSCG(startIndex, startIndex, scg);
        startIndex++;
      } else {
        break;
      }
    }

    clearState();
  }

  private Object2IntMap.Entry<Graph<V>> findMinSCSG(int startIndex) {
    /*
     * Per Johnson : "adjacency structure of strong component $K$ with least vertex in subgraph
     * of $G$ induced by $(s, s + 1, n)$". Or in contemporary terms: the strongly connected
     * component of the subgraph induced by $(v_1, \dotso ,v_n)$ which contains the minimum
     * (among those SCCs) vertex index. We return that index together with the graph.
     */
    initMinSCGState();

    ObjectList<ObjectSet<V>> foundSCCs = findSCCS(startIndex);

    // find the SCC with the minimum index
    int minIndexFound = Integer.MAX_VALUE;
    ObjectSet<V> minSCC = null;
    for (ObjectSet<V> scc : foundSCCs) {
      for (V v : scc) {
        int t = toI(v);
        if (t < minIndexFound) {
          minIndexFound = t;
          minSCC = scc;
        }
      }
    }
    if (minSCC == null) {
      return null;
    }

    // build a graph for the SCC found
    MutableGraph<V> dependencyGraph = GraphBuilder.directed().allowsSelfLoops(true).build();

    for (V v : minSCC) {
      for (V w : minSCC) {
        if (graph.hasEdgeConnecting(v, w)) {
          dependencyGraph.putEdge(v, w);
        }
      }
    }

    Object2IntMap.Entry<Graph<V>> result = new AbstractObject2IntMap.BasicEntry(dependencyGraph,
        minIndexFound);
    clearMinSCCState();
    return result;
  }

  private ObjectList<ObjectSet<V>> findSCCS(int startIndex) {
    // Find SCCs in the subgraph induced
    // by vertices startIndex and beyond.
    // A call to StrongConnectivityAlgorithm
    // would be too expensive because of the
    // need to materialize the subgraph.
    // So - do a local search by the Tarjan's
    // algorithm and pretend that vertices
    // with an index smaller than startIndex
    // do not exist.
    for (V v : graph.nodes()) {
      int vI = toI(v);
      if (vI < startIndex) {
        continue;
      }
      if (!vIndex.containsKey(v)) {
        getSCCs(startIndex, vI);
      }
    }
    ObjectList<ObjectSet<V>> result = foundSCCs;
    foundSCCs = null;
    return result;
  }

  private void getSCCs(int startIndex, int vertexIndex) {
    V vertex = toV(vertexIndex);
    vIndex.put(vertex, index);
    vLowlink.put(vertex, index);
    index++;
    path.push(vertex);
    pathSet.add(vertex);

    Set<V> edges = graph.successors(vertex);
    for (V successor : edges) {
      int successorIndex = toI(successor);
      if (successorIndex < startIndex) {
        continue;
      }
      if (!vIndex.containsKey(successor)) {
        getSCCs(startIndex, successorIndex);
        vLowlink.put(vertex, Math.min(vLowlink.get(vertex), vLowlink.get(successor)));
      } else if (pathSet.contains(successor)) {
        vLowlink.put(vertex, Math.min(vLowlink.get(vertex), vIndex.get(successor)));
      }
    }
    if (vLowlink.get(vertex).equals(vIndex.get(vertex))) {
      ObjectSet<V> result = new ObjectOpenHashSet<>();
      V temp;
      do {
        temp = path.pop();
        pathSet.remove(temp);
        result.add(temp);
      } while (!vertex.equals(temp));
      if (result.size() == 1) {
        V v = result.iterator().next();
        if (graph.edges().contains(vertex)) {
          foundSCCs.add(result);
        }
      } else {
        foundSCCs.add(result);
      }
    }
  }

  private boolean findCyclesInSCG(int startIndex, int vertexIndex, Graph<V> scg) {
    /*
     * Find cycles in a strongly connected graph per Johnson.
     */
    boolean foundCycle = false;
    V vertex = toV(vertexIndex);
    stack.push(vertex);
    blocked.add(vertex);

    for (V successor : scg.successors(vertex)) {
      int successorIndex = toI(successor);
      if (successorIndex == startIndex) {
        ObjectList<V> cycle = new ObjectArrayList<>(stack.size());
        stack.descendingIterator().forEachRemaining(cycle::add);
        cycleConsumer.accept(cycle);
        cycleVertexSuccessorConsumer.accept(vertex, successor); // Paper
        //foundCycle = true; // Paper
      } else if (!blocked.contains(successor)) {
        boolean gotCycle = findCyclesInSCG(startIndex, successorIndex, scg);
        foundCycle = foundCycle || gotCycle;
      }
    }
    if (foundCycle) {
      unblock(vertex);
    } else {
      for (V w : scg.successors(vertex)) {
        ObjectSet<V> bSet = getBSet(w);
        bSet.add(vertex);
      }
    }
    stack.pop();
    return foundCycle;
  }

  private void unblock(V vertex) {
    blocked.remove(vertex);
    ObjectSet<V> bSet = getBSet(vertex);
    while (bSet.size() > 0) {
      V w = bSet.iterator().next();
      bSet.remove(w);
      if (blocked.contains(w)) {
        unblock(w);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void initState(Consumer<ObjectList<V>> consumer) {
    cycleConsumer = consumer;
    iToV = (V[]) graph.nodes().toArray();
    vToI = new Object2IntOpenHashMap<>();
    blocked = new ObjectOpenHashSet<>();
    bSets = new Object2ObjectOpenHashMap<>();
    stack = new ArrayDeque<>();

    for (int i = 0; i < iToV.length; i++) {
      vToI.put(iToV[i], i);
    }
  }

  private void clearState() {
    cycleConsumer = null;
    iToV = null;
    vToI = null;
    blocked = null;
    bSets = null;
    stack = null;
  }

  private void initMinSCGState() {
    index = 0;
    foundSCCs = new ObjectArrayList<>();
    vIndex = new Object2IntOpenHashMap<>();
    vLowlink = new Object2IntOpenHashMap<>();
    path = new ArrayDeque<>();
    pathSet = new ObjectOpenHashSet<>();
  }

  private void clearMinSCCState() {
    index = 0;
    foundSCCs = null;
    vIndex = null;
    vLowlink = null;
    path = null;
    pathSet = null;
  }

  private int toI(V vertex) {
    return vToI.getInt(vertex);
  }

  private V toV(int i) {
    return iToV[i];
  }

  private ObjectSet<V> getBSet(V v) {
    // B sets typically not all needed,
    // so instantiate lazily.
    return bSets.computeIfAbsent(v, k -> new ObjectOpenHashSet<>());
  }
}
