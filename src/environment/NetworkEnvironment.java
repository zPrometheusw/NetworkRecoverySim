package environment;

import unalcol.agents.*;
import unalcol.agents.simulate.*;
import java.util.Vector;
import edu.uci.ics.jung.graph.*;
import graphutil.GraphCreator;
import graphutil.MyVertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import networkrecoverysim.SimulationParameters;
import serialization.StringSerializer;
import staticagents.NetworkNodeMessageBuffer;
import staticagents.Node;
import staticagents.NodeFailingProgram;
import unalcol.agents.simulate.util.SimpleLanguage;
import util.AtomicDouble;

public abstract class NetworkEnvironment extends Environment {

    protected static SimpleLanguage nodeLanguage;
    protected final Map<String, Node> nodes; // Map of name and vs nodes
    protected final int[][] adyacenceMatrix; //network as a adyacence matrix   
    protected final int[][] initialAdyacenceMatrix; //network as a adyacence matrix   
    protected final Semaphore available = new Semaphore(1);
    private final ArrayList<Double> nodeAverageLife;
    protected final Map<String, Integer> nodeVersion; // Map of node vs current version
    private final AtomicDouble totalMsgSent; //number of messages sent
    private final AtomicDouble totalMsgRecv; //number of messages received
    private final AtomicDouble totalSizeMsgSent; //number of messages sent
    private final AtomicDouble totalSizeMsgRecv; //number of messages received
    private final AtomicDouble totalMemory; //amount of memory
    private final AtomicLong simulationTime = new AtomicLong(0); // Counter of time
    private final long startingTime; // Starting time

    public int[][] getInitialAdyacenceMatrix() {
        return initialAdyacenceMatrix;
    }
    protected HashMap<String, Integer> nametoAdyLocation = new HashMap<>();  //dictionary of vertexname: id
    protected HashMap<Integer, String> locationtoVertexName = new HashMap<>(); // dictionary of id: vertexname
    private final AtomicInteger round = new AtomicInteger(0);

    private final AtomicInteger numberFailures = new AtomicInteger(0);

    private HashMap<String, Long> networkDelays; //used to administrate delays in messages

    /**
     *
     * @param _agents
     * @param _language
     * @param gr
     */
    public NetworkEnvironment(Vector<Agent> _agents, SimpleLanguage _nlanguage, Graph gr) {
        super(_agents);
        TopologySingleton.getInstance().init(gr);

        int size = gr.getVertexCount();
        adyacenceMatrix = new int[size][size];
        initialAdyacenceMatrix = new int[size][size];
        //mapVertex =  new ConcurrentHashMap<>();T

        ArrayList<MyVertex> av = new ArrayList<>(gr.getVertices());
        Collections.sort(av);
        Iterator<MyVertex> it = av.iterator();

        int i = 0;
        while (it.hasNext()) {
            MyVertex va = it.next();
            nametoAdyLocation.put(va.getName(), i);
            locationtoVertexName.put(i, va.getName());
            i++;
        }

        for (int k = 0; k < adyacenceMatrix.length; k++) {
            for (int l = 0; l < adyacenceMatrix.length; l++) {
                adyacenceMatrix[k][l] = 0;
                initialAdyacenceMatrix[k][l] = 0;
            }
        }

        it = gr.getVertices().iterator();
        while (it.hasNext()) {
            MyVertex va = it.next();
            ArrayList<MyVertex> na = new ArrayList<>(gr.getNeighbors(va));
            Iterator<MyVertex> itn = na.iterator();
            while (itn.hasNext()) {
                int k = nametoAdyLocation.get(va.getName());
                int l = nametoAdyLocation.get(itn.next().getName());
                adyacenceMatrix[k][l] = 1;
                adyacenceMatrix[l][k] = 1;
                initialAdyacenceMatrix[k][l] = 1;
                initialAdyacenceMatrix[l][k] = 1;
            }
        }
        nodes = Collections.synchronizedMap(new HashMap<>());
        nodeVersion = Collections.synchronizedMap(new HashMap<>());
        nodeLanguage = _nlanguage;
        nodeAverageLife = new ArrayList<>();
        totalMsgRecv = new AtomicDouble();
        totalSizeMsgRecv = new AtomicDouble();
        totalSizeMsgSent = new AtomicDouble();
        totalMsgSent = new AtomicDouble();
        startingTime = System.currentTimeMillis();
        totalMemory = new AtomicDouble();
    }

    /**
     * Find a vertex in a network. Return null if a node is nor alive
     *
     * @param nodename
     * @return
     */
    MyVertex findVertex(String nodename) {
        synchronized (nodes) {
            if (nodes.containsKey(nodename)) {
                Node n = nodes.get(nodename);
                if (n != null && !n.getVertex().getStatus().equals("failed")) {
                    return n.getVertex();
                }
            }
        }
        return null;
    }

    public HashMap<Integer, String> getLocationtoVertexName() {
        return locationtoVertexName;
    }

    public int[][] getAdyacenceMatrix() {
        return adyacenceMatrix;
    }

    public HashMap<String, Integer> getNametoAdyLocation() {
        return nametoAdyLocation;
    }

    /**
     * Connect two nodes
     *
     * @param vertex current node
     * @param vertexToConnect reference to node
     */
    public void connect(MyVertex vertex, String vertexToConnect) {
        synchronized (TopologySingleton.getInstance()) {
            try {
                MyVertex vertexTo = findVertex(vertexToConnect);
                if (vertexTo != null && getTopology().containsVertex(vertex) && getTopology().containsVertex(vertexTo) && !getTopology().isNeighbor(vertex, vertexTo)) {
                    adyacenceMatrix[nametoAdyLocation.get(vertex.getName())][nametoAdyLocation.get(vertexToConnect)] = 1;
                    adyacenceMatrix[nametoAdyLocation.get(vertexToConnect)][nametoAdyLocation.get(vertex.getName())] = 1;
                    String edgeName = "e" + vertex.getName() + vertexTo.getName();
                    getTopology().removeEdge(edgeName);
                    getTopology().addEdge(edgeName, vertex, vertexTo);
                }
                if (vertexTo == null) {
                    System.out.println(vertex.getName() + "connect: cannot connect with " + vertexToConnect + " is not in graph, findVertex return null.");
                }
            } catch (Exception ex) {
                System.out.println("Error trying to connect nodes: " + ex.getMessage());
            }
        }
    }

    /**
     * Add a connection between a vertex and a node
     *
     * @param dvertex vertex to connect
     * @param n node to connect
     */
    void addConnection(MyVertex dvertex, Node n) {
        try {
            synchronized (TopologySingleton.getInstance()) {
                if (getTopology().containsVertex(dvertex) && getTopology().containsVertex(n.getVertex()) && !getTopology().isNeighbor(dvertex, n.getVertex())) {
                    adyacenceMatrix[nametoAdyLocation.get(n.getVertex().getName())][nametoAdyLocation.get(dvertex.getName())] = 1;
                    adyacenceMatrix[nametoAdyLocation.get(dvertex.getName())][nametoAdyLocation.get(n.getVertex().getName())] = 1;
                    String edgeName = "e" + dvertex.getName() + n.getVertex().getName();
                    getTopology().removeEdge(edgeName);
                    getTopology().addEdge(edgeName, dvertex, n.getVertex());
                }
            }
        } catch (Exception ex) {
            System.out.println("Trying to connect " + dvertex + " with node " + n.getName() + " failed " + ", exception" + ex.getMessage());
        }
    }

    /**
     * Get list of ids of neighbours of node
     *
     * @param node
     * @return list of id
     */
    public List<String> getTopologyNames(MyVertex node) {
        List<String> names = new ArrayList();
        for (int i = 0; i < adyacenceMatrix.length; i++) {
            if (adyacenceMatrix[nametoAdyLocation.get(node.getName())][i] == 1) {
                names.add(locationtoVertexName.get(i));
            }
        }
        return names;
    }

    /**
     * Get a node given its id
     *
     * @param name
     * @return
     */
    Node getNode(String name) {
        synchronized (nodes) {
            if (nodes.containsKey(name)) {
                Node n = nodes.get(name);
//            System.out.println("Node" + n + "yaaaaaaaaaaaay");
                if (!n.getVertex().getStatus().equals("failed")) {
                    return n;
                }
            }
        }
        return null;
    }

    /**
     * Obtain minimum id from a list of neighbours additionally evaluate if node
     * have data about the node to create.
     *
     * @param currentNode current node
     * @param neigdiff array with neighbourhood of difference
     * @param nodeMissing id of the missing node
     * @return id of the minimum
     */
    public String getMinimumId(List<String> neigdiff, String nodeMissing, Node currentNode) {
        List<String> nodesAlive = new ArrayList();
        increaseTotalSizeMsgSent(neigdiff.size() * 56.0); //56 bytes is the size of a ping         
        
        
        double cont = 0;
        for(String s: neigdiff) {
            Node n = getNode(s);
            if (n != null && n.getNetworkdata().containsKey(nodeMissing)) {
                nodesAlive.add(s);
                cont += 56.0;
                //increaseTotalSizeMsgRecv(56.0);
            }
        }
        
        increaseTotalSizeMsgRecv(cont);
        //System.out.println("inc sent" + (neigdiff.size() * 56.0)  +  "recv count" + cont);

        if (!nodesAlive.isEmpty() && nodesAlive.get(0).contains("p")) {
            String min = Collections.min(nodesAlive, (o1, o2)
                    -> Integer.valueOf(o1.substring(1)).compareTo(Integer.valueOf(o2.substring(1))));
            return min;
        } else {
            String min = Collections.min(nodesAlive, (o1, o2)
                    -> Integer.valueOf(o1).compareTo(Integer.valueOf(o2)));
            return min;
        }
    }

    /**
     * Creation of a node with name d
     *
     * @param creator
     * @param nodeId
     * @param neigdiff
     */
    public void createNewNode(Node creator, String nodeId, List<String> neigdiff) {
        synchronized (TopologySingleton.getInstance()) {
            System.out.println("env round:" + this.getAge() + ", node " + creator + " is creating new node " + nodeId);
            MyVertex dvertex = findVertex(nodeId);
            if (dvertex != null) { //can ping node and avoid creation         
                if (getTopology().containsVertex(dvertex) && !getTopology().isNeighbor(dvertex, creator.getVertex())) {
                    System.out.println("Node " + nodeId + " is alive connecting instead create...[" + dvertex + ", " + creator.getVertex().getName() + "]");
                    addConnection(dvertex, creator);
                }

            } else {

                GraphCreator.VertexFactory vertexFactory = new GraphCreator.VertexFactory();
                MyVertex newVertex = vertexFactory.create();
                newVertex.setName(nodeId);
                newVertex.setStatus("alive");
                synchronized (TopologySingleton.getInstance()) {
                    getTopology().addVertex(newVertex);
                }
                addConnection(newVertex, creator);
                NodeFailingProgram np;

                if (SimulationParameters.failureProfile.equals("backtolowpf")) { //after fail node will not fail again
                    np = new NodeFailingProgram(0); // set node pf = 0
                } else if (SimulationParameters.failureProfile.contains("backtolowpf")) { //after some round number ####, backtolowpf#### sets node pf in zero
                    double rNoFail = Double.parseDouble(SimulationParameters.failureProfile.replaceAll("backtolowpf", ""));
                    if (this.getAge() >= rNoFail) {
                        np = new NodeFailingProgram(0); //using this option reduces failure prob to zero
                    } else {
                        np = new NodeFailingProgram((float) SimulationParameters.npf);
                    }
                } else {
                    np = new NodeFailingProgram((float) SimulationParameters.npf);
                }

                //NetworkNodeMessageBuffer.getInstance().createBuffer(d);
                Node nod;
                nod = new Node(np, newVertex);
                nod.setVertex(newVertex);
                nod.setArchitecture(this);

                StringSerializer serializer = new StringSerializer();
                String aprox = serializer.serialize(creator.getNetworkdata()); //not sure about this!

                increaseTotalSizeMsgSent(aprox.length());
                increaseTotalSizeMsgRecv(aprox.length());
                nod.setNetworkdata(new HashMap(creator.getNetworkdata()));

                synchronized (nodeVersion) {
                    nodeVersion.put(nodeId, nodeVersion.get(nodeId) + 1);
                }

                synchronized (nodes) {
                    nodes.put(nod.getName(), nod);
                }

                //Send message to node neigbours.
                //can be no nd but all agentData                
                if (neigdiff != null && !neigdiff.isEmpty()) {
                    neigdiff.forEach((neig) -> {
                        if (!neig.equals(creator.getName())) {
                            //message msgnodediff: connect|nodeid|nodetoconnect
                            //System.out.println(n.getVertex().getName() + "is sending diff " + dif + "to" + neig);
                            String[] msgconnect = new String[3];
                            msgconnect[0] = "connect";
                            msgconnect[1] = creator.getName();
                            msgconnect[2] = nodeId;
                            double msgConnectSize = getMessageSize(msgconnect);
                            increaseTotalSizeMsgSent(msgConnectSize);
                            NetworkNodeMessageBuffer.getInstance().putMessage(neig, msgconnect);
                        }
                        //n.getPending().get(dif.toString()).add(neig);
                    });
                }

                this.agents.add(nod);
                nod.live();
                Thread t = new Thread(nod);
                nod.setThread(t);
                t.start();
            }
            setChanged();
            notifyObservers();
        }

    }

    /**
     * Remove a vertex
     *
     * @param vertex
     * @return
     */
    public boolean removeVertex(MyVertex vertex) {
        synchronized (TopologySingleton.getInstance()) {
            //copy to avoid concurrent modification in removeEdge
            for (int i = 0; i < adyacenceMatrix.length; i++) {
                adyacenceMatrix[nametoAdyLocation.get(vertex.getName())][i] = 0;
                adyacenceMatrix[i][nametoAdyLocation.get(vertex.getName())] = 0;
            }
            try {
                if (!getTopology().removeVertex(vertex)) {
                    System.out.println("cannot remove vertex " + vertex);
                }
            } catch (Exception ex) {
                System.out.println("removeVertex: Error trying to remove vertex" + ex.getMessage());
            }
            return true;
        }
    }

    /**
     * Kill process of node n
     *
     * @param n
     */
    public void KillNode(Node n) {
        synchronized (TopologySingleton.getInstance()) {
            //Add node life to node average life structure  
            nodeAverageLife.add((double) n.getRounds());
            setChanged();
            notifyObservers(n);
           
            synchronized (nodes) {
                if (nodes.containsKey(n.getName())) { //remove from concurrent structure
                    nodes.remove(n.getName());
                    System.out.println("removed: " + n.getName());
                }
                n.getVertex().setStatus("failed"); //set status to failed
            }
            System.out.println("Node " + n.getVertex().getName() + " has failed.");
            //clear buffer of node n
            removeVertex(n.getVertex());
            n.die();
        }

        //after kill node thread deleteBuffer        
        //NetworkNodeMessageBuffer.getInstance().deleteBuffer(n.getVertex().getName());
        numberFailures.incrementAndGet();

    }

    @Override
    public abstract boolean act(Agent agent, Action action);

    @Override
    public Percept sense(Agent agent) {
        Percept p = new Percept();
        return p;
    }

    @Override
    public void init(Agent agent) {
        //@TODO: Any special initialization processs of the environment
    }

    /**
     * @return the topology
     */
    public Graph<MyVertex, String> getTopology() {
        return TopologySingleton.getInstance().getTopology();
    }

    public synchronized void updateWorldAge() {
        round.incrementAndGet();
        setChanged();
        notifyObservers();
    }

    /**
     * @return simulation time in milliseconds
     */
    public synchronized Long updateAndGetSimulationTime() {
        long currentTime = System.currentTimeMillis();
        long delta = currentTime - startingTime;
        simulationTime.set(delta);
        return simulationTime.get();
    }

    /**
     * @return simulation time in milliseconds
     */
    public long getSimulationTime() {
        return simulationTime.get();
    }

    /**
     * @return the age
     */
    public int getAge() {
        return round.get();
    }

    public int getNodesAlive() {
        synchronized (nodes) {
            return nodes.size();
        }
    }

    public HashMap<String, Long> getNetworkDelays() {
        return networkDelays;
    }

    public void setNetworkDelays(HashMap<String, Long> in) {
        networkDelays = in;
    }

    public abstract boolean isOccuped(MyVertex v);

    public List<Node> getNodes() {
        synchronized (nodes) {
            return new ArrayList<>(nodes.values());
        }
    }

    @Override
    public Vector<Action> actions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void addNodes(List<Node> lnodes) {
        synchronized (nodes) {
            for (Node n : lnodes) {
                nodes.put(n.getName(), n);
            }
        }
    }

    public void sChAndNot() {
        setChanged();
        notifyObservers();
    }

    /**
     * Return number of failures recovered
     *
     * @return
     */
    public AtomicInteger getNumberFailures() {
        return numberFailures;
    }

    /**
     * @return arrayList with average life of a node
     */
    public ArrayList<Double> getNodeAverageLife() {
        return nodeAverageLife;
    }

    /**
     * Init nodes version. Run before starting node threads by this reason it is
     * not synchronised.
     */
    public void initNodesVersion() {
        getNodes().forEach((n) -> {
            nodeVersion.put(n.getName(), 1);
        });
    }

    /**
     * @return total number of messages sent
     */
    public double getTotalMsgSent() {
        return totalMsgSent.getAndAdd(0.0);
    }

    /**
     * Total number of messages received
     *
     * @return
     */
    public double getTotalMsgRecv() {
        return totalMsgRecv.getAndAdd(0.0);
    }

    /**
     *
     * @return total size of messages received
     */
    public double getTotalSizeMsgRecv() {
        return totalSizeMsgRecv.getAndAdd(0.0);
    }

    /**
     * @return total size of messages sent
     */
    public double getTotalSizeMsgSent() {

        return totalSizeMsgSent.getAndAdd(0.0);
    }

    /**
     * Increase total size messages sent
     *
     * @param delta
     */
    public void increaseTotalSizeMsgSent(double delta) {
        synchronized (totalMsgSent) {
            totalMsgSent.getAndAdd(1.0);
        }
        synchronized (totalSizeMsgSent) {
            totalSizeMsgSent.getAndAdd(delta);
        }
    }

    /**
     * Increase total size of messages received
     *
     * @param delta
     */
    public void increaseTotalSizeMsgRecv(double delta) {
        synchronized (totalMsgRecv) {
            totalMsgRecv.getAndAdd(1.0);
        }
        synchronized (totalSizeMsgRecv) {
            totalSizeMsgRecv.getAndAdd(delta);
        }
    }

    /**
     * Increase memory consumed
     *
     * @param delta
     */
    public void increaseTotalMemory(double delta) {
        synchronized (totalMemory) {
            totalMsgRecv.getAndAdd(delta);
        }
    }

    /**
     * get memory consumed
     *
     * @return total memory consumption
     */
    public double totalMemoryConsumed(double delta) {
        synchronized (totalMemory) {
            return totalMemory.getAndAdd(0.0);
        }
    }

    /**
     * @param n
     * @return
     */
    public int getNodeVersion(Node n) {
        synchronized (nodeVersion) {
            return nodeVersion.get(n.getName());
        }
    }

    /**
     * Given a message return an approximation of its size in bytes
     *
     * @param msg a determined message
     * @return approximation of size in bytes
     */
    public double getMessageSize(String[] msg) {
        int size = 0;
        for (String m : msg) {
            size += m.length();
        }
        return size;
    }

}
