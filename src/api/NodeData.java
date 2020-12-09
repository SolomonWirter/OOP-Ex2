package api;

import gameClient.util.Point3D;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NodeData implements node_data, Comparable<node_data> {
    private int key;

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    private int tag = -1;
    private String info;
    private geo_location p;
    private double weight;

    public NodeData(int key) {
        this.key = key;
        this.p = new Point3D(0, 0, 0);
    }

    public NodeData(int key, geo_location p) {
        this.key = key;
        this.p = p;
    }

    public NodeData(node_data n) {
        this.key = n.getKey();
        this.info = n.getInfo();
        this.p = n.getLocation();
        this.weight = n.getWeight();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeData nodeData = (NodeData) o;
        return key == nodeData.key &&
                Double.compare(nodeData.weight, weight) == 0 &&
                Objects.equals(info, nodeData.info) &&
                Objects.equals(p, nodeData.p);
    }


    @Override
    public int getKey() {
        return key;
    }

    @Override
    public geo_location getLocation() {
        return p;
    }

    @Override
    public void setLocation(geo_location p) {
        this.p = new Point3D(p.x(), p.y(), p.z());
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double w) {
        weight = w;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public void setInfo(String s) {
        info = s;
    }

    @Override
    public int getTag() {
        return tag;
    }

    @Override
    public void setTag(int t) {
        tag = t;
    }

    @Override
    public String toString() {
        return "Key:(" + key + ")";
    }

    @Override
    public int compareTo(@NotNull node_data o) {
        return this.getTag() - o.getTag();
    }

}
