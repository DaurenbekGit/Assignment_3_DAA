import java.util.*;

/**
 * Assignment 3 - Graph Algorithms (Java 17).
 * Every task uses fresh local state. Original edge ID = index in input edges.
 */
public final class Algorithms {
    private Algorithms() {}

    // Shared DSU for connectivity and Kruskal.
    private static final class DSU {
        final int[] parent;
        final int[] size;
        int components;

        DSU(int n) {
            parent = new int[n];
            size = new int[n];
            components = n;
            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        int find(int x) {
            int root = x;
            while (root != parent[root]) {
                root = parent[root];
            }
            while (x != root) {
                int next = parent[x];
                parent[x] = root;
                x = next;
            }
            return root;
        }

        boolean union(int a, int b) {
            a = find(a);
            b = find(b);
            if (a == b) return false;
            if (size[a] < size[b]) {
                int temp = a;
                a = b;
                b = temp;
            }
            parent[b] = a;
            size[a] += size[b];
            components--;
            return true;
        }
    }

    /** Return [components, largest component size] after every union. */
    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        DSU dsu = new DSU(n);
        int largest = n == 0 ? 0 : 1;
        List<int[]> states = new ArrayList<>();

        for (int[] operation : operations) {
            int a = dsu.find(operation[0]);
            int b = dsu.find(operation[1]);
            if (a != b) {
                dsu.union(a, b);
                largest = Math.max(largest, dsu.size[dsu.find(a)]);
            }
            states.add(new int[]{dsu.components, largest});
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("states", states);
        return result;
    }

    /** Minimum spanning forest: sort original edge IDs and use DSU. */
    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);
        Integer[] ids = new Integer[edges.length];
        for (int i = 0; i < ids.length; i++) ids[i] = i;
        Arrays.sort(ids, Comparator.comparingInt(id -> edges[id][2]));

        DSU dsu = new DSU(n);
        long cost = 0L;
        List<Integer> selected = new ArrayList<>();
        for (int id : ids) {
            int[] edge = edges[id];
            if (dsu.union(edge[0], edge[1])) {
                cost += (long) edge[2];
                selected.add(id);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", selected);
        result.put("components", dsu.components);
        return result;
    }

    // Prim heap entry: destination, edge cost, original edge ID.
    private static final class Candidate {
        final int to;
        final int weight;
        final int id;

        Candidate(int to, int weight, int id) {
            this.to = to;
            this.weight = weight;
            this.id = id;
        }
    }

    /** Minimum spanning forest: adjacency lists and a min-priority queue. */
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        List<Integer>[] incident = emptyGraph(n);
        for (int id = 0; id < edges.length; id++) {
            int u = edges[id][0], v = edges[id][1];
            if (u == v) continue; // A self-loop cannot be in a forest.
            incident[u].add(id);
            incident[v].add(id);
        }

        boolean[] visited = new boolean[n];
        PriorityQueue<Candidate> pq = new PriorityQueue<>(
                Comparator.comparingInt((Candidate c) -> c.weight)
                        .thenComparingInt(c -> c.id));
        List<Integer> selected = new ArrayList<>();
        long cost = 0L;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            components++; // Includes vertices with no incident edges.
            visited[start] = true;
            addCandidates(start, edges, incident, visited, pq);

            while (!pq.isEmpty()) {
                Candidate best = pq.poll();
                if (visited[best.to]) continue;
                visited[best.to] = true;
                cost += (long) best.weight;
                selected.add(best.id);
                addCandidates(best.to, edges, incident, visited, pq);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", selected);
        result.put("components", components);
        return result;
    }

    private static void addCandidates(int u, int[][] edges,
                                      List<Integer>[] incident, boolean[] visited,
                                      PriorityQueue<Candidate> pq) {
        for (int id : incident[u]) {
            int[] e = edges[id];
            int v = (e[0] == u) ? e[1] : e[0];
            if (!visited[v]) pq.add(new Candidate(v, e[2], id));
        }
    }

    /** Kosaraju: iterative finish-order DFS, then DFS on transpose. */
    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);
        List<Integer>[] graph = emptyGraph(n);
        List<Integer>[] reversed = emptyGraph(n);
        for (int[] e : edges) {
            graph[e[0]].add(e[1]);
            reversed[e[1]].add(e[0]);
        }

        int[] finishOrder = finishOrder(graph);
        int[] component = new int[n];
        Arrays.fill(component, -1);
        int[] stack = new int[n];
        int label = 0;

        for (int i = n - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (component[start] != -1) continue;

            int top = 0;
            stack[top] = start;
            component[start] = label;
            while (top >= 0) {
                int u = stack[top--];
                for (int v : reversed[u]) {
                    if (component[v] == -1) {
                        component[v] = label;
                        stack[++top] = v;
                    }
                }
            }
            label++;
        }

        List<int[]> condensation = new ArrayList<>();
        Set<Long> seenPairs = new HashSet<>();
        for (int[] e : edges) {
            int from = component[e[0]], to = component[e[1]];
            if (from == to) continue;
            long key = ((long) from << 32) | (to & 0xffffffffL);
            if (seenPairs.add(key)) condensation.add(new int[]{from, to});
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", component);
        result.put("condensation", condensation);
        return result;
    }

    /** Iterative DFS postorder; avoids stack overflow on deep chains. */
    private static int[] finishOrder(List<Integer>[] graph) {
        int n = graph.length;
        boolean[] visited = new boolean[n];
        int[] nextIndex = new int[n];
        int[] stack = new int[n];
        int[] order = new int[n];
        int count = 0;

        for (int start = 0; start < n; start++) {
            if (visited[start]) continue;
            int top = 0;
            stack[0] = start;
            visited[start] = true;
            while (top >= 0) {
                int u = stack[top];
                if (nextIndex[u] < graph[u].size()) {
                    int v = graph[u].get(nextIndex[u]++);
                    if (!visited[v]) {
                        visited[v] = true;
                        stack[++top] = v;
                    }
                } else {
                    order[count++] = u;
                    top--;
                }
            }
        }
        return order;
    }

    /** DFS topological ordering with 3 states and full-graph cycle check. */
    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);
        List<Integer>[] graph = emptyGraph(n);
        for (int[] e : edges) graph[e[0]].add(e[1]);
        int[] order = topologicalOrder(graph);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acyclic", order != null);
        result.put("order", order == null ? new int[0] : order);
        return result;
    }

    // Returns null if a cycle exists (including unreachable components).
    private static int[] topologicalOrder(List<Integer>[] graph) {
        int n = graph.length;
        byte[] state = new byte[n]; // 0 unseen, 1 active, 2 complete.
        int[] nextIndex = new int[n];
        int[] stack = new int[n];
        int[] result = new int[n];
        int count = 0;

        for (int start = 0; start < n; start++) {
            if (state[start] != 0) continue;
            int top = 0;
            stack[0] = start;
            state[start] = 1;
            while (top >= 0) {
                int u = stack[top];
                if (nextIndex[u] < graph[u].size()) {
                    int v = graph[u].get(nextIndex[u]++);
                    if (state[v] == 1) return null;
                    if (state[v] == 0) {
                        state[v] = 1;
                        stack[++top] = v;
                    }
                } else {
                    state[u] = 2;
                    result[count++] = u;
                    top--;
                }
            }
        }
        for (int left = 0, right = n - 1; left < right; left++, right--) {
            int temp = result[left];
            result[left] = result[right];
            result[right] = temp;
        }
        return result;
    }

    /** Shortest paths from source, only if the entire directed graph is a DAG. */
    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);
        List<Integer>[] graph = emptyGraph(n);
        List<Integer>[] outgoing = emptyGraph(n);
        for (int id = 0; id < edges.length; id++) {
            graph[edges[id][0]].add(edges[id][1]);
            outgoing[edges[id][0]].add(id);
        }

        int[] order = topologicalOrder(graph);
        Map<String, Object> result = new LinkedHashMap<>();
        if (order == null) {
            result.put("acyclic", false);
            result.put("distance", new Long[0]);
            result.put("parent_edge", new int[0]);
            return result;
        }

        Long[] distance = new Long[n]; // null means unreachable.
        int[] parentEdge = new int[n];
        Arrays.fill(parentEdge, -1);
        distance[source] = 0L;

        for (int u : order) {
            if (distance[u] == null) continue;
            for (int id : outgoing[u]) {
                int v = edges[id][1];
                long candidate = distance[u] + (long) edges[id][2];
                if (distance[v] == null || candidate < distance[v]) {
                    distance[v] = candidate;
                    parentEdge[v] = id;
                }
            }
        }

        result.put("acyclic", true);
        result.put("distance", distance);
        result.put("parent_edge", parentEdge);
        return result;
    }

    /**
     * Reconstruct a route as vertex IDs, using original input edge IDs.
     * Returns [] if the target is unreachable; [source] for source itself.
     */
    static List<Integer> reconstructRoute(int source, int target,
                                          int[] parentEdge, int[][] edges) {
        if (target < 0 || target >= parentEdge.length
                || source < 0 || source >= parentEdge.length) {
            return Collections.emptyList();
        }
        List<Integer> route = new ArrayList<>();
        int current = target;
        for (int steps = 0; steps <= parentEdge.length; steps++) {
            route.add(current);
            if (current == source) {
                Collections.reverse(route);
                return route;
            }
            int id = parentEdge[current];
            if (id < 0 || id >= edges.length || edges[id][1] != current) {
                return Collections.emptyList();
            }
            current = edges[id][0];
        }
        return Collections.emptyList(); // Defensive guard against bad parents.
    }

    @SuppressWarnings("unchecked")
    private static List<Integer>[] emptyGraph(int n) {
        List<Integer>[] graph = (List<Integer>[]) new List[n];
        for (int i = 0; i < n; i++) graph[i] = new ArrayList<>();
        return graph;
    }
}