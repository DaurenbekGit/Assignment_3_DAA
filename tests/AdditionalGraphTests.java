
import java.util.*;
import java.lang.reflect.Array;

public class AdditionalGraphTests {

    private static int passed = 0;

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Object item(Object values, int index) {
        if (values instanceof List<?>) {
            return ((List<?>) values).get(index);
        }
        return Array.get(values, index);
    }

    private static int number(Object value) {
        return ((Number) value).intValue();
    }

    private static void run(String name, Runnable test) {
        long start = System.nanoTime();
        test.run();
        passed++;
        double ms = (System.nanoTime() - start) / 1_000_000.0;
        System.out.printf(Locale.US,
                "PASS %s (%.2f ms)%n", name, ms);
    }

    private static void testRoutes() {
        int[][] edges = {
                {0, 1, 3},
                {0, 2, 8},
                {1, 2, -7},
                {2, 3, 4}
        };

        int[] parents = {-1, 0, 2, 3, -1};

        check(Algorithms.reconstructRoute(
                        0, 3, parents, edges
                ).equals(List.of(0, 1, 2, 3)),
                "Reachable route incorrect");

        check(Algorithms.reconstructRoute(
                        0, 0, parents, edges
                ).equals(List.of(0)),
                "Source route incorrect");

        check(Algorithms.reconstructRoute(
                        0, 4, parents, edges
                ).isEmpty(),
                "Unreachable route incorrect");
    }

    private static void testTiedRoutes() {
        int[][] edges = {
                {0, 1, 2},
                {0, 1, 2},
                {1, 2, 3},
                {0, 2, 5}
        };

        check(Algorithms.reconstructRoute(
                        0, 2, new int[]{-1, 0, 3}, edges
                ).equals(List.of(0, 2)),
                "Direct tied route incorrect");

        check(Algorithms.reconstructRoute(
                        0, 2, new int[]{-1, 1, 2}, edges
                ).equals(List.of(0, 1, 2)),
                "Alternative tied route incorrect");
    }

    private static void testDsuLimit() {
        int n = 100000;
        int q = 250000;

        List<List<Integer>> operations = new ArrayList<>(q);

        for (int i = 0; i < n - 1; i++) {
            operations.add(List.of(i, i + 1));
        }

        while (operations.size() < q) {
            operations.add(List.of(0, n - 1));
        }

        Map<String, Object> input = new HashMap<>();
        input.put("n", n);
        input.put("operations", operations);

        Map<String, Object> result = Algorithms.dsu(input);
        List<?> states = (List<?>) result.get("states");

        check(states.size() == q, "Wrong DSU state count");

        Object last = states.get(states.size() - 1);

        check(number(item(last, 0)) == 1,
                "DSU should have one component");

        check(number(item(last, 1)) == n,
                "Largest DSU component incorrect");
    }

    private static List<List<Integer>> chain(int n) {
        List<List<Integer>> edges = new ArrayList<>(n - 1);

        for (int i = 0; i < n - 1; i++) {
            edges.add(List.of(i, i + 1));
        }

        return edges;
    }

    private static void testTopoDeep() {
        int n = 100000;

        Map<String, Object> input = new HashMap<>();
        input.put("n", n);
        input.put("edges", chain(n));

        Map<String, Object> result = Algorithms.topo(input);

        check(Boolean.TRUE.equals(result.get("acyclic")),
                "Deep chain incorrectly detected as cyclic");

        Object order = result.get("order");

        check(ArrayOrListSize(order) == n,
                "Incorrect topological order size");

        for (int i = 0; i < n; i++) {
            check(number(item(order, i)) == i,
                    "Wrong order at vertex " + i);
        }
    }

    private static int ArrayOrListSize(Object values) {
        if (values instanceof List<?>) {
            return ((List<?>) values).size();
        }
        return Array.getLength(values);
    }

    private static void testSccDeep() {
        int n = 100000;

        Map<String, Object> input = new HashMap<>();
        input.put("n", n);
        input.put("edges", chain(n));

        Map<String, Object> result = Algorithms.scc(input);
        Object labels = result.get("component");

        Set<Integer> unique = new HashSet<>();

        for (int i = 0; i < n; i++) {
            unique.add(number(item(labels, i)));
        }

        check(unique.size() == n,
                "Every chain vertex should be a separate SCC");

        Object condensation = result.get("condensation");

        check(ArrayOrListSize(condensation) == n - 1,
                "Incorrect condensation edge count");
    }

    private static void testDagLimit() {
        int n = 100000;
        int m = 250000;

        List<List<Integer>> edges = new ArrayList<>(m);

        for (int i = 0; i < n - 1; i++) {
            edges.add(List.of(i, i + 1, 1));
        }

        for (int i = n - 1; i < m; i++) {
            int target = 1 + (i % (n - 1));
            edges.add(List.of(0, target, 200000));
        }

        Map<String, Object> input = new HashMap<>();
        input.put("n", n);
        input.put("source", 0);
        input.put("edges", edges);

        Map<String, Object> result = Algorithms.dag(input);

        check(Boolean.TRUE.equals(result.get("acyclic")),
                "DAG incorrectly detected as cyclic");

        Object distances = result.get("distance");

        check(number(item(distances, n - 1)) == n - 1,
                "Incorrect DAG shortest distance");
    }

    private static void testDisconnectedCycle() {
        Map<String, Object> input = new HashMap<>();
        input.put("n", 4);
        input.put("source", 0);
        input.put("edges", List.of(
                List.of(0, 1, 5),
                List.of(2, 3, 1),
                List.of(3, 2, 2)
        ));

        Map<String, Object> result = Algorithms.dag(input);

        check(Boolean.FALSE.equals(result.get("acyclic")),
                "Disconnected cycle was not detected");
    }

    private static void testStateReset() {
        Map<String, Object> first = new HashMap<>();
        first.put("n", 3);
        first.put("operations", List.of(List.of(0, 1)));

        Map<String, Object> second = new HashMap<>();
        second.put("n", 3);
        second.put("operations", List.of());

        Algorithms.dsu(first);
        Map<String, Object> result = Algorithms.dsu(second);

        check(((List<?>) result.get("states")).isEmpty(),
                "State leaked between requests");
    }

    private static boolean validTopo(
            int[] order, int n, int[][] edges
    ) {
        if (order.length != n) {
            return false;
        }

        int[] position = new int[n];
        boolean[] seen = new boolean[n];

        for (int i = 0; i < n; i++) {
            int vertex = order[i];

            if (vertex < 0 || vertex >= n || seen[vertex]) {
                return false;
            }

            seen[vertex] = true;
            position[vertex] = i;
        }

        for (int[] edge : edges) {
            if (position[edge[0]] >= position[edge[1]]) {
                return false;
            }
        }

        return true;
    }

    private static void testAlternativeWitnesses() {
        int[][] edges = {{0, 2}, {1, 2}};

        check(validTopo(new int[]{0, 1, 2}, 3, edges),
                "First valid order rejected");

        check(validTopo(new int[]{1, 0, 2}, 3, edges),
                "Alternative valid order rejected");

        check(!validTopo(new int[]{2, 0, 1}, 3, edges),
                "Invalid order accepted");
    }

    public static void main(String[] args) {
        run("Route reconstruction", AdditionalGraphTests::testRoutes);
        run("Tied and parallel routes", AdditionalGraphTests::testTiedRoutes);
        run("DSU 250000 operations", AdditionalGraphTests::testDsuLimit);
        run("Topological deep chain", AdditionalGraphTests::testTopoDeep);
        run("Kosaraju deep chain", AdditionalGraphTests::testSccDeep);
        run("DAG 250000 edges", AdditionalGraphTests::testDagLimit);
        run("Disconnected DAG cycle", AdditionalGraphTests::testDisconnectedCycle);
        run("State reset", AdditionalGraphTests::testStateReset);
        run("Alternative and invalid witnesses",
                AdditionalGraphTests::testAlternativeWitnesses);

        System.out.println("ALL " + passed + " ADDITIONAL TESTS PASSED");
    }
}
