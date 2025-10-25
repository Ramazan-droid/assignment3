import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.net.URL;
import java.util.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// JUnit tests for both Prim's and Kruskal's algorithms
public class MSTAlgorithmsTest {

    ObjectMapper mapper = new ObjectMapper();

    // Helper to load graph from JSON
    private Graph loadGraph(String jsonFile, int graphId) throws Exception {
        URL resource = getClass().getClassLoader().getResource(jsonFile);
        assertNotNull(resource, "File not found: " + jsonFile);
        File file = new File(resource.toURI());
        JsonNode root = mapper.readTree(file);
        for (JsonNode graph : root.get("graphs")) {
            if (graph.get("id").asInt() == graphId) {
                List<String> vertices = new ArrayList<>();
                for (JsonNode node : graph.get("nodes")) vertices.add(node.asText());
                List<Edge> primEdges = new ArrayList<>();
                List<KEdge> kruskaEdges = new ArrayList<>();
                for (JsonNode e : graph.get("edges")) {
                    String from = e.get("from").asText();
                    String to = e.get("to").asText();
                    int weight = e.get("weight").asInt();
                    primEdges.add(new Edge(from, to, weight));
                    kruskaEdges.add(new KEdge(from, to, weight));
                }
                return new Graph(vertices, primEdges, kruskaEdges);
            }
        }
        throw new RuntimeException("Graph ID not found: " + graphId);
    }

    // Container for graph data
    class Graph {
        List<String> vertices;
        List<Edge> primEdges;
        List<KEdge> kruskaEdges;
        Graph(List<String> v, List<Edge> pe, List<KEdge> ke) {
            vertices = v;
            primEdges = pe;
            kruskaEdges = ke;
        }
    }

    // Validate MST properties
    private void validateMST(int V, List<Edge> mstEdges, int totalCost) {
        // Number of edges == V - 1
        assertEquals(V - 1, mstEdges.size(), "MST must have V-1 edges");

        // Acyclic check using Union-Find
        Map<String, String> parent = new HashMap<>();
        for (Edge e : mstEdges) {
            parent.put(e.from, e.from);
            parent.put(e.to, e.to);
        }
        for (Edge e : mstEdges) {
            String rootA = findParent(e.from, parent);
            String rootB = findParent(e.to, parent);
            assertNotEquals(rootA, rootB, "MST contains a cycle");
            parent.put(rootA, rootB);
        }

        // All vertices connected
        Set<String> connected = new HashSet<>();
        for (Edge e : mstEdges) {
            connected.add(e.from);
            connected.add(e.to);
        }
        assertEquals(V, connected.size(), "MST must connect all vertices");

        // Total cost non-negative
        assertTrue(totalCost >= 0, "Total cost must be non-negative");
    }

    private String findParent(String v, Map<String, String> parent) {
        if (!parent.get(v).equals(v)) {
            parent.put(v, findParent(parent.get(v), parent));
        }
        return parent.get(v);
    }

    // Test Prim and Kruskal for all graphs in both JSON files
    @Test
    void testPrimKruskalConsistency() throws Exception {
        String[] jsonFiles = {
                "ass_3_25graphs_fixed.json",
                "ass_3_extra_large.json"
        };

        for (String f : jsonFiles) {
            URL resource = getClass().getClassLoader().getResource(f);
            assertNotNull(resource, "File not found: " + f);
            File file = new File(resource.toURI());
            JsonNode root = mapper.readTree(file);
            for (JsonNode graphNode : root.get("graphs")) {
                int V = graphNode.get("nodes").size();
                List<String> vertices = new ArrayList<>();
                for (JsonNode node : graphNode.get("nodes")) vertices.add(node.asText());
                List<Edge> primEdges = new ArrayList<>();
                List<KEdge> kruskaEdges = new ArrayList<>();
                for (JsonNode e : graphNode.get("edges")) {
                    String from = e.get("from").asText();
                    String to = e.get("to").asText();
                    int weight = e.get("weight").asInt();
                    primEdges.add(new Edge(from, to, weight));
                    kruskaEdges.add(new KEdge(from, to, weight));
                }

                // Run Prim
                algPrim.MSTResult primResult = algPrim.primMST(vertices, primEdges);

                // Run Kruskal
                kruska.MSTResult kruskaResult = kruska.kruskalMST(vertices, kruskaEdges);

                // Correctness checks
                assertEquals(primResult.totalCost, kruskaResult.totalCost, "Total cost must match");
                validateMST(V, primResult.mstEdges, primResult.totalCost);
                validateMST(V, convertKEdgeToEdge(kruskaResult.mstEdges), kruskaResult.totalCost);

                // Performance & consistency checks
                assertTrue(primResult.operations >= 0, "Prim operations non-negative");
                assertTrue(kruskaResult.operations >= 0, "Kruskal operations non-negative");
            }
        }
    }

    // Helper to convert KEdge to Edge for validation
    private List<Edge> convertKEdgeToEdge(List<KEdge> kEdges) {
        List<Edge> edges = new ArrayList<>();
        for (KEdge ke : kEdges) edges.add(new Edge(ke.from, ke.to, ke.weight));
        return edges;
    }
}
