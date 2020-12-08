package api;

import com.google.gson.*;
import gameClient.util.Point3D;

import java.io.*;
import java.util.*;

public class DWGraph_Algo implements dw_graph_algorithms {
    private directed_weighted_graph graph;
    private HashMap<Integer, Double> Tags;
    private HashMap<Integer, Integer> pointer;

    public DWGraph_Algo() {
        this.graph = new DWGraph_DS();
    }

    @Override
    public void init(directed_weighted_graph g) {
        this.graph = g;
        this.Tags = new HashMap<Integer, Double>();
        this.pointer = new HashMap<Integer, Integer>();
    }

    @Override
    public directed_weighted_graph getGraph() {
        return graph;
    }

    @Override
    public directed_weighted_graph copy() {
        return new DWGraph_DS(graph);
    }

    /**
     * Using BFS Algorithm
     * Step 1: Check if every Vertex is reachable from a random Vertex
     * Step 2: Transpose the current Graph
     * Step 3: Check if the TransposeGraph is Connected - Same as Step 1.
     * <p>
     * Complexity = 2(O(V+E)) + 2V
     *
     * @return - TRUE - if and only if in Both Graphs every Vertex is reachable from every Vertex.
     */
    @Override
    public boolean isConnected() {
        Iterator<node_data> nodeDataIterator = graph.getV().iterator();
        node_data vertex = nodeDataIterator.next();
        BFS(vertex.getKey(), graph);
        for (node_data v : graph.getV()) {
            if (v.getTag() == -1) return false;
        }
        directed_weighted_graph graphTranspose = this.Transpose();
        Iterator<node_data> dataIterator = graphTranspose.getV().iterator();
        node_data transpose = dataIterator.next();
        BFS(transpose.getKey(), graphTranspose);
        for (node_data v : graphTranspose.getV()) {
            if (v.getTag() == -1) return false;
        }
        return true;
    }

    /**
     * Using Dijkstra Algorithm
     * if a path exist -> return the Weight of the Destination vertex (Represent the Min Weight from Source Vertex)
     *
     * @param src  - start node
     * @param dest - end (target) node
     * @return
     */
    @Override
    public double shortestPathDist(int src, int dest) {
        if (graph.getNode(src) == null | graph.getNode(dest) == null) return -1.0;
        node_data vertex = graph.getNode(src);
        Dijkstra(vertex);
        if (Tags.get(dest) == Double.MAX_VALUE) return -1;
        double shorty = Tags.get(dest);
        return shorty;

    }

    /**
     * Using Dijkstra Algorithm
     * if a path exist -> make a List that contain node_data objects that shows the shortest path
     * from Source to Destination
     * Example: v(src) -> v(1) -> v(2) -> ... -> v(dest)
     *
     * @param src  - start node
     * @param dest - end (target) node
     * @return
     */
    @Override
    public List<node_data> shortestPath(int src, int dest) {
        if (graph.getNode(src) == null | graph.getNode(dest) == null) return null;
        node_data ver = graph.getNode(src);
        Dijkstra(ver);
        if (Tags.get(dest) == Double.MAX_VALUE) return null;

        List<node_data> pathList = new LinkedList<node_data>();
        pathList.add(graph.getNode(dest));

        if (src == dest) return pathList;

        int vertex = pointer.get(dest).intValue();
        while (vertex != src) {
            pathList.add(graph.getNode(vertex));
            int temp = vertex;
            vertex = pointer.get(temp).intValue();
        }
        pathList.add(graph.getNode(src));
        Collections.reverse(pathList);

        return pathList;
    }


    @Override
    public boolean save(String file) {
        try {
            Writer writer = new FileWriter(file);
            Gson gson = new Gson();

            JsonObject obj = new JsonObject();
            JsonArray edgesArrayJson = new JsonArray();
            JsonArray graphArrayJson = new JsonArray();

            for (node_data nodeData : graph.getV()) {
                JsonObject JsonNodeData = new JsonObject();
                String loc = nodeData.getLocation().x() + "," + nodeData.getLocation().y() + "," + nodeData.getLocation().z();
                JsonNodeData.addProperty("pos", loc);
                JsonNodeData.addProperty("id", nodeData.getKey());
                JsonElement jsonElementNode = JsonNodeData;
                graphArrayJson.add(JsonNodeData);
                for (edge_data edgeData : graph.getE(nodeData.getKey())) {
                    JsonObject jsonEdgeData = new JsonObject();
                    jsonEdgeData.addProperty("src", edgeData.getSrc());
                    jsonEdgeData.addProperty("dest", edgeData.getDest());
                    jsonEdgeData.addProperty("w", edgeData.getWeight());
                    JsonElement jsonElement = jsonEdgeData;
                    edgesArrayJson.add(jsonElement);
                }
            }
            obj.add("Edges", edgesArrayJson);
            obj.add("Nodes", graphArrayJson);
            String JsonGraph = obj.toString();
            writer.write(JsonGraph);
            writer.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean load(String file) {//Boaz says we Should be use this
        try (Reader reader = new FileReader(file)) {
            JsonElement graphElement = JsonParser.parseReader(reader);
            HashMap<Integer, node_data> graphV = new HashMap<>();
            JsonObject graphVJson = graphElement.getAsJsonObject();
            JsonArray inputMap = graphVJson.getAsJsonArray("Nodes");
            JsonObject objectGraphV;
            for (int i = 0; i < inputMap.size(); i++) {
                objectGraphV = inputMap.get(i).getAsJsonObject();
                int id = objectGraphV.get("id").getAsInt();
                String s = objectGraphV.get("pos").getAsString();
                String arrStr[] = s.split(",");
                String x = "", y = "", z = "";
                x += arrStr[0];
                y += arrStr[1];
                z += arrStr[2];
                Point3D p = new Point3D(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
                node_data nodeData = new NodeData(id, p);
                graphV.put(id, nodeData);
            }
            HashMap<Integer, HashMap<Integer, edge_data>> graphEdges = new HashMap<Integer, HashMap<Integer, edge_data>>();
            HashMap<Integer, HashSet<Integer>> destEdges = new HashMap<>();
            for (node_data vertex : graphV.values()) {
                HashMap<Integer, edge_data> temp1 = new HashMap<Integer, edge_data>();
                HashSet<Integer> temp2 = new HashSet<Integer>();
                graphEdges.put(vertex.getKey(), temp1);
                destEdges.put(vertex.getKey(), temp2);
            }
            JsonObject graphEdgesJson = graphElement.getAsJsonObject();
            JsonArray edgesJsonElements = graphVJson.getAsJsonArray("Edges");
            JsonObject objectEdges;
            for (int i = 0; i < edgesJsonElements.size(); i++) {
                objectEdges = edgesJsonElements.get(i).getAsJsonObject();
                int src = objectEdges.get("src").getAsInt(), dest = objectEdges.get("dest").getAsInt();
                double w = objectEdges.get("w").getAsDouble();
                edge_data edge = new EdgeData(src, dest, w);
                graphEdges.get(src).put(dest, edge);
                destEdges.get(dest).add(src);
            }
            int edgeSize = edgesJsonElements.size();
            DWGraph_DS dwGraph_ds = new DWGraph_DS(edgeSize, graphV, graphEdges, destEdges);
            this.init(dwGraph_ds);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    //added this

    /**
     * [Dijkstra Algorithm]
     * Applying a Weight on every node that reachable from the source (node)
     * That represent the MinSum(Distance) of the Edges.
     * Using PriorityQueue for a better RunTime.
     *
     * @param node <=> (node_data).
     */
    private void Dijkstra(node_data node) {
        for (node_data vertex : graph.getV()) {
            Tags.put(vertex.getKey(), Double.MAX_VALUE);
        }
        Tags.put(node.getKey(), 0.0);
        PriorityQueue<node_data> pQueue = new PriorityQueue<node_data>();
        List<node_data> visited = new LinkedList<node_data>();
        visited.add(node);
        pQueue.add(node);
        while (!pQueue.isEmpty()) {
            node_data nodeStart = pQueue.poll();
            Iterator<edge_data> iterator = graph.getE(nodeStart.getKey()).iterator();
            while (iterator.hasNext()) {
                edge_data nodeNeighbor = iterator.next();
                double srcTag = Tags.get(nodeNeighbor.getSrc());
                double edgeWeight = graph.getEdge(nodeNeighbor.getSrc(), nodeNeighbor.getDest()).getWeight();
                double weight = srcTag + edgeWeight;
                if (weight < Tags.get(nodeNeighbor.getDest())) {
                    Tags.put(nodeNeighbor.getDest(), weight);
                    pointer.put(nodeNeighbor.getDest(), nodeNeighbor.getSrc());
                    if (!visited.contains(nodeNeighbor)) {
                        pQueue.add(graph.getNode(nodeNeighbor.getDest()));
                        visited.add(graph.getNode(nodeNeighbor.getDest()));
                    }
                }
            }
        }
    }

    /**
     * BFS Algorithm
     * Check if for every Vertex all the Vertices are reachable, marking the Tag value
     *
     * @param src   - Random Key from Graph
     * @param graph
     */
    private void BFS(int src, directed_weighted_graph graph) {
        Queue<node_data> queue = new LinkedList<node_data>();
        graph.getNode(src).setTag(0);
        queue.add(graph.getNode(src));
        while (!queue.isEmpty()) {
            node_data nodeStart = queue.peek();
            queue.poll();
            Iterator<edge_data> iterator = graph.getE(nodeStart.getKey()).iterator();
            while (iterator.hasNext()) {
                edge_data nodeNeighbor = iterator.next();
                node_data temp = graph.getNode(nodeNeighbor.getDest());
                if (temp.getTag() == -1) {
                    temp.setTag(nodeStart.getTag() + 1);
                    queue.add(temp);
                }
            }
        }
    }

    /**
     * Graph Transpose Method
     * reverse direction of every edge exist in graph
     *
     * @return -> TransposeGraph
     */
    private directed_weighted_graph Transpose() {
        directed_weighted_graph transposeGraph = new DWGraph_DS();
        for (node_data v : graph.getV()) {
            v.setTag(-1);
            transposeGraph.addNode(v);
        }
        for (node_data v : graph.getV()) {
            Iterator<edge_data> edgeDataIterator = graph.getE(v.getKey()).iterator();
            while (edgeDataIterator.hasNext()) {
                edge_data e = edgeDataIterator.next();
                transposeGraph.connect(e.getDest(), e.getSrc(), e.getWeight());
            }
        }
        return transposeGraph;
    }
}
