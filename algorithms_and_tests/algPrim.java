import java.io.File;
import java.net.URL;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Edge {
    String from;
    String to;
    int weight;

    Edge(String from, String to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "(" + from + " - " + to + " : " + weight + ")";
    }
}

public class algPrim {

    public static class MSTResult {
        List<Edge> mstEdges = new ArrayList<>();
        int totalCost = 0;
        int operations = 0; // number of comparisons / insertions
    }

    public static void main(String[] args) {
        // JSON files in src/main/resources
        String[] jsonFiles = {
                "ass_3_25graphs_fixed.json",
                "ass_3_extra_large.json"
        };

        ObjectMapper mapper = new ObjectMapper();

        for (String jsonFile : jsonFiles) {
            try {
                URL resource = algPrim.class.getClassLoader().getResource(jsonFile);
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
                    for (JsonNode node : graph.get("nodes")) {
                        vertices.add(node.asText());
                    }

                    // Read edges
                    List<Edge> edges = new ArrayList<>();
                    for (JsonNode edgeNode : graph.get("edges")) {
                        String from = edgeNode.get("from").asText();
                        String to = edgeNode.get("to").asText();
                        int weight = edgeNode.get("weight").asInt();
                        edges.add(new Edge(from, to, weight));
                    }

                    System.out.println("Original graph: " + vertices.size() + " vertices, " + edges.size() + " edges");

                    // Measure execution time
                    long startTime = System.nanoTime();
                    MSTResult result = primMST(vertices, edges);
                    long endTime = System.nanoTime();
                    double elapsedMillis = (endTime - startTime) / 1_000_000.0;

                    // Output MST info
                    System.out.println("MST edges: " + result.mstEdges);
                    System.out.println("Total cost: " + result.totalCost);
                    System.out.println("Operations performed: " + result.operations);
                    System.out.println("Execution time: " +  String.format("%.2f", elapsedMillis) + " ms");
                }

            } catch (Exception e) {
                System.out.println("Error processing file: " + jsonFile);
                e.printStackTrace();
            }
        }
    }

    public static MSTResult primMST(List<String> vertices, List<Edge> edges) {
        MSTResult result = new MSTResult();
        Set<String> visited = new HashSet<>();
        Map<String, List<Edge>> adj = new HashMap<>();

        // Build adjacency list
        for (String v : vertices) adj.put(v, new ArrayList<>());
        for (Edge e : edges) {
            adj.get(e.from).add(e);
            adj.get(e.to).add(new Edge(e.to, e.from, e.weight)); // undirected
        }

        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        String start = vertices.get(0);
        visited.add(start);
        pq.addAll(adj.get(start));

        while (!pq.isEmpty() && visited.size() < vertices.size()) {
            result.operations++;
            Edge e = pq.poll();
            if (!visited.contains(e.to)) {
                visited.add(e.to);
                result.mstEdges.add(e);
                result.totalCost += e.weight;

                for (Edge next : adj.get(e.to)) {
                    if (!visited.contains(next.to)) {
                        pq.add(next);
                        result.operations++;
                    }
                }
            }
        }

        return result;
    }
}
