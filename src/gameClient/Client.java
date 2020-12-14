package gameClient;
import Server.Game_Server_Ex2;
import api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gameClient.util.Point3D;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Client implements Runnable {
   // private game_service game;
    private int levelNumber;
    private static MyFrame _win;
    private Arena myArena;
    private HashMap<Integer, HashMap<Integer, List<node_data>>> allPaths;
    private HashMap<Integer, Integer> nextNodes;

    //formula => speed , weight, limit
    //double/ limitPartition=Limit / agents
    //SpeedWeightAll = for+=(speed/weight) | speed/weight is for higher partition for the faster agent
    //limit / SpeedWeightAll
    //limitPartitionAgent(i) = limitPartition *

    public static void main(String[] args) {
        Thread thread = new Thread(new Client());
        thread.start();
    }

    @Override
    public void run() {
//        Scanner sc = new Scanner(System.in);
//        System.out.println("Please Choose A level between [0-23]");
//        levelNumber = sc.nextInt();
//        game_service game = Game_Server_Ex2.getServer(levelNumber);
        for (int i = 0; i < 24; i++) {
            game_service game = Game_Server_Ex2.getServer(i);
            directed_weighted_graph graph = setGraph(game);
            dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
            graphAlgorithms.init(graph);

            initiate(game);
            game.startGame();
            while (game.isRunning()) {
                try {
                    myArena.setPokemons(Arena.json2Pokemons(game.getPokemons()));
                    _win.update(myArena);
                    _win.paint(_win.getGraphics());
                    _win.setTitle("As GameEx2");
                    moveAgents(game, nextNodes, Arena.json2Pokemons(game.getPokemons()));
//                _win.repaint();
                    Thread.sleep(100);
                    setNext(game, myArena.getPokemons());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//            System.out.println(game.toString());
//            System.out.println(Thread.interrupted());
//            System.exit(0);
            }
            String res = game.toString();
            System.out.println(res);
        }
    }

    public directed_weighted_graph setGraph(game_service game) {

        DWGraph_DS_Deserializer deserializer = new DWGraph_DS_Deserializer();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(directed_weighted_graph.class, deserializer);
        Gson gson = gsonBuilder.create();
        directed_weighted_graph graph = gson.fromJson(game.getGraph(), directed_weighted_graph.class);

        return graph;
    }

    public void initiate(game_service game) {

        directed_weighted_graph graph = setGraph(game);
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();

        graphAlgorithms.init(graph);
        boolean Connected = graphAlgorithms.isConnected();

        myArena = new Arena();
        myArena.setGraph(graph);

        List<CL_Pokemon> pokemonList = Arena.json2Pokemons(game.getPokemons());
        myArena.setPokemons(pokemonList);

        for (int i = 0; i < pokemonList.size(); i++) {
            Arena.updateEdge(pokemonList.get(i), graph);
        }

        findShortestPaths(graph);

        if (Connected) {
            setGameAgents(game, graph, pokemonList);
        } else {
            pokemonList = UNConnectedSetAgents(game, graph, pokemonList);
            int agents = anAgentInt(game);
            for (int i = 0; i < agents; i++) {
                game.addAgent(pokemonList.get(i).get_edge().getSrc());
            }
        }try{
            _win = new MyFrame("GameEx2");
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            int width = dimension.width;
            int height = dimension.height;
            _win.setSize(width-100,height-100);
            _win.update(myArena);
            _win.show();
//            _win.paint(_win.getGraphics());
            int b = anAgentInt(game);
            nextNodes = new HashMap<>();
            setNext(game,myArena.getPokemons());
            int a = 0;
        }catch (NullPointerException e){
            e.printStackTrace();
        }


    }

    public void findShortestPaths(directed_weighted_graph graph) {
        allPaths = new HashMap<>();
        for (node_data nodeData : graph.getV()) {
            HashMap<Integer, List<node_data>> innerMap = new HashMap<Integer, List<node_data>>();
            allPaths.put(nodeData.getKey(), innerMap);
        }
        dw_graph_algorithms graphAlgo = new DWGraph_Algo();
        graphAlgo.init(graph);
        for (node_data srcData : graph.getV()) {
            int src = srcData.getKey();
            for (node_data destData : graph.getV()) {
                int dest = destData.getKey();
                List<node_data> list = graphAlgo.shortestPath(src, dest);
                allPaths.get(src).put(dest, list);
            }
        }
    }

    public void setGameAgents(game_service game, directed_weighted_graph graph, List<CL_Pokemon> pokemonList) {
        int agents = anAgentInt(game);
        for (int i = 0; i < agents; i++) {
            game.addAgent(pokemonList.get(i).get_edge().getSrc());
        }
    }

    public int anAgentInt(game_service game) {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            int agents = jsonObject.getJSONObject("GameServer").getInt("agents");
            return agents;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<CL_Pokemon> UNConnectedSetAgents(game_service game, directed_weighted_graph graph, List<CL_Pokemon> pokemonList) {
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(graph);

        List<CL_Pokemon> reachablePokemons = new ArrayList<>();
        Iterator<CL_Pokemon> pokemonIterator = pokemonList.listIterator();
        while (pokemonIterator.hasNext()) {
            CL_Pokemon tempPokemon = pokemonIterator.next();
            Iterator<node_data> nodeDataIterator = graph.getV().iterator();
            while (nodeDataIterator.hasNext()) {
                node_data tempNodeData = nodeDataIterator.next();
                int src = tempPokemon.get_edge().getDest();
                int dest = tempNodeData.getKey();
                double dist = graphAlgorithms.shortestPathDist(src, dest);
                if (dist != Double.MAX_VALUE && !(reachablePokemons.contains(tempPokemon))) {
                    reachablePokemons.add(tempPokemon);
                }
            }
        }
        return reachablePokemons;
    }

    public void setNext(game_service game, List<CL_Pokemon> pokemonList) {
        //Do not use myArena pokemons => need update
        List<CL_Agent> agentList = Arena.getAgents(game.getAgents(), myArena.getGraph());
        List<CL_Pokemon>Pokemons = Arena.json2Pokemons(game.getPokemons());
        myArena.setAgents(agentList);
        myArena.setPokemons(Pokemons);
        for (CL_Pokemon pokemon : myArena.getPokemons()) {
            Arena.updateEdge(pokemon, myArena.getGraph());
        }

        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(myArena.getGraph());

        int from = 0, to = 0;

        for (int i = 0; i < agentList.size(); i++) {
            int delete_index = 0;
            int delete_other_index = -1;
            double min = Double.MAX_VALUE;
            int src = agentList.get(i).getSrcNode();
            for (int j = 0; j < Pokemons.size(); j++) {
                src = agentList.get(i).getSrcNode();
                int dest = Pokemons.get(j).get_edge().getDest();
                int destTempSt = dest;
                if (src == dest)
                    destTempSt = Pokemons.get(j).get_edge().getSrc();
                graphAlgorithms.init(myArena.getGraph());
                double Weight = graphAlgorithms.shortestPathDist(src, destTempSt);
                if (Weight < min) {
                    from = src;
                    to = destTempSt;
                    min = Weight;
                    delete_index = j;
                }
                for (int k = i+1; k < agentList.size(); k++) {
                    int tempSrc = agentList.get(k).getSrcNode();
                    int destTemp = dest;
                    if (tempSrc == dest)
                        destTemp = Pokemons.get(j).get_edge().getSrc();
                    graphAlgorithms.init(myArena.getGraph());
                    double tempWeight = graphAlgorithms.shortestPathDist(tempSrc, destTemp);
                    if (tempWeight < min) {
                        from = tempSrc;
                        to = destTemp;
                        min = tempWeight;
                        delete_index = j;
                        int change_index = k;
                        CL_Agent tempAgent = agentList.get(i);
                        agentList.set(i, agentList.get(change_index));
                        agentList.set(change_index, tempAgent);
                    }
                }

//                graphAlgorithms.init(myArena.getGraph());
//                double weight = graphAlgorithms.shortestPathDist(src, dest);
//                if (weight < min) {
//                    from = src;
//                    to = dest;
//                    min = weight;
//                    delete_index = j;
//                }


                if(anAgentInt(game)<Pokemons.size()){
                    Point3D p = ifSomeOneClose(Pokemons,Pokemons.get(j).get_edge().getDest());
                    if(p!=null){
                        for (int k = 0; k < Pokemons.size(); k++) {
                            if(k!=j){
                                if(Pokemons.get(k).getLocation()==p)
                                    delete_other_index= k;
                            }
                        }
                    }
                }
            }

            if (min < Double.MAX_VALUE) {
                Pokemons.remove(delete_index);
                if(delete_other_index!=-1)
                    Pokemons.remove(delete_other_index);
                int dest = allPaths.get(from).get(to).get(1).getKey();
                int ID = agentList.get(i).getID();
                if (nextNodes.keySet().contains(ID))
                    nextNodes.remove(ID);
                Arena.getAgents(game.getAgents(), myArena.getGraph()).get(i).setNextNode(dest);
                this.nextNodes.put(ID, dest);
            }
        }
    }

    public void moveAgents(game_service game, HashMap<Integer, Integer> nextNodes, List<CL_Pokemon> pokemonList) {
        int i = 0;
        double dt = 78;
        HashMap<Integer, CL_Pokemon> pokemonHashMap = new HashMap<>();
        Iterator<CL_Pokemon> pokemonIterator = pokemonList.listIterator();

        while (pokemonIterator.hasNext()) {

            CL_Pokemon tempPokemon = pokemonIterator.next();
            Arena.updateEdge(tempPokemon, myArena.getGraph());
            pokemonHashMap.put(tempPokemon.get_edge().getDest(), tempPokemon);
            pokemonHashMap.put(tempPokemon.get_edge().getSrc(), tempPokemon);

        }

        List<CL_Agent> agentList = Arena.getAgents(game.getAgents(), myArena.getGraph());
        Iterator<CL_Agent> agentIterator = agentList.listIterator();

        while (agentIterator.hasNext()) {

            CL_Agent tempAgent = agentIterator.next();
            int ID = tempAgent.getID();
            if (nextNodes.get(ID) == null)
                continue;
            int key = nextNodes.get(ID);

            if (tempAgent.getSpeed() > 2) dt = dt * 0.75;
            if (pokemonHashMap.get(key) != null)
                if (tempAgent.getSpeed() > 1)
                    dt = dt / (Math.pow(tempAgent.getSpeed(), 2)) + tempAgent.getSpeed();

            tempAgent.setNextNode(key);
            game.chooseNextEdge(ID, key);
            try {
                Thread.sleep((long) dt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (agentList.get(i).getSrcNode() != tempAgent.getSrcNode()) {
                System.out.println("Agent: " + tempAgent.getID() + ", val: " + tempAgent.getValue() + ",  " +
                        tempAgent.getSrcNode() + " , turned to node: " + key + " Speed-  " + tempAgent.getSpeed());
            }
        }
        game.move();
    }

    /**
     * This function is based on the assumption that the pokemons are coming from a high chances place to get pokemons
     * it checks if it can keep to be in the "good" loop
     * @param nodeTo
     * @param nodeFrom
     * @return
     */
    public boolean avoidFromTraps(int nodeTo, int nodeFrom){
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        for(edge_data edgeData : myArena.getGraph().getE(nodeTo)){
            if(edgeData.getSrc() == nodeTo){
                graphAlgorithms.init(myArena.getGraph());
                int from = edgeData.getDest();
                if(graphAlgorithms.shortestPathDist(from,nodeFrom)<Double.MAX_VALUE)
                    return false;
            }
        }
        return true;
    }

    /**
     *Commented function, if you cant work it out, delete it.
     */
//    public List<CL_Pokemon> keep2Meters(List<CL_Pokemon> pokemons, List<CL_Agent> agents, game_service game){
//        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
//        graphAlgorithms.init(myArena.getGraph());
//
//        double MaxFromMin = 0;
//        HashMap<CL_Pokemon,CL_Pokemon> hashMapToList = new HashMap<>();
//        CL_Pokemon[] array = new CL_Pokemon[6];
//        HashMap<Double,HashMap<Integer,Integer>> helper = new HashMap<>();
//        for (int i = 0; i < 3; i++) {
//            array[i] = pokemons.get(i);
//            array[i+3] = pokemons.get(0);
//            hashMapToList.put(pokemons.get(i),pokemons.get(0));
//            int pokeI = pokemons.get(i).get_edge().getSrc();
//            int pokeJ = pokemons.get(0).get_edge().getDest();
//            double dist = graphAlgorithms.shortestPathDist(pokeI,pokeJ);
//            if(MaxFromMin < dist)
//                MaxFromMin = dist;
//            HashMap<Integer,Integer> temp = new HashMap<>();
//            temp.put(pokeI,pokeJ);
//            helper.put(dist,temp);
//        }
//
//        for (int i = 0; i < pokemons.size(); i++) {
//            for (int j = 0; j < pokemons.size(); j++) {
//                if(i!=j){
//                    int pokeI = pokemons.get(i).get_edge().getSrc();
//                    int pokeJ = pokemons.get(j).get_edge().getDest();
//                    double dist = graphAlgorithms.shortestPathDist(pokeI,pokeJ);
//                    if(dist < MaxFromMin){
//                        array[i] = pokemons.get(i);
//                        array[i+3] = pokemons.get(j);
//                        hashMapToList.put(pokemons.get(i),pokemons.get(j));
//                        helper = keep2MetersHelper(helper,dist,pokeI,pokeJ);
//                        MaxFromMin = keep2MetersMaxFromMin(helper);
//                    }
//                }
//            }
//        }
//        CL_Pokemon[] array = new CL_Pokemon[6];
//        int index = 0;
//        for (CL_Pokemon poke : hashMapToList.keySet()) {
//            array[index] = poke;
//            array[index+3] = hashMapToList.get(poke);
//            index++;
//        }
//
////        return
//    }
//    public HashMap<Double,HashMap<Integer,Integer>> keep2MetersHelper(HashMap<Double,HashMap<Integer,Integer>> helper
//            , double dist, int src, int dest){
//        for (double distance :helper.keySet()) {
//            if (dist < distance){
//                helper.remove(distance);
//                HashMap<Integer,Integer> temp = new HashMap<>();
//                temp.put(src,dest);
//                helper.put(dist,temp);
//                return helper;
//            }
//        }
//        return helper;
//    }
//
//    public Double keep2MetersMaxFromMin(HashMap<Double,HashMap<Integer,Integer>> helper){
//        double MaxFromMin = 0;
//        for (Double dd : helper.keySet()){
//            if(MaxFromMin < dd)
//                MaxFromMin = dd;
//        }
//        return MaxFromMin;
//    }

    /**
     * The function indicates if there is 2 pokemons that close distance
     * If there is close distance pokemons (less then 8) so the agent will take him too, because they are close
     * @param pokemons
     * @param src
     * @return
     */
    public Point3D ifSomeOneClose(List<CL_Pokemon>pokemons, int src){
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(myArena.getGraph());
        for (CL_Pokemon poke : pokemons){
            int dest = poke.get_edge().getDest();
            if(graphAlgorithms.shortestPathDist(src,dest) < 2)
                return poke.getLocation();
        }
        return null;
    }

}
