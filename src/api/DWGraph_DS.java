package api;

import java.util.*;

public class DWGraph_DS implements directed_weighted_graph {
    private int edgeSize;
    private int modeCount;
    private HashMap<Integer, node_data> graphV;
    private HashMap<Integer, HashMap<Integer, edge_data>> graphEdges;
    private HashMap<Integer, HashSet<Integer>> destEdges;

    /**
     * Empty Constructor
     * To begin working with a new Graph
     */
    public DWGraph_DS() {
        this.edgeSize = 0;
        this.modeCount = 0;
        this.graphV = new HashMap<Integer, node_data>();
        this.graphEdges = new HashMap<Integer, HashMap<Integer, edge_data>>();
        this.destEdges = new HashMap<Integer, HashSet<Integer>>();
    }

    /**
     * Constructor from objects
     * Needed in order to implement Deserializer for directed_weighted_graph
     */
    public DWGraph_DS(int edgeSize, HashMap<Integer, node_data> graphV, HashMap<Integer, HashMap<Integer, edge_data>> graphEdges, HashMap<Integer, HashSet<Integer>> destEdges) {
        this.edgeSize = edgeSize;
        this.graphV = new HashMap<Integer, node_data>();
        this.graphEdges = new HashMap<Integer, HashMap<Integer, edge_data>>();
        this.destEdges = new HashMap<Integer, HashSet<Integer>>();
        for(node_data vertex : graphV.values()){
            HashMap<Integer, edge_data> temp1 = new HashMap<Integer, edge_data>();
            HashSet<Integer> temp2 = new HashSet<Integer>();
            this.graphV.put(vertex.getKey(), vertex);
            this.graphEdges.put(vertex.getKey(), temp1);
            this.destEdges.put(vertex.getKey(), temp2);
        }
        for (int src: graphEdges.keySet()){
            for(int dest:graphEdges.get(src).keySet()){
                edge_data e = graphEdges.get(src).get(dest);
                HashMap<Integer,edge_data> temp = new HashMap<>();
                this.graphEdges.get(src).put(dest,e);
                this.destEdges.get(dest).add(src);
            }
        }
    }


    /**
     * Copy Constructor
     * A deep copy for a graph, will take a while on a very large Graph
     *
     * @param graph
     */
    public DWGraph_DS(api.directed_weighted_graph graph) {
        this.graphV = new HashMap<Integer, node_data>();
        this.graphEdges = new HashMap<Integer, HashMap<Integer, edge_data>>();
        this.destEdges = new HashMap<Integer, HashSet<Integer>>();
        Iterator<node_data> node_dataIterator = graph.getV().iterator();
        while (node_dataIterator.hasNext()) {
            HashMap<Integer, edge_data> temp1 = new HashMap<Integer, edge_data>();
            HashSet<Integer> temp2 = new HashSet<Integer>();
            node_data vertex = new NodeData(node_dataIterator.next());
            graphV.put(vertex.getKey(), vertex);
            graphEdges.put(vertex.getKey(), temp1);
            destEdges.put(vertex.getKey(), temp2);
        }
        for (node_data vertex : graph.getV()) {
            Iterator<edge_data> edge_dataIterator = graph.getE(vertex.getKey()).iterator();
            while (edge_dataIterator.hasNext()) {
                edge_data edge = new EdgeData(edge_dataIterator.next());
                this.connect(edge.getSrc(), edge.getDest(), edge.getWeight());
            }
        }
        this.edgeSize = graph.edgeSize();
        this.modeCount = graph.getMC();

    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DWGraph_DS that = (DWGraph_DS) o;
        return edgeSize == that.edgeSize &&
                Objects.equals(graphV, that.graphV) &&
                Objects.equals(graphEdges, that.graphEdges) &&
                Objects.equals(destEdges, that.destEdges);
    }

    /**
     * Return A Vertex by a given Key
     *
     * @param key - the node_id
     * @return
     */
    @Override
    public api.node_data getNode(int key) {
        if (!graphV.containsKey(key)) return null;
        return graphV.get(key);
    }

    /**
     * Return an Edge by V(source).key --> V(destination).key
     *
     * @param src  - Source Vertex Key
     * @param dest - Destination Vertex Key
     * @return
     */
    @Override
    public api.edge_data getEdge(int src, int dest) {
        /*
        3 Extreme Cases, for no bugs.
        if none of the condition True it will return the Edge
        */
        if (src == dest) return null;
        if (!graphV.containsKey(src) || !graphV.containsKey(dest)) return null;
        if (!graphEdges.get(src).containsKey(dest)) return null;

        return graphEdges.get(src).get(dest);
    }

    /**
     * Connect 2 Vertices by V(source).key --> V(destination).key
     * require weight.
     *
     * @param src  - the source of the edge.
     * @param dest - the destination of the edge.
     * @param w    - positive weight representing the cost (aka time, price, etc) between src --> dest.
     */
    @Override
    public void connect(int src, int dest, double w) {
        // 3 Extreme Cases, for no bugs.
        if (src == dest) return;
        else if (w < 0) return;
        else if (!graphV.containsKey(src) || !graphV.containsKey(dest)) return;

        if (!graphEdges.get(src).containsKey(dest)) {
            edgeSize++;
            modeCount++;
        }
        edge_data edge = new EdgeData(src, dest, w);
        graphEdges.get(src).put(dest, edge);
        destEdges.get(dest).add(src);
    }

    /**
     * Collection representing all the Vertices in the Graph.
     *
     * @return
     */
    @Override
    public Collection<api.node_data> getV() {
        return graphV.values();
    }

    /**
     * Collection representing all the Edges getting out of
     * the given Vertex (all the Edges starting (source) at the given Vertex).
     *
     * @param node_id
     * @return
     */
    @Override
    public Collection<api.edge_data> getE(int node_id) {
        if(!graphV.containsKey(node_id)) return null;
        return graphEdges.get(node_id).values();
    }

    /**
     * Remove a Vertex from Graph, by given Key
     * By going over the Vertices that The requested Key is there Destination.
     * This method run in O(k). k == number of edges [Source -> Key]
     *
     * @param key
     * @return
     */
    @Override
    public api.node_data removeNode(int key) {
        //if not in Graph -> return null.
        if (!graphV.containsKey(key)) return null;
        Iterator<Integer> integerIterator = destEdges.get(key).iterator();
        while (integerIterator.hasNext()) {
            int k = integerIterator.next();
            graphEdges.get(k).remove(key);
            destEdges.get(k).remove(key);
            edgeSize--;
        }
        modeCount++;
        destEdges.remove(key);
        graphEdges.remove(key);
        return graphV.remove(key);
    }

    /**
     * Remove an Edge from the Graph
     *
     * @param src
     * @param dest
     * @return
     */
    @Override
    public api.edge_data removeEdge(int src, int dest) {
        if (!graphV.containsKey(src) || !graphV.containsKey(dest)) return null;
        if (!graphEdges.get(src).containsKey(dest)) return null;
        edgeSize--;
        modeCount++;
        return graphEdges.get(src).remove(dest);
    }

    @Override
    public int nodeSize() {
        return graphV.size();
    }

    @Override
    public int edgeSize() {
        return edgeSize;
    }

    @Override
    public int getMC() {
        return modeCount;
    }

    /**
     * Adding a Vertex to the Graph
     *
     * @param n - node_data
     */
    @Override
    public void addNode(api.node_data n) {
        if (n == null) return;
        if (graphV.containsKey(n.getKey())) return;
        graphV.put(n.getKey(), n);
        HashMap<Integer, edge_data> forNow = new HashMap<Integer, edge_data>();
        graphEdges.put(n.getKey(), forNow);
        HashSet<Integer> forDest = new HashSet<Integer>();
        destEdges.put(n.getKey(), forDest);
        modeCount++;
    }

    /**
     * toString Method
     * <p>
     * Example:
     * <p>
     * Graph:
     * Vertex: [K:(0) T:[-1] I:{null}] <~> [<0,1> Weight:[8.33], <0,2> Weight:[4.2], ]
     * Vertex: [K:(1) T:[-1] I:{null}] <~> [<1,2> Weight:[2.1], <1,0> Weight:[17.4], ]
     * Vertex: [K:(2) T:[-1] I:{null}] <~> [<2,0> Weight:[13.7], <2,1> Weight:[11.5], ]
     *
     * @return
     */
    @Override
    public String toString() {
        String p = "Graph: \n";
        for (node_data vertex : getV()) {
            p = p + "Vertex: [" + vertex.toString() + "] <~> [";
            for (edge_data edge : getE(vertex.getKey())) {
                p = p + "<" + edge.getSrc() + "," + edge.getDest() + "> " + "Weight:[" + edge.getWeight() + "], ";
            }
            p = p + "]\n";
        }
        return p;
    }

}
