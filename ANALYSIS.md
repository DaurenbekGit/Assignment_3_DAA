# Assignment 3 — Graph Algorithms for a Campus Festival

**Course:** Design and Analysis of Algorithms  
**Assignment:** Assignment 3 — Graph Algorithms  
**Programming Language:** Java 17  
**Development Environment:** IntelliJ IDEA, Windows  
**JDK:** Microsoft OpenJDK 17.0.18  
**Implementation:** `src/Java/Algorithms.java`  
**Date:** October 25, 2026

---

## 1. Project Overview

This project implements six graph algorithms in Java 17 for a campus festival scenario.

The algorithms solve problems involving venue connectivity, minimum-cost cable networks, preparation dependencies, and shortest delivery routes.

The implemented algorithms are:

1. Disjoint Set Union (DSU)
2. Kruskal's Minimum Spanning Forest
3. Prim's Minimum Spanning Forest
4. Kosaraju's Strongly Connected Components
5. DFS Topological Ordering
6. DAG Shortest Paths

The implementation uses standard Java collections, arrays, sorting, priority queues, and iterative graph traversal.

The provided `Main.java`, `Input.java`, and `Json.java` files handle the program interface and JSON processing.

**Verified results:**

- 36/36 public tests passed.
- The official state-reset check passed.
- Twelve custom JSON Lines test cases produced the expected results.
- Kruskal and Prim were benchmarked at three graph sizes.
- Eighteen timing measurements were recorded.

Additional route-helper, full-limit, and semantic witness tests are identified separately because their execution has not yet been recorded.

## 2. Program Interface and Constraints

The application uses a UTF-8 JSON Lines interface.

Each input line represents one independent request, and the program returns one JSON result per line.

The application supports these tasks:

| Task | Algorithm | Output |
|---|---|---|
| `dsu` | Disjoint Set Union | `states` |
| `kruskal` | Kruskal | `cost`, `edge_ids`, `components` |
| `prim` | Prim | `cost`, `edge_ids`, `components` |
| `scc` | Kosaraju | `component`, `condensation` |
| `topo` | DFS Topological Ordering | `acyclic`, `order` |
| `dag` | DAG Shortest Paths | `acyclic`, `distance`, `parent_edge` |

The original `ping` operation is preserved.

### Input Constraints

- Maximum number of vertices: 100,000
- Maximum number of edges or DSU operations: 250,000
- Edge weights: -1,000,000,000 to 1,000,000,000
- Vertex indices: 0 to n-1
- Empty graphs are supported except for DAG requests, which require at least one vertex.
- Self-loops, parallel edges, disconnected graphs, and negative weights are handled where applicable.

Original edge IDs are preserved as their positions in the input edge array.

Weighted totals and shortest-path distances use Java `long` values to prevent 32-bit integer overflow.

The implementation creates fresh local data structures for every request.

---

## 3. Disjoint Set Union (DSU)

### 3.1 Purpose

Disjoint Set Union maintains groups of connected vertices.

In the campus festival scenario, vertices represent venues, while union operations represent connections between venues.

### 3.2 Algorithm Steps

1. Initialize every vertex as its own parent.
2. Initialize the size of every component to one.
3. Find the representative root of each vertex in a union operation.
4. If the roots are different, merge the smaller component into the larger component.
5. Decrease the number of connected components.
6. Update the size of the largest component.
7. Record the current number of components and the largest component size.

The implementation uses two optimizations:

**Path compression:** When finding a root, update visited vertices to point directly to their representative.

**Union by size:** Attach the smaller component to the larger component.

### 3.3 Invariant

Every connected component has exactly one representative root.

The size stored at a representative root equals the number of vertices in its component.

The component counter equals the number of disjoint sets.

### 3.4 Correctness

Initially, every vertex forms its own component, so all invariants hold.

For each union operation, there are two cases.

If both vertices have the same root, they already belong to the same component. No change is necessary.

If their roots differ, the algorithm merges exactly two components and decreases the component counter by one.

The new component size is the sum of the two previous sizes.

Path compression changes parent pointers without changing component membership.

Therefore, DSU correctly maintains the component count and largest component size after every operation.

### 3.5 Boundary Cases

- Empty graph
- Self-union
- Repeated unions
- Disconnected vertices
- Large numbers of union operations

### 3.6 Complexity

Let n be the number of vertices and q the number of operations.

**Time:** O(n + q α(n))

**Memory:** O(n + q), including the output states.

The inverse Ackermann function α(n) grows extremely slowly.

---

## 4. Kruskal's Minimum Spanning Forest

### 4.1 Purpose

Kruskal's algorithm finds a minimum spanning forest in an undirected weighted graph.

It can be used to connect campus festival venues at minimum total cable cost.

A minimum spanning forest contains a minimum spanning tree for each connected component, including isolated vertices as separate components.

### 4.2 Algorithm Steps

1. Assign every edge its original input ID.
2. Sort all edge IDs by their corresponding weights.
3. Initialize a DSU structure.
4. Process edges in nondecreasing weight order.
5. Find the representative components of both endpoints.
6. Select an edge only if its endpoints belong to different components.
7. Merge these components.
8. Add the selected edge weight to the total cost.
9. Return the total cost, original selected edge IDs, and number of components.

### 4.3 Invariant

At every step, the selected edges form an acyclic forest.

Each selected edge is safe for inclusion in a minimum spanning forest.

### 4.4 Correctness

Initially, the selected forest is empty and therefore acyclic.

An edge is added only when it connects two different DSU components. Thus, adding the edge cannot create a cycle.

Since edges are processed in nondecreasing weight order, each selected edge is a minimum-weight available edge crossing an appropriate cut.

According to the cut property of minimum spanning trees, this edge can safely belong to an optimal spanning forest.

When all edges have been processed, each connected component has a minimum spanning tree.

Therefore, the algorithm returns a minimum spanning forest with minimum total cost.

### 4.5 Boundary Cases

- Empty graphs
- Isolated vertices
- Disconnected components
- Negative edge weights
- Self-loops
- Parallel edges
- Equal-weight alternatives
- Large total costs

Self-loops are rejected because both endpoints belong to the same component.

### 4.6 Complexity

Let n represent the number of vertices and m the number of edges.

**Time:** O(n + m log(m + 1))

**Memory:** O(n + m)

Sorting the edges dominates the time complexity. DSU operations have near-constant amortized complexity.

---

## 5. Prim's Minimum Spanning Forest

### 5.1 Purpose

Prim's algorithm also finds a minimum spanning forest.

Unlike Kruskal, it builds a tree by repeatedly selecting the cheapest edge connecting visited vertices to unvisited vertices.

### 5.2 Algorithm Steps

1. Build adjacency lists using original edge IDs.
2. Initialize a visited array.
3. Create a minimum priority queue.
4. Select an unvisited starting vertex.
5. Mark the starting vertex as visited.
6. Add candidate edges to the priority queue.
7. Extract the minimum-weight candidate edge.
8. If the destination is unvisited, select the edge.
9. Add its weight to the total cost.
10. Continue until the current component is complete.
11. Restart from another unvisited vertex.
12. Return the total cost, selected edge IDs, and component count.

The implementation uses a lazy priority queue and supports disconnected graphs.

### 5.3 Invariant

Within each explored component, the selected edges form a tree.

Every selected edge connects the explored tree to a previously unvisited vertex.

### 5.4 Correctness

Initially, the explored tree contains only one vertex.

At every step, Prim selects the cheapest available edge crossing from the visited set to an unvisited vertex.

By the cut property, the selected edge is safe for a minimum spanning tree.

Since each selected edge introduces a new vertex, no cycle is created.

The algorithm continues until the entire connected component has been explored.

Restarting from remaining unvisited vertices ensures that all disconnected components and isolated vertices are included.

Therefore, the algorithm produces a minimum spanning forest.

### 5.5 Boundary Cases

- Empty graphs
- Isolated vertices
- Disconnected components
- Negative weights
- Equal-weight edges
- Parallel edges
- Self-loops

### 5.6 Complexity

**Time:** O(n + m log(m + 1))

**Memory:** O(n + m)

The priority queue stores candidate edges, while adjacency lists and the visited array represent the graph and current traversal state.

---

## 6. Strongly Connected Components — Kosaraju

### 6.1 Purpose

Kosaraju's algorithm identifies strongly connected components (SCCs) in directed graphs.

An SCC is a maximal set of vertices in which every vertex can reach every other vertex.

In the campus festival scenario, SCCs can represent groups of mutually dependent preparation tasks.

### 6.2 Algorithm Steps

1. Construct the original directed graph.
2. Construct the transposed graph by reversing every edge.
3. Perform the first DFS on the original graph.
4. Record vertices according to DFS finishing order.
5. Process vertices in decreasing finishing order.
6. Perform the second DFS on the transposed graph.
7. Assign one component label to all vertices reached in each second-pass traversal.
8. Examine every original edge.
9. Add distinct connections between different SCCs to the condensation graph.

Both DFS passes use iterative traversal.

### 6.3 Invariant

During the second DFS pass, each completed traversal identifies exactly one strongly connected component.

Vertices assigned the same component label are mutually reachable.

### 6.4 Correctness

The first DFS pass determines the finishing order of vertices.

The second pass uses decreasing finishing order on the transposed graph.

This order allows each traversal to identify one maximal group of mutually reachable vertices without incorrectly merging separate SCCs.

After labeling all vertices, the algorithm examines the original directed edges.

Edges whose endpoints belong to the same SCC are ignored.

Each distinct cross-component connection is added exactly once to the condensation graph.

Therefore, the algorithm produces the correct SCC partition and condensation graph.

### 6.5 Boundary Cases

- Empty graphs
- Isolated vertices
- Self-loops
- Duplicate edges
- Multiple SCCs
- Deep directed chains

### 6.6 Complexity

**Time:** O(n + m) expected

**Memory:** O(n + m)

Two DFS passes and the transpose construction require linear time.

A hash set is used to remove duplicate condensation connections in expected constant time per edge.

---

## 7. DFS Topological Ordering

### 7.1 Purpose

Topological ordering determines a valid execution sequence for tasks with directed dependencies.

For every directed edge u → v, vertex u must appear before vertex v.

A valid topological order exists only for a Directed Acyclic Graph (DAG).

### 7.2 Algorithm Steps

The algorithm uses three DFS states:

- 0 — Unvisited
- 1 — Active
- 2 — Finished

The steps are:

1. Build directed adjacency lists.
2. Start DFS from every unvisited vertex.
3. Mark the current vertex as active.
4. Explore outgoing edges.
5. If an edge reaches an active vertex, report a directed cycle.
6. When all outgoing edges have been processed, mark the vertex as finished.
7. Record vertices in finishing order.
8. Reverse the finishing order to obtain a topological ordering.

### 7.3 Invariant

Every active vertex belongs to the current DFS path.

Every finished vertex has had all its outgoing edges processed.

### 7.4 Correctness

An edge pointing to an active vertex is a back edge.

Such an edge closes a directed path into a cycle, so the graph cannot have a topological ordering.

If no cycle is found, each vertex is recorded only after all vertices reached through its outgoing edges have been processed.

Reversing the finishing order guarantees that every directed edge points forward in the resulting order.

The algorithm begins a new DFS from every remaining unvisited vertex, ensuring that disconnected components are checked.

Therefore, the algorithm returns a valid topological ordering if and only if the graph is acyclic.

### 7.5 Boundary Cases

- Empty graphs
- Isolated vertices
- Self-loops
- Parallel edges
- Disconnected cycles
- Deep chains

### 7.6 Complexity

**Time:** O(n + m)

**Memory:** O(n + m)

The implementation uses an explicit DFS stack and stores the current adjacency-list position for each suspended traversal.

This avoids recursive call-stack overflow and repeated scanning of already processed edges.

---

## 8. DAG Shortest Paths

### 8.1 Purpose

The DAG shortest-path algorithm calculates minimum distances from a source vertex in a directed acyclic graph.

It supports positive, zero, and negative edge weights.

### 8.2 Algorithm Steps

1. Build directed adjacency lists.
2. Preserve original edge IDs.
3. Calculate a topological ordering of the entire graph.
4. If any directed cycle exists, return `acyclic=false` and empty arrays.
5. Initialize all distances to `null`.
6. Initialize all parent edges to -1.
7. Set the source distance to zero.
8. Process vertices in topological order.
9. Skip unreachable vertices.
10. Relax outgoing edges from reachable vertices.
11. Save the original edge ID whenever a shorter path is found.
12. Return distances and parent edges.

### 8.3 Invariant

Before processing a vertex in topological order, all its possible incoming predecessors have already been processed.

Thus, its shortest distance has already been finalized.

### 8.4 Correctness

The algorithm first checks the entire graph for directed cycles.

If a cycle exists, the input is not a DAG and the algorithm rejects it.

Otherwise, every edge goes from an earlier vertex to a later vertex in topological order.

When a vertex is processed, all incoming paths have already been considered.

Relaxing its outgoing edges correctly transfers shortest-path information to later vertices.

By induction over topological order, every reachable vertex receives its correct shortest distance.

Unreachable vertices retain `null`, and their parent edge remains -1.

The source receives distance zero and parent edge -1.

Negative weights do not invalidate the algorithm because the graph contains no directed cycles.

### 8.5 Boundary Cases

- Single-vertex graphs
- Unreachable vertices
- Negative weights
- Parallel edges
- Equal-cost paths
- Self-loops
- Cycles in disconnected components
- Large distance values

### 8.6 Complexity

**Time:** O(n + m)

**Memory:** O(n + m)

The topological traversal and edge relaxation each require linear time.

---

## 9. Route Reconstruction Helper

The implementation includes the Java helper:

```java
static List<Integer> reconstructRoute(
    int source,
    int target,
    int[] parentEdge,
    int[][] edges
)
```

The helper uses original parent edge IDs to reconstruct a vertex sequence.

Starting from the target, it follows the selected incoming edge backward until the source is reached.

The resulting sequence is reversed before being returned.

The helper returns:

- A source-to-target vertex sequence for a valid reachable route.
- `[source]` when the target is the source.
- An empty list when no valid route can be reconstructed.

It also checks invalid edge references and prevents unlimited traversal of malformed parent chains.

### 9.1 Reachable, Source and Unreachable Examples

Test graph:

```json
{
  "task": "dag",
  "n": 5,
  "source": 0,
  "edges": [[0,1,3],[0,2,8],[1,2,-7],[2,3,4]]
}
```

Observed DAG output:

```json
{
  "acyclic": true,
  "distance": [0,3,-4,0,null],
  "parent_edge": [-1,0,2,3,-1]
}
```

Direct helper calls to test:

```java
int[][] edges = {
    {0, 1, 3},
    {0, 2, 8},
    {1, 2, -7},
    {2, 3, 4}
};

int[] parentEdge = {-1, 0, 2, 3, -1};

Algorithms.reconstructRoute(0, 3, parentEdge, edges);
Algorithms.reconstructRoute(0, 0, parentEdge, edges);
Algorithms.reconstructRoute(0, 4, parentEdge, edges);
```

Expected results:

| Case | Expected route | Edge IDs | Weight |
|---|---|---|---|
| Reachable target 3 | [0,1,2,3] | [0,2,3] | 0 |
| Source target 0 | [0] | [] | 0 |
| Unreachable target 4 | [] | [] | No route |

The reachable path has total weight:

3 + (-7) + 4 = 0.

The edge IDs correspond to directed edges from source to target.

### 9.2 Tied and Parallel Edge Example

Test graph:

```json
{
  "task": "dag",
  "n": 3,
  "source": 0,
  "edges": [[0,1,2],[0,1,2],[1,2,3],[0,2,5]]
}
```

Observed DAG output:

```json
{
  "acyclic": true,
  "distance": [0,2,5],
  "parent_edge": [-1,0,3]
}
```

Direct helper calls to test:

```java
int[][] tiedEdges = {
    {0, 1, 2},
    {0, 1, 2},
    {1, 2, 3},
    {0, 2, 5}
};

Algorithms.reconstructRoute(
    0, 2, new int[]{-1, 0, 3}, tiedEdges
);

Algorithms.reconstructRoute(
    0, 2, new int[]{-1, 1, 2}, tiedEdges
);
```

Expected outcomes:

- Direct route `[0,2]`, selected edge ID 3, weight 5.
- Alternative route `[0,1,2]`, selected edge IDs 1 and 2, weight 5.

Both are valid shortest routes.

### 9.3 Complexity and Verification

For a valid reconstructed route containing L vertices:

**Time:** O(L)

**Output space:** O(L)

**Verification status:** The DAG output arrays were observed during custom testing. The direct helper calls above have not yet been executed separately, so these are expected results rather than recorded helper outputs.

---

## 10. Testing and Validation

### 10.1 Public Tests

The official checking commands were executed:

```powershell
py -3 tools/run.py --setup
py -3 tools/run.py --check
```

Observed results:

| Verification | Result |
|---|---|
| Setup — ping | PASS |
| Public test cases | 36/36 PASS |
| State reset | PASS |

The public tests covered:

- Published examples
- Empty graphs
- Disconnected components
- Isolated vertices
- Repeated union operations
- Self-loops
- Parallel edges
- Negative weights
- Tied optimal solutions
- 64-bit weighted totals
- Directed cycles
- Cycles unreachable from the source
- Deep chains

The public checker also successfully executed two complete batches in one process.

### 10.2 Custom Tests

Twelve additional test cases were created in:

`tests/my_cases.jsonl`

Command:

```powershell
py -3 tools/run.py --input tests/my_cases.jsonl
```

The outputs were saved in:

`tests/actual_outputs.txt`

The file contains twelve result lines.

The expected results were determined independently from the graph structures and compared with the observed outputs.

### 10.3 Custom Test Results

| No. | Test | Expected result | Observed |
|---|---|---|---|
| 1 | DSU merging | [[4,2],[3,3],[3,3],[2,3]] | Match |
| 2 | Empty DSU | Empty states | Match |
| 3 | Kruskal, disconnected graph | Cost 3, components 2 | Match |
| 4 | Prim, same graph | Cost 3, components 2 | Match |
| 5 | Kosaraju SCC | Four SCCs and two condensation edges | Match |
| 6 | Topological ordering | Valid ordering | Match |
| 7 | Topological cycle | Cycle detected | Match |
| 8 | DAG negative weights | [0,3,-4,0,null] | Match |
| 9 | Unreachable DAG cycle | Cycle detected | Match |
| 10 | Kruskal 64-bit total | 3000000000 | Match |
| 11 | Prim 64-bit total | 3000000000 | Match |
| 12 | DAG tied/parallel paths | [0,2,5] | Match |

### 10.4 Explanation of Selected Tests

**DSU merging**

Five vertices initially form five components.

Union `[0,1]` reduces the count to four and creates a largest component of size two.

Union `[1,2]` reduces the count to three and increases the largest size to three.

Repeating the connection `[0,2]` changes nothing.

Union `[3,4]` reduces the total component count to two.

Expected states:

`[[4,2],[3,3],[3,3],[2,3]]`

**Kruskal and Prim**

For the weighted graph containing edges of weights 4, 2, 3, -2 and a negative self-loop, the correct spanning forest uses the weight-2, weight-3 and weight-(-2) edges.

Its total cost is:

2 + 3 - 2 = 3.

There are two connected components.

Observed Kruskal output:

```json
{"cost":3,"edge_ids":[3,1,2],"components":2}
```

Observed Prim output:

```json
{"cost":3,"edge_ids":[2,1,3],"components":2}
```

Both outputs represent the same optimal forest. The selected edge order differs, but the cost and components agree.

**Strongly Connected Components**

The directed test graph has the SCCs:

- {0,1}
- {2,3}
- {4}
- {5}

Observed output:

```json
{
  "component": [1,1,2,2,3,0],
  "condensation": [[1,2],[2,3]]
}
```

The output correctly groups mutually reachable vertices and records the distinct cross-component connections.

**Topological Ordering**

Observed output:

```json
{"acyclic":true,"order":[4,1,0,2,3]}
```

Every vertex occurs exactly once, and every directed edge respects the ordering.

**DAG Shortest Paths**

Observed output:

```json
{
  "acyclic": true,
  "distance": [0,3,-4,0,null],
  "parent_edge": [-1,0,2,3,-1]
}
```

The result correctly handles a negative edge weight and an unreachable vertex.

### 10.5 Non-Unique Answer Validation

Some graph problems have multiple valid solutions.

Therefore, a correct checker must validate solution properties rather than requiring one exact ordering of IDs or labels.

**Minimum Spanning Forest**

Consider a triangle where all three edges have weight 1.

Valid alternatives include:

- Edge IDs `[0,1]`, cost 2.
- Edge IDs `[0,2]`, cost 2.

An invalid witness is `[0,1,2]`, because these three edges create a cycle.

A forest validator must check the original edge IDs, absence of cycles, coverage of every connected component, edge count `n - components`, total cost and optimality.

**Strongly Connected Components**

Component labels may be renamed without changing the solution.

A correct validator checks that every label represents exactly one SCC and that the condensation graph contains precisely the distinct cross-component pairs.

**Topological Ordering**

For directed edges `0→2` and `1→2`, the following orders are both valid:

- `[0,1,2]`
- `[1,0,2]`

The order `[2,0,1]` is invalid because vertex 2 appears before its predecessors.

**DAG Shortest Paths**

Two equal-cost routes can have different parent edge IDs.

A valid parent witness must use actual directed edges, reach the source, and have the reported shortest total weight.

These examples explain acceptance of alternative valid answers and rejection of invalid witnesses.

**Verification status:** The semantic conditions and examples are documented. A separate automated checker demonstrating the acceptance and rejection of these witnesses has not yet been executed.

---

## 11. State Reset and Deep DFS

### 11.1 State Reset

Every task constructs fresh local data structures.

The implementation does not retain graph-specific state between independent input requests.

Examples include:

- DSU parent and size arrays
- Sorting arrays for Kruskal
- Priority queues and visited arrays for Prim
- Component labels for SCC
- DFS color and stack arrays
- DAG distance and parent-edge arrays

The official checker successfully ran repeated batches in one process.

**Observed result: PASS state reset.**

### 11.2 Deep DFS

Recursive DFS may cause stack overflow when processing long chains.

The implementation uses iterative DFS with explicit stacks.

The traversal stores each vertex's current adjacency-list position, ensuring that suspended DFS calls resume without repeatedly scanning previously processed edges.

The official checker included deep-chain public tests of 2,000 vertices for SCC, topological ordering, and DAG shortest paths.

All these tests passed.

The benchmark also exercised Kruskal and Prim on graphs containing 100,000 vertices and 250,000 edges.

Full-limit custom tests for DSU, SCC, topological ordering, and DAG remain to be recorded separately.

---

## 12. Kruskal vs Prim Performance Benchmark

### 12.1 Objective

The purpose of the benchmark is to compare the execution times of Kruskal and Prim on the same undirected weighted graphs.

The benchmark uses three input sizes, with three timed executions per algorithm per size.

### 12.2 Benchmark Files

Java benchmark source:

`tests/Benchmark.java`

Recorded measurements:

`tests/benchmark_results.txt`

The benchmark generates reproducible graphs and measures both implementations using Java 17.

### 12.3 Graph Generation

The generator uses the constant seed:

`20261025`

The random generator is initialized with:

```java
new Random(SEED + n)
```

Each graph is constructed by:

1. Creating a chain connecting all n vertices.
2. Adding random undirected edges until the required edge count is reached.
3. Assigning random integer weights from -1000 to 1000.
4. Excluding self-loops from the additional generated edges.

Parallel edges are possible.

The initial chain guarantees graph connectivity.

The same graph input is passed to both algorithms.

### 12.4 Graph Sizes

| Size | Vertices | Edges |
|---|---:|---:|
| Small | 1,000 | 3,000 |
| Medium | 10,000 | 30,000 |
| Large | 100,000 | 250,000 |

The large benchmark reaches the maximum published vertex and edge limits.

### 12.5 Benchmark Environment

| Property | Value |
|---|---|
| Operating System | Windows |
| IDE | IntelliJ IDEA |
| Processor | 12th Gen Intel Core i7-12700H |
| Installed RAM | 16 GB (15.63 GiB reported) |
| Programming Language | Java 17 |
| JDK | Microsoft OpenJDK 17.0.18 |
| Timer | System.nanoTime() |
| Random Seed | 20261025 |
| Warm-up | One call per algorithm per graph size |
| Timed Repetitions | Three per algorithm per size |

The benchmark was run locally using the Java runtime.

### 12.6 Measurement Procedure

For each graph size:

1. Generate a weighted graph outside the timed section.
2. Perform one untimed Kruskal call.
3. Perform one untimed Prim call.
4. Compare the returned total costs and component counts.
5. Run three timed calls per method, alternating Kruskal and Prim.
6. Convert nanoseconds into milliseconds.
7. Sort the three measurements for each algorithm.
8. Report the middle measurement as the median.

A mismatch in the returned cost or component count causes the benchmark to throw an exception.

The timer starts immediately before the algorithm method call and stops immediately after that call returns.

The measured time includes:

- Input conversion inside the algorithm
- Graph data-structure construction inside the method
- Sorting or priority-queue operations
- Algorithm computation
- Result construction

The measurements exclude:

- Java compilation
- Initial random graph generation
- Console output
- Writing the benchmark result file

The same timing boundary is applied to both algorithms.

### 12.7 Actual Benchmark Results

All values are in milliseconds.

| Graph Size | Kruskal Runs | Kruskal Median | Prim Runs | Prim Median |
|---|---|---:|---|---:|
| Small | 11.7176, 7.9305, 3.6430 | 7.931 | 8.2027, 6.1052, 4.0825 | 6.105 |
| Medium | 46.2978, 113.7013, 59.5948 | 59.595 | 63.1131, 35.1553, 41.1318 | 41.132 |
| Large | 344.4411, 417.3675, 357.4529 | 357.453 | 663.7387, 853.1082, 579.3699 | 663.739 |

These results are taken from the recorded benchmark execution.

### 12.8 Performance Comparison

| Graph Size | Faster Algorithm | Median Difference |
|---|---|---:|
| Small | Prim | 1.826 ms |
| Medium | Prim | 18.463 ms |
| Large | Kruskal | 306.286 ms |

Prim was faster on the small and medium graphs.

Kruskal was faster on the large graph.

### 12.9 Explanation of Observations

Kruskal and Prim use different data structures.

Kruskal sorts all input edges and uses DSU to avoid cycles.

Prim creates adjacency lists and repeatedly extracts minimum-weight candidate edges from a priority queue.

On the smaller graphs, the observed Prim execution times were lower.

On the largest graph, Kruskal produced a lower median execution time.

The differences may be influenced by:

- Sorting overhead
- Priority queue operations
- Memory allocation
- JVM optimization
- Garbage collection
- CPU cache behavior
- Input graph characteristics

The observed benchmark results do not establish that one algorithm is universally faster.

One warm-up call and three measurements per algorithm are sufficient for the assignment's minimum measurement count, but they do not eliminate all runtime variability.

Additional repetitions and different graph distributions would provide stronger experimental evidence.

### 12.10 Complexity Comparison

**Kruskal**

Time: O(n + m log(m + 1))  
Memory: O(n + m)

**Prim with a lazy priority queue**

Time: O(n + m log(m + 1))  
Memory: O(n + m)

Both algorithms have similar asymptotic complexity in these implementations.

However, practical runtime can differ because complexity notation does not represent constant factors or system-level overhead.

### 12.11 Benchmark Commands

Compile the Java benchmark:

```powershell
javac -encoding UTF-8 -d out src/Java/Main.java src/Java/Algorithms.java src/Java/Input.java src/Java/Json.java tests/Benchmark.java
```

Run the benchmark and save results:

```powershell
java -cp out Benchmark | Tee-Object -FilePath tests/benchmark_results.txt
```

**Benchmark status: Completed.**

The benchmark checked equality of costs and component counts before timing. Independent validation of every selected forest edge ID remains an additional verification item.

---

## 13. Build, Run and Test Commands

### 13.1 Java Requirements

The project requires Java 17.

Verify the environment:

```powershell
java -version
javac -version
```

The observed versions were:

```text
openjdk version "17.0.18"
javac 17.0.18
```

### 13.2 Compile Java Manually

From the project root:

```powershell
New-Item -ItemType Directory -Force out | Out-Null

javac -encoding UTF-8 -d out src/Java/Main.java src/Java/Algorithms.java src/Java/Input.java src/Java/Json.java
```

Run the program:

```powershell
java -cp out Main
```

The program reads JSON Lines from standard input.

### 13.3 Official Public Checks

```powershell
py -3 tools/run.py --setup
py -3 tools/run.py --check
```

Observed results:

- Setup: PASS
- Public tests: 36/36 PASS
- State reset: PASS

### 13.4 Custom Tests

```powershell
py -3 tools/run.py --input tests/my_cases.jsonl
```

Save results:

```powershell
py -3 tools/run.py --input tests/my_cases.jsonl | Out-File -Encoding utf8 tests/actual_outputs.txt
```

The custom test input file contains twelve requests.

The actual output file contains twelve JSON result lines.

The Python launcher is supplied by the assignment for compiling, launching, and testing the selected language.

All graph algorithms themselves are implemented in Java 17.

### 13.5 Benchmark

```powershell
javac -encoding UTF-8 -d out src/Java/Main.java src/Java/Algorithms.java src/Java/Input.java src/Java/Json.java tests/Benchmark.java

java -cp out Benchmark | Tee-Object -FilePath tests/benchmark_results.txt
```

---

## 14. Project Structure

```text
Assignment_3_DAA-main/
|
|-- src/
|   |-- Java/
|       |-- Algorithms.java
|       |-- Input.java
|       |-- Json.java
|       |-- Main.java
|
|-- tests/
|   |-- README.md
|   |-- my_cases.jsonl
|   |-- actual_outputs.txt
|   |-- Benchmark.java
|   |-- benchmark_results.txt
|
|-- tools/
|   |-- run.py
|
|-- README.md
|-- README_RU.md
|-- ANALYSIS.md
|-- submission.json
```

The main algorithm implementations are located in `src/Java/Algorithms.java`.

The test cases, their actual outputs, and the benchmark evidence are stored in the `tests/` directory.

The project is intended to run with Java 17 without external graph libraries.

---

## 15. Sources and AI Assistance

### 15.1 Sources

The following materials were used:

1. Official Assignment 3 — Graph Algorithms specification.
2. Instructor's GitHub template: https://github.com/RZhaniya/daa-assignment-3-graphs
3. Java 17 standard library.
4. The instructor-provided public checker and examples.

### 15.2 AI Assistance Disclosure

OpenAI ChatGPT was used to assist with:

- Java algorithm implementation
- Understanding graph algorithms
- Preparing custom test cases
- Debugging and IntelliJ IDEA configuration
- Creating the Java benchmark
- Drafting and reviewing this analysis

The Java program was tested locally with the official checker.

All 36 public cases passed, and twelve custom JSON Lines test cases produced the expected outputs.

The performance measurements in this report were obtained from an actual Java benchmark execution, rather than AI-generated estimates.

The author remains responsible for reviewing the implementation, understanding the algorithms, and verifying all submitted results.

---

## 16. Additional Verification Results

Additional verification was performed using `tests/AdditionalGraphTests.java`.

The following nine checks passed:

1. Route reconstruction for reachable, source, and unreachable targets.
2. Route reconstruction with tied and parallel edges.
3. DSU with 100,000 vertices and 250,000 operations.
4. Topological ordering on a 100,000-vertex deep chain.
5. Kosaraju SCC on a 100,000-vertex deep chain.
6. DAG shortest paths with 100,000 vertices and 250,000 edges.
7. Detection of a directed cycle unreachable from the source.
8. DSU state reset between requests.
9. Acceptance of alternative valid topological orders and rejection of an invalid order.

**Result: ALL 9 ADDITIONAL TESTS PASSED.**

The additional tests were executed using Java 17 with a maximum heap size of 2 GB.

**Command:**

```powershell
java -Xmx2g -cp out AdditionalGraphTests
```

**Evidence:** `tests/AdditionalGraphTests.java` and `tests/additional_results.txt`.

The official public checks, twelve custom JSON Lines examples, and Kruskal-versus-Prim benchmark were also completed.

Independent semantic witness validation for all six algorithms, including minimum spanning forest edge sets and SCC labels, has not been fully automated. The official checker nevertheless passed all its public cases.

The project is ready for a final repository and submission configuration review.

---

## 17. Conclusion

This assignment implements six graph algorithms in Java 17.

The implementation demonstrates connectivity management, minimum spanning forests, strongly connected components, topological ordering, and shortest paths in directed acyclic graphs.

It uses efficient data structures and techniques, including:

- Path compression
- Union by size
- Edge sorting
- Priority queues
- Adjacency lists
- Iterative DFS
- Topological edge relaxation
- Original edge ID preservation
- 64-bit weighted calculations

The solution passed all **36 public tests** and the official state-reset check.

Twelve additional custom test cases produced the expected results.

Kruskal and Prim were benchmarked on three graph sizes using eighteen real timing measurements.

Prim achieved lower median times on the small and medium graphs, while Kruskal was faster on the large graph during the recorded benchmark execution.

The theoretical analysis explains the algorithms' correctness and complexity. The measured results demonstrate how implementation details and graph size can influence actual runtime.

The remaining verification tasks are documented separately to distinguish completed evidence from planned checks.

Overall, the project provides a working Java 17 implementation of the required graph algorithms, supported by public tests, custom examples, correctness arguments, and measured performance results.