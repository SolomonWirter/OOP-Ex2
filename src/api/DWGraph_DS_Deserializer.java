package api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;

public class DWGraph_DS_Deserializer implements JsonDeserializer<DWGraph_DS> {
    @Override
    public DWGraph_DS deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int EdgeSize = jsonObject.get("edgeSize").getAsInt();
        int MC = jsonObject.get("modeCount").getAsInt();
        //****graphV
        JsonElement jsonGraphV = jsonObject.get("graphV");
        Type graphVType = new TypeToken<HashMap<Integer,NodeData>>(){}.getType();
        HashMap<Integer,node_data> graphV = new Gson().fromJson(jsonGraphV, graphVType);
        //********graphV
        //*********graphEdges
        JsonElement jsonGraphEdges = jsonObject.get("graphEdges");
        Type graphEdgesType = new TypeToken<HashMap<Integer, HashMap<Integer,EdgeData>>>(){}.getType();
        HashMap<Integer, HashMap<Integer,edge_data>> graphEdges = new Gson().fromJson(jsonGraphEdges, graphEdgesType);
        //**************graphEdges
        //**************destEdges
        JsonElement jsonDestEdges = jsonObject.get("destEdges");
        Type destEdgesType = new TypeToken<HashMap<Integer, HashSet<Integer>>>(){}.getType();
        HashMap<Integer, HashSet<Integer>> destEdges = new Gson().fromJson(jsonDestEdges, destEdgesType);
        return new DWGraph_DS(EdgeSize,graphV,graphEdges,destEdges);
    }
}
