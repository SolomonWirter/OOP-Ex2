# Ex2 Project – weighted & directed Graph. - Welcome !

>**This Project represent 2 main Packages**

**Package api:**

>Contain 5 Classes:

**_Class NodeData - implements node_data_**

>The Vertex in the Graph is a Node that Connect between Paths
>*Has 2 main fields `int key`, `geo_location p`.*

>Contain Copy Constructor and Also receiving the above fields Constructor
> `NodeData(int key)`, `NodeData(int key, geo_location p)`.

* The Key field is "Set" only in Constructor. `getKey()` will return it
* The geo_location is Dot in the "Virtual Space" for Drawing the Graph using GUI
* `setLocation(geo_location p)` - for set a new location,  `getLocation()` - will return it


**_Class EdgeData - implements edge_data_**

>The Edge is Connecting between 2 Vertices with a Direction `V(Src) -> V(Dest)`
>*Has 3 main fields `int src`, `int dest`, `int weight`*

>Contain Copy Constructor and Also receiving the above fields Constructor

* This is a Simple Class that mostly represent the Edges of the Graph with `Weight , Direction`
* There are no Setters in this Class you can set only in the Constructor 
* There are Getters for each field above `getSrc()`, `getDest()`, `getWeight()`.

**_Class DWGraph_DS - implements directed_weighted_graph._**

>**Graph `V` – for Vertices `NodeData` , `E` – for Edges `EdgeData` . -> each Edge contain a `Weight Value`**

<center> 
<h3> Image For Demonstration </h3>

  ![alt text](https://i.ibb.co/19PncRT/For-1.png)
</center> 

* The Graph is using `HashMap` Data Structure for storing all the Vertexes of the Graph & Key for quick access to each Vertex.
* Using also a Second `HashMap` for containing a list of Edges `EdgeData` for each Vertex `(+Weight)`.

**Info:**  - 'key' values are 'Integer'
>Contain Empty and Copy Constructors.

* For receiving/adding/remove a Vertex use `getNode(key)` -> node_info ; `addNode(node_data)` -> void ; `removeNode(key)` -> node_info.
* For checking/receiving/remove an Edge use `hasEdge(key1,key2)` -> boolean value ; `getEdge(key1,key2)` -> double value ; `removeEdge(key1,key2)` -> void.
* Collection<node_info> - `getV()` -> Collection of Vertices of the Graph ; `getE(node_id)` -> Collection of Edges that V(node_id) - Source.
* `nodeSize()` -> Integer ; `edgeSize()` -> Integer ; `getMC()` -> Integer - [Counter for Graph Modifications]



**_Class WGraph_Algo - implements weighted_graph_algorithms._**

* Checking the Graph above if it is Connected – (Every Vertex is reachable from Every Vertex).
* Returning the Shortest path from Vertex - A to Vertex – B, (A,B\ \in\ V).

**info:**

* For Applying any method on a graph first use `init(weighted_graph)` to initiate the Graph of the Class.
* IF you are not sure which Graph you working on use `getGraph()` -> weighted_graph.
* Can Also Copy the current Graph using `copy()`.  
* Also, save\load weighted_graph is possible `save(File Name)` ; `load(File Name)` -> Both Methods return boolean value for success or not. 
* For checking if the Graph is connected apply `isConnected()` -> boolean value.

  `isConnected()` - Use `BFS` - [Breath-First-Search Algorithm] -> Granting each Vertex a Tag Value.
  > Explanation: each Vertex receive a Value that represent the number of Vertices from the Source Vertex.
  > The Graph is Directional, so we will Apply BFS on Graph and also on Graph^T - Opposite Directions
  > The Graph isConnected if and only if Graph^T (Transpose) isConnected

* There are 2 Methods for receiving the Shortest Path -

* `shortestPathDist(src - Key, dest - Key)` - return double value - represent the shortest distance from src to dest (Weight of Edges) 
* `shortestPath(src - Key, dest - Key)` - return List - Vertex(src) -> Vertex(1) -> V -> V -> ... -> Vertex(dest) 
* Both Shortest Path Methods - Uses `Dijkstra Algorithm` -> Granting each Vertex a Tag Value (EdgeWeight Sum Form Source)-[Min_Sum] 

  >Explanation: Dijkstra Algorithm Works Similar to BFS instead of counting of many Vertices are form Source Vertex
  > The Dijkstra Calculate the weight of the edges from the Source Vertex - and return the Min Sum for each reachable Vertex.

<center> 
<h3> Illustration of Dijkstra </h3>

![alt text](https://upload.wikimedia.org/wikipedia/commons/5/57/Dijkstra_Animation.gif)

</center>

**_Class DWGraph_DS_Deserializer implements JsonDeserializer<directed_weighted_graph>_**

>**This Class receives a JSON String and Return Graph**

**Package gameClient:**

<center>

![alt text](https://i.ibb.co/nQS2Pr6/Game.png)

</center>

**This Package represents A Simple Game For Catching Pokemons on Graph Using Algorithms**

>**Classes `Range`, `Range2D`, `Range2Range`, `Point3D`**
> * For Implementing geo_location we use Point3D
> * To Use GUI we use the other 3 Classes for Frame , and Point3D for placing the geo_location in the Graphics.
 

>**Class `CL_Agent`**
> * The Agent need to catch the pokemons
> * Agent has speed, and we need to calculate the speed,edgeWeight and distance in geo_location for better performance.

<center> 

![alt text](https://i.ibb.co/jfCphSx/agent.png)

</center>

>**Class `CL_Pokemon`**
> * The Pokemons are respawn randomly on the Graph, and they're going to be there till the Agent will catch them.
> * Pokemons has different Values, The Algorithm decide the Agent which is the Ideal Pokemon for each Agent.
> * There are 2 type `1 , -1`, Src(key) > Dest(key) -> `-1`, Src(key) < Dest(key) -> `1`.

<center> 
<h4> TYPE 1 </h4>

![alt text](https://i.ibb.co/Bst465z/pokemon-Type1.png)

<h4> TYPE -1 </h4>

![alt text](https://i.ibb.co/8sbsVK8/pokemon-Type-Minus1.png)

</center> 

>**Class Arena**

* This Class is the Arena where it all happens, Update The Pokemons on the Edges `updateEdge(Pokemon, Graph)`
* Knows how to follow the Agents Location Using `getAgents(String, Graph)`
* The `String` is the Locations of the Agents -> Receiving it from the Game.

<h2>Class Ex2 -> Our Game Client</h2>

>**This Class where all the above Combine.**

* The Algorithm that send the Agents towards the Pokemons is here...
* Gets The Value of the Pokemon and the Edge Weight At first.
* The Highest ratio between the Value and the Weight is the Ideal Pokemon to be near it at Start Game.
* After that the Agents need to search for another prey 
* If there are more than 1 Pokemon on an Edge the Algorithim count them as one Pokemon with the sum Value
* Calculate the ShortestPath using Dijkstra and the Highest ratio is the Ideal Pokemon to go after.
* There will not be a situation when 2 Agents after the same pokemon 
* In case the Agent has no Pokemon to go after it will chase a random Pokemon
* In this case it can happen that 1 Pokemon has 2 Agents 


<center>

<h2> Enjoy ! </h2>

</center>