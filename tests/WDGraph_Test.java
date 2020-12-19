import api.*;
import gameClient.util.Point3D;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class WDGraph_Test {
    private static int _errors = 0, _tests = 0, _number_of_exception = 0;
    private static String _log = "";


    @Test
    @Order(3)
    public void Copy() {
        directed_weighted_graph g1 = new DWGraph_DS();
        for (int i = 0; i < 10; i++) {
            node_data vertex = new NodeData(i);
            g1.addNode(vertex);
        }
        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(g1);
        directed_weighted_graph g2 = ag.copy();
        g2.removeNode(0);
        g2.removeNode(1);
        g2.removeNode(2);
        g1.removeNode(5);
        assertFalse(g1.equals(g2));

        g2.removeNode(5);
        g2.addNode(new NodeData(0));
        g2.addNode(new NodeData(1));
        g2.addNode(new NodeData(2));

        assertEquals(g1, g2);
    }

    @Test
    @Order(4)
    public void IsConnected() {
        directed_weighted_graph graph = new DWGraph_DS();
        for (int i = 0; i < 20; i++) {
            graph.addNode(new NodeData(i));
        }
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                graph.connect(i, j, 0.0);
            }
        }
        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(graph);
        assertTrue(ag.isConnected());

        directed_weighted_graph notCON = new DWGraph_DS();
        for (int i = 0; i < 25; i++) {
            notCON.addNode(new NodeData(i));
        }
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 10; j++) {
                notCON.connect(i, j, 0.0);
            }
        }
        ag.init(notCON);
        assertFalse(ag.isConnected());
    }

    @Test
    @Order(5)
    public void isConnected() {
        directed_weighted_graph graph = new DWGraph_DS();
        for (int i = 0; i < 10; i++) {
            graph.addNode(new NodeData(i));
        }
        graph.connect(0, 1, 0.0);
        graph.connect(0, 2, 0.0);
        graph.connect(0, 3, 0.0);
        graph.connect(0, 4, 0.0);
        graph.connect(0, 5, 0.0);
        graph.connect(0, 6, 0.0);
        graph.connect(0, 7, 0.0);
        graph.connect(0, 8, 0.0);
        graph.connect(0, 9, 0.0);
        graph.connect(1, 2, 0.0);
        graph.connect(2, 3, 0.0);
        graph.connect(3, 4, 0.0);
        graph.connect(4, 5, 0.0);
        graph.connect(5, 6, 0.0);
        graph.connect(6, 7, 0.0);
        graph.connect(7, 8, 0.0);
        graph.connect(8, 9, 0.0);
        graph.connect(9, 1, 0.0);
        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(graph);
        assertFalse(ag.isConnected());

    }


    @Test
    @Order(6)
    public void IsConnected_RunTime() {
        long start = new Date().getTime();
        directed_weighted_graph graph = new DWGraph_DS();
        int j = 0;
        int f = 0;
        for (int i = 0; i < 1000000; i++) {
            graph.addNode(new NodeData(i));
            graph.connect(i, i - 2, 0.0);
            graph.connect(i, i - 1, 0.0);
            graph.connect(i, i - 3, 0.0);
            graph.connect(i, j, 0.0);
            graph.connect(i, j + 1, 0.0);
            graph.connect(i, j + 2, 0.0);
            graph.connect(i, f, 0.0);
            graph.connect(f, i, 0.0);
            j++;
        }
        j = 0;
        for (int i = 1000000; i > 0; i--) {
            graph.connect(i, i - 1, 0.0);
            graph.connect(i, i - 2, 0.0);
            graph.connect(i, i - 3, 0.0);
            graph.connect(i, j, 0.0);
            graph.connect(i, j + 1, 0.0);
            graph.connect(i, j + 2, 0.0);
            j++;
        }

        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(graph);
        assertEquals(true, ag.isConnected());
        long end = new Date().getTime();
        double dt = (end - start) / 1000.0;
        boolean t = dt < 20;
        test("runtime test: ", t, true);
    }

    @Test
    @Order(7)
    public void Copy_RunTime() {
        long start = new Date().getTime();
        directed_weighted_graph graph = new DWGraph_DS();
        int j = 0;
        int f = 0;
        for (int i = 0; i < 1000000; i++) {
            graph.addNode(new NodeData(i));
            graph.connect(i, i - 2, 0.0);
            graph.connect(i, i - 1, 0.0);
            graph.connect(i, i - 3, 0.0);
            graph.connect(i, j, 0.0);
            graph.connect(i, j + 1, 0.0);
            graph.connect(i, j + 2, 0.0);
            graph.connect(i, f, 0.0);
            graph.connect(f, i, 0.0);
            j++;
        }
        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(graph);
        directed_weighted_graph copy = ag.copy();
        long end = new Date().getTime();
        double dt = (end - start) / 1000.0;
        boolean t = dt < 10;
        test("runtime test: ", t, true);
    }

    @Test
    @Order(8)
    public void ShortList() {
        directed_weighted_graph g1 = new DWGraph_DS();
        for (int i = 0; i <= 5; i++) {
            g1.addNode(new NodeData(i));
        }
        g1.connect(0, 1, 8.33);
        g1.connect(1, 3, 2.1);
        g1.connect(3, 4, 4.9);
        g1.connect(0, 2, 4.2);
        g1.connect(0, 3, 3.3);
        g1.connect(3, 5, 15.7);
        g1.connect(0, 5, 100.4);
        g1.connect(4, 5, 1.4);
        g1.connect(5, 4, 13.4);
        g1.connect(1, 4, 17.4);
        g1.connect(2, 5, 11.5);
        g1.connect(5, 0, 0.5);
        g1.connect(3, 1, 7.5);
        g1.connect(3, 2, 1.96);
        g1.connect(2, 0, 13.7);
        g1.connect(4, 3, 23.7);
        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(g1);
        List<node_data> path1 = new LinkedList<node_data>();
        path1.add(new NodeData(0));
        path1.add(new NodeData(3));
        path1.add(new NodeData(4));
        path1.add(new NodeData(5));
        assertEquals(path1, ag.shortestPath(0, 5));
        List<node_data> path2 = new LinkedList<node_data>();
        path2.add(new NodeData(4));
        path2.add(new NodeData(5));
        path2.add(new NodeData(0));
        path2.add(new NodeData(3));
        assertEquals(path2, ag.shortestPath(4, 3));
        List<node_data> path3 = new LinkedList<node_data>();
        path3.add(new NodeData(1));
        path3.add(new NodeData(4));
        assertNotEquals(path3, ag.shortestPath(1, 4));
        List<node_data> path4 = new LinkedList<node_data>();
        path4.add(new NodeData(2));
        path4.add(new NodeData(5));
        path4.add(new NodeData(0));
        path4.add(new NodeData(3));
        assertEquals(path4, ag.shortestPath(2, 3));

    }
    @Test
    @Order(9)
    public void ShortWeight() {
        directed_weighted_graph g1 = new DWGraph_DS();
        for (int i = 0; i <= 5; i++) {
            g1.addNode(new NodeData(i));
        }
        g1.connect(0, 1, 8.33);
        g1.connect(1, 3, 2.1);
        g1.connect(3, 4, 4.9);
        g1.connect(0, 2, 4.2);
        g1.connect(0, 3, 3.3);
        g1.connect(3, 5, 15.7);
        g1.connect(0, 5, 100.4);
        g1.connect(4, 5, 1.4);
        g1.connect(5, 4, 13.4);
        g1.connect(1, 4, 17.4);
        g1.connect(2, 5, 11.5);
        g1.connect(5, 0, 0.5);
        g1.connect(3, 1, 7.5);
        g1.connect(3, 2, 1.96);
        g1.connect(2, 0, 13.7);
        g1.connect(4, 3, 3.7);
        dw_graph_algorithms ag = new DWGraph_Algo();
        ag.init(g1);
        assertEquals(9.6, ag.shortestPathDist(0, 5));
        assertEquals(0.0, ag.shortestPathDist(2, 2));
        assertEquals(1.96, ag.shortestPathDist(3, 2));
    }

    @Test
    @Order(1)
    public void save() {
        directed_weighted_graph graph = new DWGraph_DS();
        for (int i = 0; i < 10; i++) {
            graph.addNode(new NodeData(i));
        }
        graph.connect(0, 1, 1.1);
        graph.connect(2, 3, 4.4);
        dw_graph_algorithms aGraph = new DWGraph_Algo();
        aGraph.init(graph);
        String g = "graphCheck";
        assertTrue(aGraph.save(g));
    }

    @Test
    @Order(2)
    public void load() {
        dw_graph_algorithms aGraph = new DWGraph_Algo();
        assertTrue(aGraph.load("graphCheck"));
    }

    @Test
    @Order(10)
    public void eq() {
        directed_weighted_graph g1 = new DWGraph_DS();
        g1.addNode(new NodeData(0));
        g1.addNode(new NodeData(1));
        g1.addNode(new NodeData(2));
        g1.addNode(new NodeData(3));
        g1.addNode(new NodeData(4));
        g1.connect(0, 1, 2);
        directed_weighted_graph g2 = new DWGraph_DS();
        g2.addNode(new NodeData(0));
        g2.addNode(new NodeData(1));
        g2.addNode(new NodeData(2));
        g2.addNode(new NodeData(3));
        g2.addNode(new NodeData(4));
        g2.connect(0, 1, 2);


        assertEquals(g1, g2);

    }

    @Test
    @Order(11)
    public void A_Bit_FromAll() {
        directed_weighted_graph graph = new DWGraph_DS();
        for (int i = 1; i < 50; i++) {
            graph.addNode(new NodeData(i));
        }
        node_data ver = new NodeData(0);
        Point3D P = new Point3D(1, 2, 3);
        ver.setLocation(P);
        graph.addNode(ver);
        Connect(graph);
        //Path N.1
        graph.connect(0, 1, 3.75);
        graph.connect(0, 2, 1.75);
        graph.connect(1, 3, 5.75);
        graph.connect(1, 4, 2.75);
        graph.connect(2, 5, 6.75);
        graph.connect(3, 4, 9.75);
        graph.connect(0, 5, 13.75);
        //Path N.2
        graph.connect(12, 13, 1.75);
        graph.connect(12, 14, 4.75);
        graph.connect(13, 14, 2.75);
        graph.connect(13, 15, 2.75);
        graph.connect(14, 15, 6.75);
        graph.connect(14, 16, 3.75);
        graph.connect(15, 16, 0.75);
        graph.connect(12, 16, 13.75);


        dw_graph_algorithms algo = new DWGraph_Algo();
        algo.init(graph);
        String forSave = "Graph";
        algo.save(forSave);
        directed_weighted_graph graphV2 = algo.copy();
        dw_graph_algorithms algoV2 = new DWGraph_Algo();
        algoV2.init(graphV2);
        algoV2.save("GraphV2");
        algoV2.load("Graph");
        assertEquals(algoV2.load("Graph"), algo.load("GraphV2"));

        assertEquals(algoV2.getGraph(), graph);
        assertTrue(algoV2.isConnected() & algo.isConnected());

        assertEquals(8.5, algo.shortestPathDist(0, 5));
        assertEquals(5.25, algo.shortestPathDist(12, 16));

        List<node_data> path1 = new LinkedList<node_data>();
        path1.add(ver);
        path1.add(new NodeData(2));
        path1.add(new NodeData(5));
        List<node_data> path2 = new LinkedList<node_data>();
        path2.add(new NodeData(12));
        path2.add(new NodeData(13));
        path2.add(new NodeData(15));
        path2.add(new NodeData(16));
        assertEquals(path1, algoV2.shortestPath(0, 5));
        assertEquals(path2, algoV2.shortestPath(12, 16));

    }

    @Test
    @Order(12)
    public void rmNode() {
        directed_weighted_graph graph = new DWGraph_DS();
        for (int i = 0; i < 1000; i++) {
            graph.addNode(new NodeData(i));
        }
        Connect(graph);
        graph.removeNode(0);
        for (int i = 0; i < 1000; i++) {
            graph.removeNode(i);
        }
    }

    private void Connect(directed_weighted_graph graph) {
        for (int i = 0; i < graph.nodeSize(); i++) {
            for (int j = 0; j < graph.nodeSize(); j++) {
                graph.connect(i, j, 12.5);
            }
        }
    }

    private static void test(String test, boolean val, boolean req) {
        test(test, "" + val, "" + req);
    }

    private static void test(String test, int val, int req) {
        test(test, "" + val, "" + req);
    }

    private static void test(String test, String val, String req) {
        boolean ans = true;
        ans = val.equals(req);
        String tt = _tests + ") " + test + "  pass: " + ans;
        _log += "\n" + tt;
        if (!ans) {
            _errors++;
            String err = "  ERROR(" + _errors + ") " + val + "!=" + req;
            System.err.println(tt + err);
            _log += err;

        }
    }

}
