/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unalcol.agents.NetworkSim.util;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ----------------------
 * MessagesSent2.java
 * ----------------------
 * (C) Copyright 2003, 2004, by David Browning and Contributors.
 *
 * Original Author:  David Browning (for the Australian Institute of Marine Science);
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: MessagesSent2.java,v 1.12 2004/06/02 14:35:42 mungady Exp $
 *
 * Changes
 * -------
 * 21-Aug-2003 : Version 1, contributed by David Browning (for the Australian Institute of 
 *               Marine Science);
 * 27-Aug-2003 : Renamed BoxAndWhiskerCategoryDemo --> MessagesSent2, moved dataset creation
 *               into the demo (DG);
 *
 */
import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.Graph;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.Log;
import org.jfree.util.LogContext;
import unalcol.agents.NetworkSim.GraphElements;

/**
 * Demonstration of a box-and-whisker chart using a {@link CategoryPlot}.
 *
 * @author David Browning
 */
public class CompiledBoxPlotAgentIdandNode extends ApplicationFrame {

    private static String experimentsDir = ".";
    private static String[] aMode;
    private static int numberSeries = 0;
    private static int interval = 500;

    /**
     * Access to logging facilities.
     */
    private static final LogContext LOGGER = Log.createContext(CompiledBoxPlotAgentIdandNode.class);
    private static int dimensionX = 1600;
    private static int dimensionY = 800;

    private static String graphName = "";
    public HashMap<String, Double> graphMetric = new HashMap<>();

    private HashMap<String, Double> loadGraphMetric() {
        Graph<GraphElements.MyVertex, String> g = GraphSerialization.loadDeserializeGraph(graphName);
        BetweennessCentrality ranker = new BetweennessCentrality(g);
        ranker.step();
        ranker.setRemoveRankScoresOnFinalize(false);
        ranker.evaluate();
        HashMap<String, Double> map = new HashMap();
        int i = 1;
        for (GraphElements.MyVertex v : g.getVertices()) {
            Double rank = ranker.getVertexRankScore(v);
            Double normalized = rank / ((g.getEdgeCount() - 1) * (g.getEdgeCount() - 2) / 2);
            //map.put(v.toString(), (double)i++);//(double)g.getNeighborCount(v));
            map.put(v.toString(), (double) g.getNeighborCount(v));
        }
        return map;
    }

    /**
     * Creates a new demo.
     *
     * @param title the frame title.
     */
    public CompiledBoxPlotAgentIdandNode(final String title, ArrayList<Double> pf) {
        super(title);
        createSampleDataset(pf);
        /*final CategoryAxis xAxis = new CategoryAxis("");
        //final NumberAxis yAxis = new NumberAxis("Round number");
        final NumberAxis yAxis = new NumberAxis("");
        yAxis.setAutoRangeIncludesZero(false);
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
        renderer.setFillBox(false);
        renderer.setMeanVisible(false);
        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
        renderer.setFillBox(true);
        renderer.setSeriesPaint(0, Color.WHITE);
        renderer.setSeriesPaint(1, Color.LIGHT_GRAY);
        renderer.setSeriesOutlinePaint(0, Color.BLACK);
        renderer.setSeriesOutlinePaint(1, Color.BLACK);
        renderer.setUseOutlinePaintForWhiskers(true);
        Font legendFont = new Font("SansSerif", Font.PLAIN, 16);
        renderer.setLegendTextFont(0, legendFont);
        renderer.setLegendTextFont(1, legendFont);
        renderer.setMedianVisible(true);
        renderer.setMeanVisible(false);
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

        Font font = new Font("Dialog", Font.PLAIN, 12);
        xAxis.setTickLabelFont(font);
        yAxis.setTickLabelFont(font);
        yAxis.setLabelFont(font);
        xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        xAxis.setMaximumCategoryLabelLines(5);

        final JFreeChart chart = new JFreeChart(
                "Agent Number vs Round number" + getTitle(pf),
                new Font("SansSerif", Font.BOLD, 18),
                plot,
                true
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(dimensionX, dimensionY));
        setContentPane(chartPanel);

        TextTitle legendText = null;
        if (pf.size() == 1) {
            legendText = new TextTitle("Round Number");
        } else {
            legendText = new TextTitle("Round number - Probability of Failure");
        }

        legendText.setFont(font);
        legendText.setPosition(RectangleEdge.BOTTOM);
        chart.addSubtitle(legendText);
        chart.getLegend().setItemFont(font);

        FileOutputStream output;
        try {
            output = new FileOutputStream("Agents vs round" + pf + ".jpg");
            ChartUtilities.writeChartAsJPEG(output, 1.0f, chart, dimensionX, dimensionY, null);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BoxPlotAgNumbervsRound.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(BoxPlotAgNumbervsRound.class.getName()).log(Level.SEVERE, null, ex);
        }*/

    }

    /**
     * Creates a sample dataset.
     *
     * @return A sample dataset.
     */
    private BoxAndWhiskerCategoryDataset createSampleDataset(ArrayList<Double> Pf) {
        String extension;
        Hashtable<String, String> Pop = new Hashtable<>();
        PrintWriter escribir;
        Scanner sc = null;
        ArrayList<Integer> aPops = new ArrayList<>();
        ArrayList<Double> aPf = new ArrayList<>();
        ArrayList<String> aTech = new ArrayList<>();
        ArrayList<String> filenamesSorted = new ArrayList<>();
        Hashtable<String, File> fileHashtable = new Hashtable<>();

        File f = new File(experimentsDir);
        File[] files = f.listFiles();
        DefaultBoxAndWhiskerCategoryDataset dataset;
        for (File file : files) {
            if (file.isDirectory() && file.getName().endsWith("newagbynode")) {
                
                File subdir = new File(file.getName());
                File[] filesInfo = subdir.listFiles();

                Arrays.sort(filesInfo, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {

                        String f1name = f1.getName();
                        String f2name = f2.getName();

                        String[] splittt = f1name.split(Pattern.quote("+"));
                        String[] splittt2 = f2name.split(Pattern.quote("+"));
                        Date date1 = null, date2 = null;

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String dateInString = f1name.split(Pattern.quote("+"))[splittt.length - 2];
                        String dateInString2 = f2name.split(Pattern.quote("+"))[splittt2.length - 2];
                        try {
                            date1 = sdf.parse(dateInString);
                            date2 = sdf.parse(dateInString2);
                        } catch (ParseException ex) {
                            Logger.getLogger(GrowthCurveSimilarityvsSimulationNumber.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("date1:" + date1 + ", date2:" + date2);
                        return date1.compareTo(date2);
                        //return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });

                int lastRoundReaded = 0;
                String graphtype = "";
                String mode = "";

                HashMap<String, ArrayList<Double>> InfoBySimulation = new HashMap<>();
                ArrayList<Integer> popsizes = new ArrayList<>();
                for (File fileInfo : filesInfo) {
                    extension = "";
                    int i = fileInfo.getName().lastIndexOf('.');
                    int p = Math.max(fileInfo.getName().lastIndexOf('/'), fileInfo.getName().lastIndexOf('\\'));
                    if (i > p) {
                        extension = fileInfo.getName().substring(i + 1);
                    }
                    System.out.println("jaakak" + fileInfo.isFile());

                    // System.out.println(file.getName() + "extension" + extension);
                    if (fileInfo.isFile() && extension.equals("csv") && fileInfo.getName().startsWith("exp") && fileInfo.getName().contains("newagbynode")) {
                        ArrayList Data;
                        System.out.println(fileInfo.getName());
                        System.out.println("get: " + fileInfo.getName());
                        String[] filenamep = fileInfo.getName().split(Pattern.quote("+"));
                        System.out.println("file" + filenamep[8]);
                        int popsize = Integer.valueOf(filenamep[2]);
                        double pf = Double.valueOf(filenamep[4]);
                        mode = filenamep[6];
                        int maxIter = -1;
                        //if (!filenamep[8].isEmpty()) {
                        maxIter = Integer.valueOf(filenamep[8]);
                        //}

                        //if (!filenamep[8].isEmpty()) {
                        maxIter = Integer.valueOf(filenamep[8]);
                        //}
                        graphtype = filenamep[13];

                        System.out.println("psize:" + popsize);
                        System.out.println("pf:" + pf);
                        System.out.println("mode:" + mode);
                        System.out.println("maxIter:" + maxIter);
                        System.out.println("graph type:" + graphtype);

                        if (!popsizes.contains(popsize)) {
                            popsizes.add(popsize);
                        }
                        //Read each fileInfo file
                        try {
                            sc = new Scanner(fileInfo);

                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(BoxPlotRoundNumberSort.class
                                    .getName()).log(Level.SEVERE, null, ex);
                        }

                        String[] data = null;

                        HashMap<String, Double> totalBySim = new HashMap<>();
                        while (sc.hasNext()) {
                            String line = sc.nextLine();
                            System.out.println("line:" + line);
                            data = line.split(",");
                            String nodeId = data[0];

                            String[] replicasIds = data[1].split("-");
                            for (int k = 0; k < replicasIds.length; k++) {
                                if (!totalBySim.containsKey(nodeId + "-" + replicasIds[k])) {
                                    totalBySim.put(nodeId + "-" + replicasIds[k], (double) 1);
                                } else {
                                    totalBySim.put(nodeId + "-" + replicasIds[k], totalBySim.get(nodeId + "-" + replicasIds[k]) + 1.0);
                                }
                            }
                        }

                        for (String key : totalBySim.keySet()) {
                            if (!InfoBySimulation.containsKey(key)) {
                                InfoBySimulation.put(key, new ArrayList<Double>());
                            }
                            InfoBySimulation.get(key).add(totalBySim.get(key));
                        }
                        sc.close();
                    }
                }
                //String[] aMode = {"random", "levywalk", "sandc", "sandclw"};
                //String[] aMode = {"levywalk", "lwphevap", "hybrid", "hybrid3", "hybrid4", "sequential"};
                //if (/*Pf == pf && */isInMode(aMode, mode)) {
                for (int ps : popsizes) {
                    for (int l = 0; l < ps; l++) {
                        dataset = new DefaultBoxAndWhiskerCategoryDataset();
                        for (String k : InfoBySimulation.keySet()) {
                            if (Integer.valueOf(k.split("-")[1]) == l) {
                                StatisticsNormalDist st = new StatisticsNormalDist(InfoBySimulation.get(k), InfoBySimulation.get(k).size());
                                String[] filenametmp = file.getName().split(Pattern.quote(graphtype));
                                String fn2 = filenametmp[1].replace(".graph.csv", "");
                                //minimum.add((double)graphMetric.get(k), (double)(st.getMedian() - st.getStdDevMedian()));
                                //maximum.add((double)graphMetric.get(k), (double)(st.getMedian() + st.getStdDevMedian()));                    
                                //maximum.add((double)graphMetric.get(k), st.getMax());
                                dataset.add(InfoBySimulation.get(k), k, getTechniqueName(mode) + "\n" + graphtype + "\n" + fn2 + "\n");
                            }
                        }
                        final CategoryAxis xAxis = new CategoryAxis("");
                        //final NumberAxis yAxis = new NumberAxis("Round number");
                        final NumberAxis yAxis = new NumberAxis("");
                        yAxis.setAutoRangeIncludesZero(false);
                        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
                        renderer.setFillBox(false);
                        renderer.setMeanVisible(false);
                        renderer.setToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
                        renderer.setFillBox(true);
                        renderer.setSeriesPaint(0, Color.WHITE);
                        renderer.setSeriesPaint(1, Color.LIGHT_GRAY);
                        renderer.setSeriesOutlinePaint(0, Color.BLACK);
                        renderer.setSeriesOutlinePaint(1, Color.BLACK);
                        renderer.setUseOutlinePaintForWhiskers(true);
                        Font legendFont = new Font("SansSerif", Font.PLAIN, 16);
                        renderer.setLegendTextFont(0, legendFont);
                        renderer.setLegendTextFont(1, legendFont);
                        renderer.setMedianVisible(true);
                        renderer.setMeanVisible(false);
                        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

                        Font font = new Font("Dialog", Font.PLAIN, 12);
                        xAxis.setTickLabelFont(font);
                        yAxis.setTickLabelFont(font);
                        yAxis.setLabelFont(font);
                        xAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
                        xAxis.setMaximumCategoryLabelLines(5);

                        final JFreeChart chart = new JFreeChart(
                                "Agent Number vs Round number" + getTitle(Pf),
                                new Font("SansSerif", Font.BOLD, 18),
                                plot,
                                true
                        );

                        final ChartPanel chartPanel = new ChartPanel(chart);
                        chartPanel.setPreferredSize(new java.awt.Dimension(dimensionX, dimensionY));
                        setContentPane(chartPanel);

                        TextTitle legendText = null;
                        if (Pf.size() == 1) {
                            legendText = new TextTitle("Replicas Count");
                        } else {
                            legendText = new TextTitle("Replicas Count");
                        }

                        legendText.setFont(font);
                        legendText.setPosition(RectangleEdge.BOTTOM);
                        chart.addSubtitle(legendText);
                        chart.getLegend().setItemFont(font);

                        String agNumberdirName = file.getName() + "Idagent";
                        createDir(agNumberdirName);

                        FileOutputStream output;
                        try {
                            output = new FileOutputStream("./" + agNumberdirName + "/NodeId-agId" + l +" vs Replicas Count" + file.getName() + ".jpg");
                            ChartUtilities.writeChartAsJPEG(output, 1.0f, chart, dimensionX, dimensionY, null);
                        } catch (FileNotFoundException ex) {
                            Logger.getLogger(CompiledBoxPlotAgentIdandNode.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            Logger.getLogger(CompiledBoxPlotAgentIdandNode.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }

            }
        }

        return null;
    }

    private void createDir(String filename) {
        File theDir = new File(filename);

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + filename);
            boolean result = false;

            try {
                theDir.mkdir();
                result = true;
            } catch (SecurityException se) {
                System.out.println("Security Exception!");
            }
            if (result) {
                System.out.println("DIR created");
            }
        }

    }

    private static ArrayList<Double> getFailureProbs() {
        ArrayList<Double> pfs = new ArrayList<>();
        String sDirectorio = experimentsDir;
        System.out.println("experiments dir:" + sDirectorio);
        File f = new File(sDirectorio);
        String extension;
        File[] files = f.listFiles();

        for (File file : files) {
            extension = "";
            int i = file.getName().lastIndexOf('.');
            int p = Math.max(file.getName().lastIndexOf('/'), file.getName().lastIndexOf('\\'));
            if (i > p) {
                extension = file.getName().substring(i + 1);
            }

            // System.out.println(file.getName() + "extension" + extension);
            if (file.isFile() && extension.equals("csv") && file.getName().startsWith("exp") && !file.getName().contains("gstats")) {
                System.out.println(file.getName());
                System.out.println("get: " + file.getName());
                String[] filenamep = file.getName().split(Pattern.quote("+"));
                System.out.println("file" + filenamep[8]);
                int popsize = Integer.valueOf(filenamep[2]);
                double pf = Double.valueOf(filenamep[4]);
                String mode = filenamep[6];
                int maxIter = -1;
                //if (!filenamep[8].isEmpty()) {
                maxIter = Integer.valueOf(filenamep[8]);
                //}
                System.out.println("psize:" + popsize);
                System.out.println("pf:" + pf);
                System.out.println("mode:" + mode);
                System.out.println("maxIter:" + maxIter);

                System.out.println("pf:" + pf);
                if (!pfs.contains(pf)) {
                    pfs.add(pf);
                }
            }
        }
        System.out.println("pfs" + pfs);
        return pfs;
    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    /**
     * For testing from the command line.
     *
     * @param args ignored.
     */
    public static void main(final String[] args) {
//        double pf = Double.valueOf(args[0]);
        //       System.out.println("pf:" + args[0]);
        /* SASO Paper */
 /*
         double pf = 0;
         ArrayList<Double> pf0 = new ArrayList<>();
         ArrayList<Double> pfg1 = new ArrayList<>();
         ArrayList<Double> pfg2 = new ArrayList<>();
         ArrayList<Double> pfg3 = new ArrayList<>();
         pf0.add(0.0);
         pfg1.add(1.0E-4);
         pfg1.add(3.0E-4);
         pfg2.add(5.0E-4);
         pfg2.add(7.0E-4);
         pfg3.add(9.0E-4);
         //pfg3.add(1.0E-3);
         //Log.getInstance().addTarget(new PrintStreamLogTarget(System.out));
         final RoundNumber1 demo = new RoundNumber1("Round Number", pf0);
         final RoundNumber1 demo1 = new RoundNumber1("Round Number", pfg1);
         final RoundNumber1 demo2 = new RoundNumber1("Round Number", pfg2);
         final RoundNumber1 demo3 = new RoundNumber1("Round Number", pfg3);
         //demo.pack();
         //RefineryUtilities.centerFrameOnScreen(demo);
         //demo.setVisible(true);
         */
        if (args.length > 0) {
            experimentsDir = args[0];
        }
        if (args.length > 1) {
            graphName = args[1];
        }

        if (args.length > 2) {
            dimensionX = Integer.valueOf(args[2]);
        }

        if (args.length > 3) {
            dimensionY = Integer.valueOf(args[3]);
        }

        /*if (args.length > 1) {
            mazeMode = args[1];
        }

        aMode = new String[args.length - 2];

        for (int i = 2; i < args.length; i++) {
            aMode[i - 2] = args[i];
        }*/
        ArrayList<Double> failureProbs = getFailureProbs();

        for (Double pf : failureProbs) {
            ArrayList<Double> pfi = new ArrayList<>();
            pfi.add(pf);
            final CompiledBoxPlotAgentIdandNode demo = new CompiledBoxPlotAgentIdandNode("Agent Number vs Round  Number", pfi);
        }

        /*
         double pf = 0;
         ArrayList<Double> pf0 = new ArrayList<>();
         ArrayList<Double> pf1 = new ArrayList<>();
         ArrayList<Double> pf3 = new ArrayList<>();
         ArrayList<Double> pf5 = new ArrayList<>();
         ArrayList<Double> pf7 = new ArrayList<>();
         ArrayList<Double> pf9 = new ArrayList<>();
         ArrayList<Double> pf01 = new ArrayList<>();
         pf0.add(0.0);
         pf1.add(1.0E-4);
         pf3.add(3.0E-4);
         pf5.add(5.0E-4);
         pf7.add(7.0E-4);
         pf9.add(9.0E-4);
         pf01.add(1.0E-3);
         //pfg3.add(1.0E-3);
         //Log.getInstance().addTarget(new PrintStreamLogTarget(System.out));
         final RoundNumber1 demo = new RoundNumber1("Round Number", pf0);
         final RoundNumber1 demo1 = new RoundNumber1("Round Number", pf1);
         final RoundNumber1 demo2 = new RoundNumber1("Round Number", pf3);
         final RoundNumber1 demo3 = new RoundNumber1("Round Number", pf5);
         final RoundNumber1 demo4 = new RoundNumber1("Round Number", pf7);
         final RoundNumber1 demo5 = new RoundNumber1("Round Number", pf9);
         final RoundNumber1 demo6 = new RoundNumber1("Round Number", pf01);
         //demo.pack();
         //RefineryUtilities.centerFrameOnScreen(demo);
         //demo.setVisible(true);
         */
    }

    private boolean isInMode(String[] aMode, String mode) {
        for (String temp : aMode) {
            if (temp.equals(mode)) {
                return true;
            }
        }
        return false;
    }

    private static String getTechniqueName(String mode) {
        switch (mode) {
            case "sandclw":
                return "SandC with Lw";
            case "lwsandc2":
                return "Lw and C";
            case "lwsandc":
                return "Lw and C-Lw";
            case "lwphevap2":
                return "C and Evap";
            case "lwphevap":
                return "C-Lw and Evap";
            default:
                return mode;
        }
    }

    private String getTitle(ArrayList<Double> pf) {
        String s = " with a pf=";

        for (int i = 0; i < pf.size(); i++) {
            s += pf.get(i);
            if (i != pf.size() - 1) {
                s += " and pf=";
            }
        }
        return s;
    }

}
