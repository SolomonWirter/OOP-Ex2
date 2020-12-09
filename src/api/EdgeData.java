package api;

public class EdgeData implements edge_data {
    private int src;
    private int dest;
    private double weight;
    private String info;
    private int tag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeData edgeData = (EdgeData) o;
        return src == edgeData.src &&
                dest == edgeData.dest &&
                Double.compare(edgeData.weight, weight) == 0 &&
                info.equals(edgeData.info);
    }


    public EdgeData(int src, int dest, double weight){
        this.src = src;
        this.dest = dest;
        this.weight = weight;
        this.info = "";
    }
    public EdgeData(edge_data e){
        this.src = e.getSrc();
        this.dest = e.getDest();
        this.weight = e.getWeight();
        this.info = e.getInfo();
    }
    @Override
    public int getSrc() {
        return src;
    }

    @Override
    public int getDest() {
        return dest;
    }

    @Override
    public double getWeight() {
        return weight;
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
        return "EdgeData{" +
                "src=" + src +
                ", dest=" + dest +
                ", weight=" + weight +
                ", info='" + info + '\'' +
                ", tag=" + tag +
                '}';
    }
}
