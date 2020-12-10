package gameClient;
import Server.Game_Server_Ex2;
import api.DWGraph_Algo;
import api.game_service;
import api.geo_location;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import gameClient.util.Point3D;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Ex2 {
    private game_service game;
    private Arena arena;
    private HashMap<Integer,HashMap<Integer, List<api.node_data>>> dijkstraAllMap;
    HashMap<CL_Agent,Integer> nextNode;

    public Ex2(int level) {
        this.game = Game_Server_Ex2.getServer(level);
        this.arena = new Arena();
        api.dw_graph_algorithms graphAlgo = setGraph(game.getGraph());//can't be null
        arena.setGraph(graphAlgo.getGraph());
        arena.setPokemons(arena.json2Pokemons(game.getPokemons()));

        //update edges of the pokemons. DO NOT REMOVE without consulting.
        for(CL_Pokemon poke : arena.getPokemons()){
            arena.updateEdge(poke,arena.getGraph());
        }

//      need to add the agents to the game before we add them to the graph
        SetGameAgents();
        //did it
        arena.setAgents(arena.getAgents(game.getAgents(),arena.getGraph()));

        this.dijkstraAllMap = new HashMap<>();
        dijkstraAll(graphAlgo);

        //for move function
        this.nextNode = new HashMap<>();
        setNextNodes();
    }

    public static void main(String[] args) {
        Ex2 ex2 = new Ex2(22);
        System.out.println(ex2.game.toString());
        System.out.println(ex2.dijkstraAllMap.get(0).get(5).get(2).getKey());


        //game loop
//        ex2.game.startGame();
//        int valueAll = 0;
//        while(ex2.game.isRunning()){
//
//        }
    }

    /**
     * The original save won't work so i have to create this
     * *until we find something else
     * @param JsonGraph
     * @return
     */
    public static api.dw_graph_algorithms setGraph(String JsonGraph){
        //The original save won't work here
        api.dw_graph_algorithms ga = new DWGraph_Algo();
        try{
            Writer writer = new FileWriter("myGraph.json");
            writer.write(JsonGraph);
            writer.flush();
        } catch (IOException e) {
            //if won't work return empty graph
            e.printStackTrace();
            return ga;
        }
        if(ga.load("myGraph.json"));
            return ga;
    }

    /**
     * Return how much agents we can add in game
     * It helpful so I think keep it.
     * @param game
     * @return int
     */
    public static int howMuchAgents(String game){
        JsonElement gameElement = JsonParser.parseString(game);
        JsonObject pokemonsObject = gameElement.getAsJsonObject();
        JsonElement gameServerElement = pokemonsObject.get("GameServer");
        JsonObject gameServerObject = gameServerElement.getAsJsonObject();
        return gameServerObject.get("agents").getAsInt();
    }

    /**
     * To shortest path from any node to pokemon we should Transpose the graph and use dijkstra
     * while dijkstra is O((V+E)*log(V)) we could transpose once and use dijkstra to each vertex in graph
     * that will increase complexity to O((V+E)(V*log(V))) + E
     * while the complexity can be struggle it not something that happening in game
     * the space complexity added will be O(v*v) which isn't so bad...
     */
    public void dijkstraAll(api.dw_graph_algorithms graphAlgo){

        api.directed_weighted_graph graph = graphAlgo.getGraph();
        //added empty map to add values after
        for (api.node_data nodeData : graph.getV()){
            HashMap<Integer, List<api.node_data>> innerMap = new HashMap<Integer, List<api.node_data>>();
            dijkstraAllMap.put(nodeData.getKey(), innerMap);
        }

        for (api.node_data srcData :graph.getV()){
            int src = srcData.getKey();
            for(api.node_data destData :graph.getV()){
                int dest = destData.getKey();
                List<api.node_data> list = graphAlgo.shortestPath(src, dest);
                dijkstraAllMap.get(src).put(dest, list);
            }
        }
    }

    /**
     * Set agents at the start
     * It great when you ready to collect pokemon in your first move
     */
    public void SetGameAgents(){
        List<CL_Pokemon> gotOne= this.arena.getPokemons();
        for (int i = 0; i < howMuchAgents(this.game.toString()); i++){
            if(i+1 > this.arena.getPokemons().size()){
                i=howMuchAgents(this.game.toString());
            }
            game.addAgent(gotOne.get(i).get_edge().getSrc());
        }
    }
    /**
     * Set the next nodes for each move by compare between paths weights
     * I think we could use it in any while loop
     */
    public void setNextNodes(){

        List<CL_Pokemon> gotOne= this.arena.getPokemons();
        List<CL_Agent> haveOne= this.arena.getAgents();

        int from = 0;
        int to = 0;

        for (int i = 0; i < haveOne.size(); i++) {

            int src = haveOne.get(i).getSrcNode();
            double min = Double.MAX_VALUE;
            int deleteIndex = 0;

            for (int j = 0; j < gotOne.size(); j++) {

                int dest = gotOne.indexOf(j);
                double pathWeight = getPathWeight(this.dijkstraAllMap.get(src).get(dest));

                if(pathWeight<min) {
                    from = src;
                    to = dest;
                    min = pathWeight;
                    deleteIndex = j;

                }
            }
            //if the agent don't have path to any pokemon
            if(min < Double.MAX_VALUE){
                gotOne.remove(deleteIndex);
                this.nextNode.put(haveOne.get(i), this.dijkstraAllMap.get(from).get(to).get(1).getKey());
            }
            else
                this.nextNode.put(haveOne.get(i), haveOne.get(i).getSrcNode());//no Move...
        }
    }

    /**
     * Get weight path of dijkstraAllMap
     */
    public double getPathWeight(List<api.node_data> path){
        api.directed_weighted_graph graph = this.arena.getGraph();
        double PathWeight = 0;
        if(path.size() <= 1)
            return 0;
        else{
            for (int i = 0; i < path.size()-1; i++) {
                PathWeight += graph.getEdge(i,i+1).getWeight();
            }
            return PathWeight;
        }
    }
}
