package api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gameClient.Arena;

import java.util.Iterator;

public class GameServices implements game_service {
    private Arena A;


    @Override
    public String getGraph() {
        //graph for server api in Ariel_OOP_2020/Assignments/Ex2/data/ graphs.
        //gson
        Gson gson = new Gson();
        JsonObject obj = new JsonObject();
        JsonArray edgeJson = new JsonArray();
        JsonArray nodesJson = new JsonArray();
        Iterator<node_data> node_dataIterator = A.getGraph().getV().iterator();
        while (node_dataIterator.hasNext()){
//        for(node_data node : graph.getV()){
            node_data node = node_dataIterator.next();
            JsonObject edgeOjson = new JsonObject();
            Iterator<edge_data> edge_dataIterator = A.getGraph().getE(node.getKey()).iterator();
            while (edge_dataIterator.hasNext()){
                edge_data edge = edge_dataIterator.next();
//            for(edge_data edge : graph.getE(node.getKey())){
                edgeOjson.add("src", gson.toJsonTree(edge.getSrc()));
                edgeOjson.add("weight", gson.toJsonTree(edge.getWeight()));
                edgeOjson.add("dest", gson.toJsonTree(edge.getDest()));
            }
            edgeJson.add(edgeOjson);
            String loc = node.getLocation().x()+","+node.getLocation().y()+","+node.getLocation().z();
            JsonObject nodes1 = new JsonObject();
            nodes1.add("id", gson.toJsonTree(node.getKey()));
            nodes1.add("pos",gson.toJsonTree(loc));
            nodesJson.add(nodes1);
        }
        obj.add("Edges",edgeJson);
        obj.add("Nodes", nodesJson);
        return obj.toString();
    }

    /**
     * Returns a JSON string, representing all Pokemons (fixed bonus coin).
     * @return
     */
    @Override
    public String getPokemons() {
        String json = new Gson().toJson(A.getPokemons());
        return json;
    }
    /**
     * Returns a JSON string, representing all the Agents.
     * @return
     */
    @Override
    public String getAgents() {
        String json = new Gson().toJson(A.getAgents());
        return json;
    }

    @Override
    public boolean addAgent(int start_node) {
        A.getAgents().get(0).setCurrNode(start_node);
        return false;
    }

    @Override
    public long startGame() {

        return 0;
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public long stopGame() {
        return 0;
    }

    @Override
    public long chooseNextEdge(int id, int next_node) {
        return 0;
    }

    @Override
    public long timeToEnd() {
        return 0;
    }

    @Override
    public String move() {
        return null;
    }

    @Override
    public boolean login(long id) {
        return false;
    }
}
