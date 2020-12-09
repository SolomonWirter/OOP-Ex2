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
import java.util.Collection;
import java.util.LinkedList;

public class Ex2 {
    public static void main(String[] args) {
        game_service game = Game_Server_Ex2.getServer(8);
        //get graph as api.dw_graph_algorithms object
        api.dw_graph_algorithms graphAlgo = setGraph(game.getGraph());
        System.out.println(game.toString());

        System.out.println(howMuchAgents(game.toString()));
//        System.out.println(game.timeToEnd());
//


//        System.out.println(graphAlgo.getGraph());
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
     * The function is get a pokemon position, it's type and a graph (Game.getGraph)
     * type is needed for us to know if the
     * and return the edge they are on.
     * @param p
     * @return
     */
    public api.edge_data getEdge(Point3D p, int type, api.directed_weighted_graph graph){
        for (api.node_data nodeData : graph.getV()){
            for (api.edge_data edgeData:graph.getE(nodeData.getKey())){
                //this line checks if type checks out with src node and dest node // rules of assignment
                if(!((type == 1 && edgeData.getSrc() > edgeData.getDest())||(type == -1 &&edgeData.getSrc() > edgeData.getDest()))){
                    double xSrc = graph.getNode(edgeData.getSrc()).getLocation().x();
                    double xDest = graph.getNode(edgeData.getDest()).getLocation().x();
                    double ySrc = graph.getNode(edgeData.getSrc()).getLocation().y();
                    double yDest = graph.getNode(edgeData.getDest()).getLocation().y();

                    if ((yDest < p.y() && ySrc > p.y()) || (yDest > p.y() && ySrc < p.y())) {
                        if ((xDest < p.x() && xSrc > p.x()) || (xDest > p.x() && xSrc < p.x())) {
                            //m = y1-y2/x1-x2
                            double m = (ySrc - p.y()) / (xSrc - p.x());
                            //y-y1 = m(x-x1) => y = m(x+x1) + y1
                            double isIt = m * (xDest - xSrc) + ySrc;
                            if (isIt == yDest)
                                return edgeData;
                        }
                    }
                }
            }
        }
        //if null it means that haven't pokemon on the graph edges..
        return null;
    }

    /**
     * deserializer for Pokemons that we are getting from graph
     * return a list of pokemons but we need constructor without edge=>
     * Maybe we could use the get Edge method for each pokemon and then use the edge for the constructor
     */
    public LinkedList<CL_Pokemon> Deserializer_CL_Pokemon(String pokemonJson, api.directed_weighted_graph graph) {
        LinkedList<CL_Pokemon> l = new LinkedList<CL_Pokemon>();
        JsonElement pokemonsElement = JsonParser.parseString(pokemonJson);
        JsonObject pokemonsObject = pokemonsElement.getAsJsonObject();

        JsonArray pokemonsJsonArray = pokemonsObject.getAsJsonArray("Pokemons");
        JsonObject jsonPokemons;

        for (int i = 0; i < pokemonsJsonArray.size(); i++) {
            jsonPokemons = pokemonsJsonArray.get(i).getAsJsonObject();

            int type = jsonPokemons.get("type").getAsInt();
            double value = jsonPokemons.get("value").getAsDouble();
            String point = jsonPokemons.get("pos").getAsString();
            String[] pointArray = point.split(",");

            double x = Double.parseDouble(pointArray[0]);
            double y = Double.parseDouble(pointArray[1]);
            double z = Double.parseDouble(pointArray[2]);

            Point3D p = new Point3D(x, y, z);
            api.edge_data edgeData = getEdge(p, type, graph);

            // added pokemon to pokemon list
            // 0 is speed because it have no use in CL_pokemon class
            l.add(new CL_Pokemon(p,type,value,0,edgeData));
        }
        return l;
    }


    /**
     * Return how much agents we can add in game
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
}
