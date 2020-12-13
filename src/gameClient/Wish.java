package gameClient;
import api.*;
import Server.Game_Server_Ex2;
import com.google.gson.*;
import gameClient.util.Point3D;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Wish implements Runnable{
    private static MyFrame _win;
    private Arena arena;
    private HashMap<Integer, HashMap<Integer, List<node_data>>> dijkstraAllMap;
    private HashMap<Integer,Integer> nextNodes;

    public static void main(String[] args) {
        Thread client = new Thread(new Wish());
        client.start();
    }

    @Override
    public void run() {
        game_service game = Game_Server_Ex2.getServer(23);
        boolean isConnected = buildGraph(game.getGraph(), game).isConnected();
        if(Arena.json2Pokemons(game.getPokemons()).size()<=howMuchAgents(game.toString()))
            isConnected = false;
        init(game,isConnected);
        game.startGame();
        while (game.isRunning()){
            try {
                arena.setPokemons(Arena.json2Pokemons(game.getPokemons()));
                _win.update(arena);
                _win.paint(_win.getGraphics());
                _win.setTitle("Until now - " + game.toString());
                moveAgents(game, arena, nextNodes);
                Thread.sleep(100);
                setNextNodes(game);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String res = game.toString();
        System.out.println(res);
        Thread.interrupted();

    }

    //we'll put to sleep according to edge => fuck yes
    public static void moveAgents(game_service game, Arena arena, HashMap<Integer, Integer> nextNodes) throws InterruptedException {//arena have graph
        int i = 0;
        int dt = 50;
        List <CL_Pokemon> pokemons = Arena.json2Pokemons(game.getPokemons());
        HashMap<Integer, CL_Pokemon> pokeMap = new HashMap<>();
        for(CL_Pokemon poke: pokemons){
            Arena.updateEdge(poke, arena.getGraph());
            pokeMap.put(poke.get_edge().getDest(), poke);
            pokeMap.put(poke.get_edge().getSrc(),poke);
        }
        for(CL_Agent agent : Arena.getAgents(game.getAgents(), arena.getGraph())){//changed the nextNode
            int ToNode = nextNodes.get(agent.getID());
//            int dy = (int)(dt/(int)(Math.pow((agent.getSpeed()),2)));//we need to find formula for this
            if(agent.getSpeed() > 2)
                dt*=0.75;
            if(pokeMap.get(ToNode)!=null){
                if(agent.getSpeed() > 1){
                    dt = (int)(dt/(int)(Math.pow((agent.getSpeed()),2)+agent.getSpeed()));
                    if(agent.getSpeed()>2) {
                        dt = (int) (dt / (Math.pow(pokeMap.get(ToNode).get_edge().getWeight(), 0.5)));
                    }
                }
            }

            agent.setNextNode(ToNode);
            int id = Arena.getAgents(game.getAgents(), arena.getGraph()).get(i).getID();
            game.chooseNextEdge(agent.getID(), ToNode);
            Thread.sleep(dt);
            if(Arena.getAgents(game.getAgents(), arena.getGraph()).get(i).getSrcNode()!=agent.getSrcNode()){
                System.out.println("Agent: "+agent.getID()+", val: "+agent.getValue()+",  "+
                        agent.getSrcNode() +" , turned to node: "+ToNode +" Speed-  "+ agent.getSpeed());
                }
            i++;
        }
        game.move();
    }
    public void init(game_service game, boolean isConnected) {
        dw_graph_algorithms gg = buildGraph(game.getGraph(), game);
        //gg.init(g);
        arena = new Arena();
        arena.setGraph(gg.getGraph());

        String info = game.toString();
        JSONObject line;
        try {
            line = new JSONObject(info);
            JSONObject gameObject = line.getJSONObject("GameServer");
            int rs = gameObject.getInt("agents");
            System.out.println(info);
            System.out.println(game.getPokemons());
            ArrayList<CL_Pokemon> cl_fs = Arena.json2Pokemons(game.getPokemons());
            for(int a = 0;a<cl_fs.size();a++) {
                Arena.updateEdge(cl_fs.get(a),gg.getGraph());
            }
            dijkstraAllMap = new HashMap<>();
            dijkstraAll(gg.getGraph());
            arena.setPokemons(cl_fs);
            if(isConnected)
                SetGameAgents(game, gg.getGraph());
            else
                SetGameAgentsButFalse(game, gg.getGraph());
            List<CL_Agent> cl_agents = Arena.getAgents(game.getAgents(), gg.getGraph());
            arena.setAgents(cl_agents);
            _win = new MyFrame("test Ex2");
            _win.setSize(1000, 700);
            _win.update(arena);
            _win.show();
            nextNodes = new HashMap<>();
            setNextNodes(game);
        }
        catch (JSONException | InterruptedException e) {e.printStackTrace();}
    }


    /**
     * If the graph isn't fully connected, i'll prefer to set the agents in
     * the place with the most connectivity nodes
     * @param game
     * @param graph
     */
    public void SetGameAgentsButFalse(api.game_service game, directed_weighted_graph graph){
        ArrayList<CL_Pokemon> gotOne= Arena.json2Pokemons(game.getPokemons());
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(graph);
        int agentsSize = howMuchAgents(game.toString());

        for (CL_Pokemon poke : gotOne) {Arena.updateEdge(poke, graph);}

        HashMap<Point3D, Integer> maxUnConnectivityMap = new HashMap<>();
        for(CL_Pokemon poke : gotOne){
            int forNowUnConnectivity = 0;
            for(node_data node : graph.getV()){
                if(node.getKey() != poke.get_edge().getDest()){
                    double dist =graphAlgorithms.shortestPathDist(poke.get_edge().getDest(), node.getKey());
                    if(dist == Double.MAX_VALUE )
                        forNowUnConnectivity++;
                }
            }
            maxUnConnectivityMap.put(poke.getLocation(), forNowUnConnectivity);
        }
        ArrayList<CL_Pokemon> cl_pokemons = new ArrayList<>();
        for (int i = 0; i < agentsSize; i++) {
            int max= graph.getV().size();
            Point3D temp = new Point3D(0,0,0);
            for(Point3D p :maxUnConnectivityMap.keySet()){
                if(max > maxUnConnectivityMap.get(p))
                    temp = p;
            }
            for(CL_Pokemon poke : gotOne){
                if(poke.getLocation() == temp)
                    cl_pokemons.add(poke);
            }
        }
    }
    public void SetGameAgents(api.game_service game, directed_weighted_graph graph){
        ArrayList<CL_Pokemon> gotOne= Arena.json2Pokemons(game.getPokemons());
        for (int i = 0; i < howMuchAgents(game.toString()); i++){
            Arena.updateEdge(gotOne.get(i), graph);
            if(i+1 > gotOne.size()){
                i=howMuchAgents(game.toString());
            }else{
                game.addAgent(gotOne.get(i).get_edge().getSrc());
            }
        }
    }

    public int howMuchAgents(String game){
        JsonElement gameElement = JsonParser.parseString(game);
        JsonObject pokemonsObject = gameElement.getAsJsonObject();
        JsonElement gameServerElement = pokemonsObject.get("GameServer");
        JsonObject gameServerObject = gameServerElement.getAsJsonObject();
        return gameServerObject.get("agents").getAsInt();
    }

    public api.dw_graph_algorithms buildGraph(String JsonGraph, api.game_service game){
        api.DWGraph_DS_Deserializer deserializer = new api.DWGraph_DS_Deserializer();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(api.directed_weighted_graph.class, deserializer);
        Gson gson = gsonBuilder.create();
        api.directed_weighted_graph graph = gson.fromJson(game.getGraph(), api.directed_weighted_graph.class);
        api.dw_graph_algorithms graphAlgo = new DWGraph_Algo();
        graphAlgo.init(graph);
        return graphAlgo;
    }

    public void dijkstraAll(api.directed_weighted_graph graph){

//        api.directed_weighted_graph graph = graphAlgo.getGraph();
        //added empty map to add values after
        for (api.node_data nodeData : graph.getV()){
            HashMap<Integer, List<api.node_data>> innerMap = new HashMap<Integer, List<api.node_data>>();
            dijkstraAllMap.put(nodeData.getKey(), innerMap);
        }
        api.dw_graph_algorithms graphAlgo = new DWGraph_Algo();
        graphAlgo.init(graph);
        for (api.node_data srcData :graph.getV()){
            int src = srcData.getKey();
            for(api.node_data destData :graph.getV()){
                int dest = destData.getKey();
                List<api.node_data> list = graphAlgo.shortestPath(src, dest);
                dijkstraAllMap.get(src).put(dest, list);
            }
        }
    }

    public void setNextNodes(game_service game) throws InterruptedException {
//        Thread.sleep(100);
        String AgentsAfterMove = game.getAgents();

        String g2p = game.getPokemons();

        List<CL_Pokemon> Pokemons = Arena.json2Pokemons(g2p);

        List<CL_Agent> Agents = Arena.getAgents(AgentsAfterMove, arena.getGraph());
        arena.setAgents(Agents);
        arena.setPokemons(Pokemons);
        for(CL_Pokemon pok : arena.getPokemons()){Arena.updateEdge(pok, arena.getGraph());}
        int from = 0;
        int to = 0;
        dw_graph_algorithms ga = new DWGraph_Algo();
        ga.init(arena.getGraph());
        //agents loop
        for (int i = 0; i <  Agents.size(); i++) {
            int src = Agents.get(i).getSrcNode();
            double min = Double.MAX_VALUE;
            int deleteIndex = 0;
            //pokemons inner loop
            for (int j = 0; j < Pokemons.size(); j++) {
                Arena.updateEdge(Pokemons.get(j), arena.getGraph());
                int dest = Pokemons.get(j).get_edge().getDest();
                if (src == dest)
                    dest = Pokemons.get(j).get_edge().getSrc();
                ga.init(arena.getGraph());
                double pathWeight = ga.shortestPathDist(src, dest);
                if (pathWeight < min) {
                    from = src;
                    to = dest;
                    min = pathWeight;
                    deleteIndex = j;
                }
            }
            //if the agent don't have path to any pokemon
            if (min < Double.MAX_VALUE) {
                Pokemons.remove(deleteIndex);
                int dest = dijkstraAllMap.get(from).get(to).get(1).getKey();//next node after src in dijkstra path
                int id = Agents.get(i).getID();
                if (nextNodes.keySet().contains(id)) {//changed the nextNode
                    nextNodes.remove(id);
                }
                Arena.getAgents(game.getAgents(), arena.getGraph()).get(i).setNextNode(dest);
                nextNodes.put(id, dest);
            }
        }
    }

    public void setNextNodesd(game_service game) throws InterruptedException {
        String AgentsAfterMove;
        AgentsAfterMove = game.getAgents();
        String g2p = game.getPokemons();
        List<CL_Pokemon> Pokemons = Arena.json2Pokemons(g2p);
        List<CL_Agent> Agents = Arena.getAgents(AgentsAfterMove, arena.getGraph());
        arena.setAgents(Agents);
        arena.setPokemons(Pokemons);
        for(CL_Pokemon pok : arena.getPokemons()){Arena.updateEdge(pok, arena.getGraph());}
        int from = 0;
        int to = 0;
        dw_graph_algorithms ga = new DWGraph_Algo();
        ga.init(arena.getGraph());
        //agents loop
        for (int i = 0; i <  Agents.size(); i++) {
            int src = Agents.get(i).getSrcNode();
            double min = Double.MAX_VALUE;
            int deleteIndex = 0;
            //pokemons inner loop
            for (int j = 0; j < Pokemons.size(); j++) {
                Arena.updateEdge(Pokemons.get(j), arena.getGraph());
                int dest = Pokemons.get(j).get_edge().getDest();
                if (src == dest)
                    dest = Pokemons.get(j).get_edge().getSrc();
                ga.init(arena.getGraph());
                double pathWeight = ga.shortestPathDist(src, dest);
                if (pathWeight < min) {
                    from = src;
                    to = dest;
                    min = pathWeight;
                    deleteIndex = j;
                }
            }
            //if the agent don't have path to any pokemon
            if (min < Double.MAX_VALUE) {
                Pokemons.remove(deleteIndex);
                int dest = dijkstraAllMap.get(from).get(to).get(1).getKey();//next node after src in dijkstra path
                int id = Agents.get(i).getID();
                if (nextNodes.keySet().contains(id)) {//changed the nextNode
                    nextNodes.remove(id);
                }
                Arena.getAgents(game.getAgents(), arena.getGraph()).get(i).setNextNode(dest);
                nextNodes.put(id, dest);
            }
        }
    }

}