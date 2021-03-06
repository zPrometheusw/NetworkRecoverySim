/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphcreation;


import graphutil.GraphGenerator;
import graphcreation.HubAndSpokeGraphGenerator;
import graphcreation.WattsBetaSmallWorldGenerator;
import graphcreation.LongHubAndSpokeGraphGenerator;
import graphcreation.CommunitySpokeGraphGenerator;
import graphcreation.CircleLongHubAndSpokeGraphGenerator;
import edu.uci.ics.jung.algorithms.generators.Lattice2DGenerator;
import edu.uci.ics.jung.algorithms.generators.random.BarabasiAlbertGenerator;
import edu.uci.ics.jung.algorithms.generators.random.EppsteinPowerLawGenerator;
import edu.uci.ics.jung.algorithms.generators.random.KleinbergSmallWorldGenerator;
import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.Graph;
import graphutil.GraphCreator;
import graphutil.MyVertex;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Arles Rodriguez
 */
public class graphSimpleFactorySave {

    private static void connectSeparateGraph(Graph<MyVertex, String> graph, GraphCreator.EdgeFactory e) {
        double sum = 0;
        double n = graph.getVertexCount();
        //Transformer<MyVertex, Double> distances = DistanceStatistics.averageDistances(graph, new UnweightedShortestPath<>(graph));
        UnweightedShortestPath u = new UnweightedShortestPath(graph);

        for (MyVertex v : graph.getVertices()) {
            for (MyVertex w : graph.getVertices()) {
                if (!w.equals(v)) {
                    //System.out.println("<" + w + "," + v + ">" + u.getDistance(v, w).doubleValue());
                    //if(distances.containsKey(v+"-"+w)){
                    //   sum += distances.get(v+"-"+w);
                    /*}else{*/
                    if (u.getDistance(v, w) != null) {
                        double distance = u.getDistance(v, w).doubleValue();
                        sum += distance;
                    } else {
                        System.out.println("Graph is not connected now!");
                        graph.addEdge(e.create(), v, w);
                    }
                    /* distances.put(v+"-"+w, distance);
                    }*/
                }
            }
        }
    }

    public static Graph<MyVertex, String> createGraph(String topology) {
        //Network creation
        //GraphCreator.VertexFactory v =  new GraphCreator.VertexFactory(languaje, ap, dataset);
        //temp new
        Graph<MyVertex, String> g = null;
        int seed = (int) (Math.random() * 10000);

        GraphCreator.VertexFactory v = new GraphCreator.VertexFactory();

        GraphCreator.EdgeFactory e = new GraphCreator.EdgeFactory();

        switch (topology) {
            case "scalefree":
                Set<MyVertex> seedSet = new HashSet<>();
                //chgne
                BarabasiAlbertGenerator bag = new BarabasiAlbertGenerator<>(new GraphCreator.GraphFactory(), v, e, GraphGenerator.startNodesScaleFree, GraphGenerator.edgesToAttachScaleFree, seed, seedSet);
                bag.evolveGraph(GraphGenerator.numSteps - 1);
                g = bag.create();
                connectSeparateGraph(g, e);
                break;
            case "smallworld":
                g = new WattsBetaSmallWorldGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, GraphGenerator.beta, GraphGenerator.degree, true).generateGraph();
                break;
            case "community":
                g = new CommunityNetworkGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, GraphGenerator.beta, GraphGenerator.degree, true, GraphGenerator.clusters).generateGraph();
                break;
            case "communitycircle":
                g = new CommunityCircleNetworkGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, GraphGenerator.beta, GraphGenerator.degree, true, GraphGenerator.clusters).generateGraph();
                break;
            case "kleinberg":
                g = new KleinbergSmallWorldGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, 0).create();
                break;
            case "line":
                g = new LineGraphGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, false).generateGraph();
                break;
            case "hubandspoke":
                g = new HubAndSpokeGraphGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, false).generateGraph();
                break;
            case "foresthubandspoke":
                g = new ForestHubAnsSpokeGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, GraphGenerator.clusters, false).generateGraph();
                break;
            case "circle":
                g = new LineGraphGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, true).generateGraph();
                break;
            case "lattice":
                g = new Lattice2DGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.rows, GraphGenerator.columns, false).create();
                //layout = new CircleLayout<>(g);
                break;
            case "longhubandspoke":
                g = new LongHubAndSpokeGraphGenerator(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, GraphGenerator.length, false).generateGraph();
                break; 
                
            case "circlelonghubandspoke":
                g = new CircleLongHubAndSpokeGraphGenerator<>(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.vertexNumber, GraphGenerator.length, false).generateGraph();
                break; 
            case "comunityspokegraph":
                g = CommunitySpokeGraphGenerator.createGraph(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.communitySrc, GraphGenerator.spokes, GraphGenerator.length);
                break;
            default:
                g = new EppsteinPowerLawGenerator<>(new GraphCreator.GraphFactory(), v, new GraphCreator.EdgeFactory(), GraphGenerator.rows, GraphGenerator.columns, 5).create();
                break;
        }

        System.out.println("creating g: " + g.toString() + " with method " + topology);
        return g;
    }
}
