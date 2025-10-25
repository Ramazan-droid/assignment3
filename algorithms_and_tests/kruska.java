import java.io.File;
import java.net.URL;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Renamed Edge class for Kruskal
class KEdge {
    String from;
    String to;
    int weight;

    KEdge(String from, String to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "(" + from + " - " + to + " : " + weight + ")";
    }
}

// Disjoint Set for union-find
class DisjointSet {
    private Map<String, String> parent = new HashMap<>();

    public void makeSet(List<String> vertices) {
        for (String v : vertices) parent.put(v, v);
    }

    public String find(String v) {
        if (!parent.get(v).equals(v)) {
            parent.put(v, find(parent.get(v))); // path compression
        }
        return parent.get(v);
    }

    public boolean union(String a, String b) {
        String rootA = find(a);
        String rootB = find(b);
        if (rootA.equals(rootB)) return false; // already connected
        parent.put(rootA, rootB);
        return true;
    }
}

// MST result

public class kruska {
    public static class MSTResult {
        public List<KEdge> mstEdges = new ArrayList<>();
        public int totalCost = 0;
        public int operations = 0; // count comparisons/unions
    }

    // Kruskal algorithm
    public static MSTResult kruskalMST(List<String> vertices, List<KEdge> edges) {
        MSTResult result = new MSTResult();

        // Sort edges by weight
        edges.sort(Comparator.comparingInt(e -> e.weight));

        DisjointSet ds = new DisjointSet();
        ds.makeSet(vertices);

        for (KEdge e : edges) {
            result.operations++; // count union check
            if (ds.union(e.from, e.to)) {
                result.mstEdges.add(e);
                result.totalCost += e.weight;
            }
        }

        return result;
    }

    public static void main(String[] args) {
        String[] jsonFiles = {
                "ass_3_25graphs_fixed.json",
                "ass_3_extra_large.json"
        };
        ObjectMapper mapper = new ObjectMapper();

        for (String jsonFile : jsonFiles) {
            try {
                URL resource = kruska.class.getClassLoader().getResource(jsonFile);
                if (resource == null) {
                    System.out.println("File not found: " + jsonFile);
                    continue;
                }

                File file = new File(resource.toURI());
                JsonNode root = mapper.readTree(file);
                JsonNode graphs = root.get("graphs");

                for (JsonNode graph : graphs) {
                    int graphId = graph.get("id").asInt();
                    System.out.println("\n=== Graph ID: " + graphId + " (from " + jsonFile + ") ===");

                    // Read vertices
                    List<String> vertices = new ArrayList<>();
                    for (JsonNode node : graph.get("nodes")) vertices.add(node.asText());

                    // Read edges
                    List<KEdge> edges = new ArrayList<>();
                    for (JsonNode edgeNode : graph.get("edges")) {
                        String from = edgeNode.get("from").asText();
                        String to = edgeNode.get("to").asText();
                        int weight = edgeNode.get("weight").asInt();
                        edges.add(new KEdge(from, to, weight));
                    }

                    System.out.println("Original graph: " + vertices.size() + " vertices, " + edges.size() + " edges");

                    // Measure execution time
                    long startNano = System.nanoTime();
                    MSTResult result = kruskalMST(vertices, edges);
                    long endNano = System.nanoTime();
                    double elapsedMillis = (endNano - startNano) / 1_000_000.0;

                    // Print results
                    System.out.println("MST edges: " + result.mstEdges);
                    System.out.println("Total MST cost: " + result.totalCost);
                    System.out.println("Operations performed: " + result.operations);
                    System.out.println("Execution time: " + String.format("%.2f", elapsedMillis) + " ms");
                }

            } catch (Exception e) {
                System.out.println("Error processing file: " + jsonFile);
                e.printStackTrace();
            }
        }
    }
}
