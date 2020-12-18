package gameClient;

import Server.Game_Server_Ex2;
import api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class Ex2 implements Runnable {

    private int gameLevel ;
    private int ID ;
    private MyFrame _win;
    private Arena myArena;
    private HashMap<Integer, HashMap<Integer, List<node_data>>> allPaths;
    private HashMap<Integer, HashMap<Integer, edge_data>> edgeDataHashMap;
    private HashMap<Integer, HashMap<Integer, Double>> allPathsWeights;
    private String string = "";
    private static game_service game;
    private long dt;
    private List<CL_Agent> agentList;

    public static void main(String[] args) {
        String id = args[0];
        String level = args[1];
        Ex2 c = new Ex2();
        c.ID=Integer.parseInt(id);
        c.gameLevel = Integer.parseInt(level);

        Thread thread = new Thread(c);
        thread.setName("PokemonGame");
        thread.start();
    }

    @Override
    public void run() {

        game = Game_Server_Ex2.getServer(gameLevel);
        game.login(ID);
        initiate(game);
        List<CL_Agent> agentList = Arena.getAgents(game.getAgents(), myArena.getGraph());
        game.startGame();
        long StartTime = game.timeToEnd() / 1000;
        while (game.isRunning()) {

            long time = game.timeToEnd() / 1000;
            _win.update(myArena);
            _win.paint(_win.getGraphics());
            _win.setTitle("As GameEx2: " + "TimeToEnd: " + time + "Moves: " + movesToJSON() + "Grade: " + gradeToJSON());

            synchronized (Thread.currentThread()) {
                MoveAgents();
            }
            try {
                Thread.sleep(dt);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println(game.toString());
        System.exit(0);
    }

    /**
     * Receive JSON String that represent the current Graph for THE Game
     * Return an actual Graph that we can "WORK" on.
     *
     * @param game
     * @return
     */
    public directed_weighted_graph setGraph(game_service game) {

        DWGraph_DS_Deserializer deserializer = new DWGraph_DS_Deserializer();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(directed_weighted_graph.class, deserializer);
        Gson gson = gsonBuilder.create();
        directed_weighted_graph graph = gson.fromJson(game.getGraph(), directed_weighted_graph.class);

        return graph;
    }

    /**
     * The init function of the game process
     * Build the arena Components
     * Set the Starting Point OF the Agents
     * And Receive all that there is to "KNOW" about the current Graph
     *
     * @param game
     */
    public void initiate(game_service game) {

        directed_weighted_graph graph = setGraph(game);
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();

        myArena = new Arena();
        myArena.setGraph(graph);

        List<CL_Pokemon> pokemonList = Arena.json2Pokemons(game.getPokemons());
        myArena.setPokemons(pokemonList);

        for (int i = 0; i < pokemonList.size(); i++) {
            Arena.updateEdge(pokemonList.get(i), graph);
        }

        setAllPathsWeights(graph);
        edgeDataHashMap = allEdges(graph);
        SortByValue(pokemonList);
        findShortestPaths(graph);
        setGameAgents(game, graph, pokemonList);

        _win = new MyFrame("GameEx2 - " + game.toString());

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int width = dimension.width;
        int height = dimension.height;

        _win.setSize(width - 100, height - 100);
        _win.update(myArena);
        _win.setVisible(true);
        _win.paint(_win.getGraphics());
        _win.repaint();
        _win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    /**
     * Under initial function:
     * Using Dijkstra Algorithm
     * For receiving before Game.Start all ShortestPaths Weights for each Node of the Graph
     * For lighter game operation
     *
     * @param graph
     */
    public void setAllPathsWeights(directed_weighted_graph graph) {

        allPathsWeights = new HashMap<>();
        for (node_data nodeData : graph.getV()) {
            HashMap<Integer, Double> innerMap = new HashMap<Integer, Double>();
            allPathsWeights.put(nodeData.getKey(), innerMap);
        }

        dw_graph_algorithms graphAlgo = new DWGraph_Algo();
        graphAlgo.init(graph);

        for (node_data srcData : graph.getV()) {
            int src = srcData.getKey();
            for (node_data destData : graph.getV()) {
                int dest = destData.getKey();
                double dist = graphAlgo.shortestPathDist(src, dest);
                allPathsWeights.get(src).put(dest, dist);
            }
        }
    }

    /**
     * Under initial function:
     * Using Dijkstra Algorithm
     * For receiving before Game.Start all ShortestPaths for each Node of the Graph
     * For lighter game operation
     *
     * @param graph
     */
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

    /**
     * Adding Agents to the Game
     * Placing them close to the Ideal Pokemon
     * Also Calculate if there is a Pokemon that will make the agent "Stuck"
     * if there is such the Agents will ignore it
     *
     * @param game
     * @param graph
     * @param pokemonList
     */
    public void setGameAgents(game_service game, directed_weighted_graph graph, List<CL_Pokemon> pokemonList) {

        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(graph);

        if (!(graphAlgorithms.isConnected())) {

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
            int agents = anAgentInt();

            for (int i = 1; i < reachablePokemons.size(); i++) {

                double a = reachablePokemons.get(i - 1).getValue();
                double b = reachablePokemons.get(i).getValue();
                double edgeA = reachablePokemons.get(i - 1).get_edge().getWeight();
                double edgeB = reachablePokemons.get(i).get_edge().getWeight();

                if (a / edgeA < b / edgeB)
                    Swap(reachablePokemons.get(i - 1), reachablePokemons.get(i)); //Take the Highest Value to the head of the List
            }
            for (int i = 0; i < agents; i++) {
                game.addAgent(reachablePokemons.get(i).get_edge().getSrc());
            }
        } else {
            int agents = anAgentInt();

            for (int i = 1; i < pokemonList.size(); i++) {

                double a = pokemonList.get(i - 1).getValue();
                double b = pokemonList.get(i).getValue();
                double edgeA = pokemonList.get(i - 1).get_edge().getWeight();
                double edgeB = pokemonList.get(i).get_edge().getWeight();

                if (a / edgeA < b / edgeB)
                    Swap(pokemonList.get(i - 1), pokemonList.get(i)); //Take the Highest Value to the head of the List
            }
            for (int i = 0; i < agents; i++) {
                game.addAgent(pokemonList.get(i).get_edge().getSrc());
            }
        }

    }

    /**
     * return how many agents are in game
     *
     * @return
     */
    public int anAgentInt() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            int agents = jsonObject.getJSONObject("GameServer").getInt("agents");
            return agents;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * In use when initiate the game
     * receive all Edges Of the Graph
     *
     * @param graph
     * @return
     */
    public HashMap<Integer, HashMap<Integer, edge_data>> allEdges(directed_weighted_graph graph) {
        HashMap<Integer, HashMap<Integer, edge_data>> allEdges = new HashMap<>();
        for (node_data vertex : graph.getV()) {
            HashMap<Integer, edge_data> map = new HashMap<>();
            allEdges.put(vertex.getKey(), map);
        }
        for (node_data vertex : graph.getV()) {
            for (edge_data edge : graph.getE(vertex.getKey())) {
                allEdges.get(vertex.getKey()).put(edge.getSrc(), edge);
            }
        }
        return allEdges;
    }

    /**
     * In use when move function
     * Check if there are 2 OR more Pokemons on the same Edge
     * If there are more at least 2 -> Put in a List<CL_Pokemon>.
     * The MoveAgents Function count them as a Single Pokemon.
     *
     * @param pokemonList
     * @return
     */
    public HashMap<edge_data, List<CL_Pokemon>> MultiEdge(List<CL_Pokemon> pokemonList) {
        HashMap<edge_data, List<CL_Pokemon>> SharedEdges = new HashMap<>();

        for (CL_Pokemon pokemon : pokemonList) {

            edge_data sharedEdge = pokemon.get_edge();

            for (CL_Pokemon clPokemon : pokemonList) {

                if (SharedEdges.get(sharedEdge) == null) {
                    ArrayList<CL_Pokemon> pokes = new ArrayList<>();
                    SharedEdges.put(sharedEdge, pokes);
                }
                if (sharedEdge.equals(clPokemon.get_edge()) && !(SharedEdges.get(sharedEdge).contains(clPokemon))) {
                    SharedEdges.get(sharedEdge).add(clPokemon);
                }
            }
        }

        return SharedEdges;
    }

    /**
     * Sorting, but for Pokemons
     * By value
     *
     * @param pokemonList
     */
    public void SortByValue(List<CL_Pokemon> pokemonList) {
        for (int i = 1; i < pokemonList.size(); i++) {
            double value1 = pokemonList.get(i - 1).getValue();
            double value2 = pokemonList.get(i).getValue();
            if (value1 < value2) {
                Swap(pokemonList.get(i - 1), pokemonList.get(i));
            }
        }
    }

    /**
     * return the sum of value of the pokemon list.
     *
     * @param pokemonList
     * @return
     */
    public double getSumValue(List<CL_Pokemon> pokemonList) {
        double sum = 0;
        for (int i = 0; i < pokemonList.size(); i++) {
            sum += pokemonList.get(i).getValue();
        }
        return sum;
    }

    /**
     * The generic Swap function
     *
     * @param a
     * @param b
     */
    public void Swap(CL_Pokemon a, CL_Pokemon b) {
        CL_Pokemon temp = a;
        a = b;
        b = temp;
    }

    /**
     * The main logical core Of the game
     * Updates arena
     * Using allPathsWeights for checking the shortest path
     * between agents and pokemons and assign each to another.
     * also set the amount of sleep mil-sec for all agents
     */
    public void MoveAgents() {
        game.move();
        agentList = Arena.getAgents(game.getAgents(), myArena.getGraph());
        List<CL_Pokemon> pokemonList = Arena.json2Pokemons(game.getPokemons());

        myArena.setAgents(agentList);
        myArena.setPokemons(pokemonList);

        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(myArena.getGraph());

        for (int i = 0; i < pokemonList.size(); i++) {
            Arena.updateEdge(pokemonList.get(i), myArena.getGraph());
        }

        HashMap<edge_data, List<CL_Pokemon>> sharedEdges = MultiEdge(pokemonList);
        List<CL_Pokemon> curFruit = new ArrayList<>();

        for (CL_Agent agent : agentList) {
            double tempVal = 0;
            double tempSVT = Double.MAX_VALUE;
            for (List<CL_Pokemon> pokemonSet : sharedEdges.values()) {

                int src = agent.getSrcNode();
                CL_Pokemon pokemon = pokemonSet.get(0);
                int closePokes = pokemon.get_edge().getSrc();

                double weight = allPathsWeights.get(src).get(closePokes);
                double value = getSumValue(pokemonSet);

                if (weight != 0) {
                    if (weight / agent.getSpeed() < tempSVT) {
                        tempSVT = weight / agent.getSpeed();
                        if (value / weight > tempVal && !(curFruit.contains(pokemon))) {
                            agent.set_curr_fruit(pokemon);
                            tempVal = value / weight;
                        }
                    }
                } else {
                    agent.set_curr_fruit(pokemon);
                    break;
                }
                for (edge_data edge : edgeDataHashMap.get(closePokes).values()) {
                    for (int i = 0; i < pokemonList.size(); i++) {
                        if (pokemonList.get(i).get_edge().equals(edge)) {
                            curFruit.add(pokemonList.get(i));
                        }
                    }
                }
                if (agent.get_curr_fruit() == null) {
                    for (CL_Pokemon pokemon1 : pokemonList) {
                        if (!curFruit.contains(pokemon1))
                            agent.set_curr_fruit(pokemon1);
                    }
                }
                if (agent.get_curr_fruit() == null)
                    agent.set_curr_fruit(pokemon);
            }
            curFruit.add(agent.get_curr_fruit());
        }
        long d = Long.MAX_VALUE;
        for (CL_Agent agent : agentList) {
            double value = agent.getLocation().distance(agent.get_curr_fruit().getLocation());
            String agentWhere = "";
            int src = agent.getSrcNode();
            int dest = agent.get_curr_fruit().get_edge().getSrc();

            List<node_data> list = allPaths.get(src).get(dest);
            if (list.size() > 1) {
                synchronized (Thread.currentThread()) {
                    game.chooseNextEdge(agent.getID(), list.get(1).getKey());
                }
                agentWhere = "Agent: " + agent.getID() + ", val: " + agent.getValue() + "   turned to node: " + list.get(1).getKey();
            } else {
                synchronized (Thread.currentThread()) {
                    game.chooseNextEdge(agent.getID(), agent.get_curr_fruit().get_edge().getDest());
                }
                agentWhere = "Agent: " + agent.getID() + ", val: " + agent.getValue() + "   turned to node: " + agent.get_curr_fruit().get_edge().getDest();
            }
            if (!string.equals(agentWhere)) {
                string = agentWhere;
            }

            if (agent.getLocation().distance(agent.get_curr_fruit().getLocation()) <= value)
                if (d > Sleeeep(agent, agentList.size())) {
                    d = Sleeeep(agent, agentList.size());
                }
        }
        dt = d;
    }

    /**
     * return the sleep time for agent using
     * Agent Class function get_sg_dt
     * Calculate the time needed for the Agent to reach is Target Pokemon
     *
     * @param agent
     * @param A
     * @return
     */
    public long Sleeeep(CL_Agent agent, int A) {
        if (A > 1) A = A * 70;
        agent.set_SDT(A + 19);
        long dt = agent.get_sg_dt();
        return dt;
    }

    /**
     * Return how much pokemons in game
     * Used in game gui
     *
     * @return int
     */
    public int pokeToJSON() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            JSONObject object = jsonObject.getJSONObject("GameServer");
            return object.getInt("pokemons");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Return boolean for game.isLoggedIn()
     * Used in game gui
     *
     * @return
     */
    public boolean isLoggedToJSON() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            JSONObject object = jsonObject.getJSONObject("GameServer");
            return object.getBoolean("is_logged_in");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * return the number of moves till now
     * Used in game gui
     *
     * @return
     */
    public int movesToJSON() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            JSONObject jsonArray = jsonObject.getJSONObject("GameServer");
            return jsonArray.getInt("moves");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Return grade till now
     * Used in game gui
     *
     * @return
     */
    public int gradeToJSON() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            JSONObject object = jsonObject.getJSONObject("GameServer");
            return object.getInt("grade");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * return game level
     * Used in game gui
     *
     * @return
     */
    public int gameLevelToJSON() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            JSONObject object = jsonObject.getJSONObject("GameServer");
            return object.getInt("game_level");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * return id of User
     * Used in game gui
     *
     * @return
     */
    public int idToJSON() {
        try {
            JSONObject jsonObject = new JSONObject(game.toString());
            JSONObject object = jsonObject.getJSONObject("GameServer");
            return object.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Only used in game game gui
     *
     * @return long
     */
    public long timeToEnd() {
        return game.timeToEnd();
    }

}


