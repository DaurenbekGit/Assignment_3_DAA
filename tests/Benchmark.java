
import java.util.*;

public class Benchmark {

    private static final long SEED = 20261025L;

    private static Map<String, Object> createGraph(int n, int m) {
        Random random = new Random(SEED + n);
        List<List<Integer>> edges = new ArrayList<>(m);

        // Connect all vertices.
        for (int v = 1; v < n; v++) {
            int weight = random.nextInt(2001) - 1000;
            edges.add(List.of(v - 1, v, weight));
        }

        // Add more random edges.
        while (edges.size() < m) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);

            if (u == v) {
                continue;
            }

            int weight = random.nextInt(2001) - 1000;
            edges.add(List.of(u, v, weight));
        }

        Map<String, Object> input = new HashMap<>();
        input.put("n", n);
        input.put("edges", edges);
        return input;
    }

    private static double measure(
            Map<String, Object> input,
            boolean useKruskal
    ) {
        long start = System.nanoTime();

        if (useKruskal) {
            Algorithms.kruskal(input);
        } else {
            Algorithms.prim(input);
        }

        long end = System.nanoTime();
        return (end - start) / 1_000_000.0;
    }

    private static double median(double[] values) {
        double[] copy = values.clone();
        Arrays.sort(copy);
        return copy[1];
    }

    public static void main(String[] args) {

        int[][] sizes = {
                {1000, 3000},
                {10000, 30000},
                {100000, 250000}
        };

        System.out.println("Benchmark: Kruskal vs Prim");
        System.out.println("Seed: " + SEED);
        System.out.println("Timer: System.nanoTime()");
        System.out.println("Warm-up: 1 call per algorithm");

        for (int[] size : sizes) {
            int n = size[0];
            int m = size[1];

            Map<String, Object> input = createGraph(n, m);

            // Warm-up and verify equal optimal costs.
            Map<String, Object> k = Algorithms.kruskal(input);
            Map<String, Object> p = Algorithms.prim(input);

            if (!Objects.equals(k.get("cost"), p.get("cost"))
                    || !Objects.equals(
                    k.get("components"),
                    p.get("components"))) {
                throw new IllegalStateException(
                        "Kruskal and Prim results differ!");
            }

            double[] kruskalTimes = new double[3];
            double[] primTimes = new double[3];

            for (int i = 0; i < 3; i++) {
                kruskalTimes[i] = measure(input, true);
                primTimes[i] = measure(input, false);
            }

            System.out.println("\nVertices: " + n
                    + ", Edges: " + m);

            System.out.println("Kruskal runs: "
                    + Arrays.toString(kruskalTimes));

            System.out.println("Prim runs: "
                    + Arrays.toString(primTimes));

            System.out.printf(Locale.US,
                    "Kruskal median: %.3f ms%n",
                    median(kruskalTimes));

            System.out.printf(Locale.US,
                    "Prim median: %.3f ms%n",
                    median(primTimes));
        }
    }
}
