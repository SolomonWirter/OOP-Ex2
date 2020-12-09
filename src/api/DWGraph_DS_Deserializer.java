package api;

import com.google.gson.*;
import gameClient.util.Point3D;

import java.lang.reflect.Type;
import java.util.Iterator;

public class DWGraph_DS_Deserializer implements JsonDeserializer<directed_weighted_graph> {

    @Override
    public directed_weighted_graph deserialize(JsonElement jsonElement,
                                               Type type,
                                               JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        directed_weighted_graph graph = new DWGraph_DS();

        JsonArray nodes = jsonObject.getAsJsonArray("Nodes");

        Iterator<JsonElement> jsonElementIterator = nodes.iterator();
        while (jsonElementIterator.hasNext()) {

            JsonObject jsonVertex = jsonElementIterator.next().getAsJsonObject();

            int id = jsonVertex.get("id").getAsInt();
            String point = jsonVertex.get("pos").getAsString();
            String[] pointArray = point.split(",");

            double x = Double.parseDouble(pointArray[0]);
            double y = Double.parseDouble(pointArray[1]);
            double z = Double.parseDouble(pointArray[2]);

            geo_location p = new Point3D(x, y, z);
            node_data vertex = new NodeData(id, p);

            graph.addNode(vertex);
        }
        JsonArray edges = jsonObject.getAsJsonArray("Edges");

        Iterator<JsonElement> elementIterator = edges.iterator();
        while (elementIterator.hasNext()) {

            JsonObject jsonEdge = elementIterator.next().getAsJsonObject();

            int src = jsonEdge.get("src").getAsInt();
            double w = jsonEdge.get("w").getAsDouble();
            int dest = jsonEdge.get("dest").getAsInt();

            graph.connect(src, dest, w);
        }

        return graph;

    }


}

