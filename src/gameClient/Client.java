package gameClient;

import Server.Game_Server_Ex2;
import api.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;


public class Client implements Runnable {
    // private game_service game;
    private int levelNumber;
    private static MyFrame _win;
    private Arena myArena;
    private HashMap<Integer, HashMap<Integer, List<node_data>>> allPaths;
    private HashMap<Integer, HashMap<Integer, edge_data>> edgeDataHashMap;
    private HashMap<edge_data, List<CL_Pokemon>> SharedEdges;
    private HashMap<Integer, HashMap<Integer, Double>> allPathsWeights;


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
        Scanner sc = new Scanner(System.in);
        System.out.println("Please Choose A level between [0-23]");
        levelNumber = sc.nextInt();
        game_service game = Game_Server_Ex2.getServer(levelNumber);

        initiate(game);
        game.startGame();
        while (game.isRunning()) {
            try {

                _win.update(myArena);
                _win.paint(_win.getGraphics());
                _win.setTitle("As GameEx2: " + game.toString());
                _win.repaint();

                synchronized (this) {
                    MoveAgentsV2(game);
                }

                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String res = game.toString();
        System.out.println(res);
        System.exit(0);
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
            int agents = anAgentInt(game);

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
            int agents = anAgentInt(game);

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

    public double pathWeight(int src, int dest, directed_weighted_graph graph) {

        double weight = 0;
        List<node_data> nodeDataList = allPaths.get(src).get(dest);
        for (int i = 1; i < nodeDataList.size(); i++) {

            int key1 = nodeDataList.get(i - 1).getKey();
            int key2 = nodeDataList.get(i).getKey();
            weight += graph.getEdge(key1, key2).getWeight();

        }

        return weight;
    }

    public void Swap(CL_Pokemon a, CL_Pokemon b) {
        CL_Pokemon temp = a;
        a = b;
        b = temp;
    }

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

    public void MoveAgentsV2(game_service game) {

        String moves = game.move();

        List<CL_Agent> agentList = Arena.getAgents(moves, myArena.getGraph());
        List<CL_Pokemon> pokemonList = Arena.json2Pokemons(game.getPokemons());

        myArena.setAgents(agentList);
        myArena.setPokemons(pokemonList);

        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(myArena.getGraph());

        for (int i = 0; i < pokemonList.size(); i++) {
            Arena.updateEdge(pokemonList.get(i), myArena.getGraph());
        }

        SharedEdges = MultiEdge(pokemonList);
        List<CL_Pokemon> curFruit = new ArrayList<>();

        for (CL_Agent agent : agentList) {
            double tempVal = 0;
            for (List<CL_Pokemon> pokemonSet : SharedEdges.values()) {

                int src = agent.getSrcNode();
                CL_Pokemon pokemon = pokemonSet.get(0);
                int closePokes = pokemon.get_edge().getSrc();

                double weight = allPathsWeights.get(src).get(closePokes);

                double value = 0;
                for (int i = 0; i < pokemonSet.size(); i++) {
                    value += pokemonSet.get(i).getValue();
                }

                if (weight != 0) {
                    if (value / weight > tempVal && !(curFruit.contains(pokemonSet.get(0)))) {
                        agent.set_curr_fruit(pokemonSet.get(0));
                        tempVal = value / weight;
                    }
                } else {
                    agent.set_curr_fruit(pokemonSet.get(0));
                    break;
                }

                for (edge_data edge : edgeDataHashMap.get(closePokes).values()) {
                    for (int i = 0; i < pokemonList.size(); i++) {
                        if (pokemonList.get(i).get_edge() == edge) {
                            curFruit.add(pokemonList.get(i));
                        }
                    }
                }
            }
            curFruit.add(agent.get_curr_fruit());
        }


        for (CL_Agent agent : agentList) {
            if (agent.get_curr_fruit() != null) {

                int src = agent.getSrcNode();
                int dest = agent.get_curr_fruit().get_edge().getSrc();

                List<node_data> list = allPaths.get(src).get(dest);
                if (list.size() > 1) {
                    game.chooseNextEdge(agent.getID(), list.get(1).getKey());
                } else {
                    game.chooseNextEdge(agent.getID(), agent.get_curr_fruit().get_edge().getDest());
                }
            } else {
                game.chooseNextEdge(agent.getID(), agent.getSrcNode());
            }
        }
    }


    public List<CL_Pokemon> pokeOnPath(List<node_data> nodeDataList, List<CL_Pokemon> pokemonList, directed_weighted_graph graph) {

        List<edge_data> dataList = new ArrayList<>();
        List<CL_Pokemon> allPokesOnPath = new ArrayList<>();

        for (int i = 1; i < nodeDataList.size(); i++) {
            int src = nodeDataList.get(i - 1).getKey();
            int dest = nodeDataList.get(i).getKey();
            edge_data dataSD = graph.getEdge(src, dest);
            edge_data dataDS = graph.getEdge(dest, src);
            if (dataSD != null) dataList.add(dataSD);
            if (dataDS != null) dataList.add(dataDS);
        }
        System.out.println(dataList);
        System.out.println(nodeDataList);
        for (CL_Pokemon pokemon : pokemonList) {
            System.out.println(pokemon.get_edge());
            if (dataList.contains(pokemon.get_edge())) {
                allPokesOnPath.add(pokemon);
            }
        }

        return allPokesOnPath;
    }

    public void SortByValue(List<CL_Pokemon> pokemonList) {
        for (int i = 1; i < pokemonList.size(); i++) {
            double value1 = pokemonList.get(i - 1).getValue();
            double value2 = pokemonList.get(i).getValue();
            if (value1 < value2) {
                Swap(pokemonList.get(i - 1), pokemonList.get(i));
            }
        }
    }

    public void MoveAgents(game_service game) {

        List<CL_Pokemon> pokemonList = Arena.json2Pokemons(game.getPokemons());
        List<CL_Agent> agentList = Arena.getAgents(game.move(), myArena.getGraph());

        myArena.setAgents(agentList);
        myArena.setPokemons(pokemonList);

        //game.move();
        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(myArena.getGraph());

        for (int i = 0; i < pokemonList.size(); i++) {
            Arena.updateEdge(pokemonList.get(i), myArena.getGraph());
        }
        List<CL_Pokemon> curFruit = new ArrayList<>();
        double tempVal = 0;
        for (CL_Agent agent : agentList) {
            for (CL_Pokemon pokemon : pokemonList) {

                int src = agent.getSrcNode();
                int dest = pokemon.get_edge().getSrc();
                double value = pokemon.getValue();
                double weight = pathWeight(src, dest, myArena.getGraph());

                if (weight != 0) {
                    if (value / weight > tempVal && !(curFruit.contains(pokemon))) {

                        tempVal = value / weight;
                        agent.set_curr_fruit(pokemon);

                    }
                } else {
                    agent.set_curr_fruit(pokemon);
                    break;
                }
            }
            tempVal = 0;
            curFruit.add(agent.get_curr_fruit());
        }
        for (CL_Agent agent : agentList) {

            int src = agent.getSrcNode();
            int dest = agent.get_curr_fruit().get_edge().getSrc();

            List<node_data> list = allPaths.get(src).get(dest);
            if (list.size() > 1)
                game.chooseNextEdge(agent.getID(), list.get(1).getKey());
            else {
                game.chooseNextEdge(agent.getID(), agent.get_curr_fruit().get_edge().getDest());
            }
        }
    }

    public void MoveAgentsV3(game_service game) {

        List<CL_Pokemon> pokemonList = Arena.json2Pokemons(game.getPokemons());
        List<CL_Agent> agentList = Arena.getAgents(game.move(), myArena.getGraph());

        //SortByValue(pokemonList);

        myArena.setAgents(agentList);
        myArena.setPokemons(pokemonList);

        dw_graph_algorithms graphAlgorithms = new DWGraph_Algo();
        graphAlgorithms.init(myArena.getGraph());

        for (int i = 0; i < pokemonList.size(); i++) {
            Arena.updateEdge(pokemonList.get(i), myArena.getGraph());
        }

        List<CL_Pokemon> AlreadyOnHunt = new ArrayList<>();
        HashMap<edge_data, List<CL_Pokemon>> multiEdges = MultiEdge(pokemonList);

        for (CL_Agent agent : agentList) {
            double currentMax = 0;
            for (List<CL_Pokemon> pokemonSet : multiEdges.values()) {

                int src = agent.getSrcNode();
                int dest = pokemonSet.get(0).get_edge().getSrc();

                List<node_data> list = allPaths.get(src).get(dest);
                List<CL_Pokemon> allPokesOnPath = pokeOnPath(list, pokemonList, myArena.getGraph());

                int last = allPokesOnPath.get(allPokesOnPath.size() - 1).get_edge().getDest();

                double pathValue = getSumValue(allPokesOnPath);
                double pathWeight = graphAlgorithms.shortestPathDist(src, last);

                if (pathWeight != 0) {
                    if (pathValue / pathWeight > currentMax && !AlreadyOnHunt.contains(pokemonSet.get(0))) {
                        currentMax = pathValue / pathWeight;
                        agent.set_curr_fruit(pokemonSet.get(0));
                    }

                } else {
                    agent.set_curr_fruit(pokemonSet.get(0));
                }
                AlreadyOnHunt.addAll(pokemonSet);
            }
        }
        for (CL_Agent agent : agentList) {

            int src = agent.getSrcNode();
            int dest = agent.get_curr_fruit().get_edge().getSrc();
            List<node_data> list = allPaths.get(src).get(dest);

            if (list.size() > 1)
                game.chooseNextEdge(agent.getID(), list.get(1).getKey());
            else {
                game.chooseNextEdge(agent.getID(), agent.get_curr_fruit().get_edge().getDest());
            }
        }
    }

    public double getSumValue(List<CL_Pokemon> pokemonList) {
        double sum = 0;
        for (int i = 0; i < pokemonList.size(); i++) {
            sum += pokemonList.get(i).getValue();
        }
        return sum;
    }

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
}


