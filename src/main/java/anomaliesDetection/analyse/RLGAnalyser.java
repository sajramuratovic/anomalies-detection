package anomaliesDetection.analyse;

import anomaliesDetection.anomaliesReporting.CollisionFailure;
import anomaliesDetection.anomaliesReporting.ElementProtrusionFailure;
import anomaliesDetection.anomaliesReporting.ResponsiveLayoutFailure;
import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.responsiveLayoutGraph.AlignmentConstraint;
import anomaliesDetection.responsiveLayoutGraph.Node;
import anomaliesDetection.responsiveLayoutGraph.ResponsiveLayoutGraph;
import anomaliesDetection.responsiveLayoutGraph.Type;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class RLGAnalyser {

    ResponsiveLayoutGraph responsiveLayoutGraph;
    ArrayList<ResponsiveLayoutFailure> errors;
    WebDriver driver;
    String url;
    ArrayList<Integer> bpoints;
    ArrayList<Node> onePixelOverflows;
    HashMap<Integer, LayoutFactory> layouts;
    int vmin, vmax;

    public RLGAnalyser(ResponsiveLayoutGraph r, WebDriver webDriver, String fullUrl, ArrayList<Integer> breakpoints, HashMap<Integer, LayoutFactory> lFactories, int vmin, int vmax) {
        responsiveLayoutGraph = r;
        driver = webDriver;
        url = fullUrl;
        bpoints = breakpoints;
        onePixelOverflows = new ArrayList<>();
        layouts = lFactories;
        this.vmin = vmin;
        this.vmax = vmax;
        errors = new ArrayList<>();
    }

    public ArrayList<ResponsiveLayoutFailure> analyse() {

        //checkForViewportOverflows(responsiveLayoutGraph.getNodes());
        detectOverflowOrOverlap(responsiveLayoutGraph.getAlignmentConstraints());
        //checkForSmallRanges(responsiveLayoutGraph.getAlignmentConstraints());
        //checkForWrappingElements();

        return errors;
    }

    public void checkForViewportOverflows(HashMap<String, Node> nodes) {
        //TODO
    }

    /**
     * This method examines the alignment constraints from the RLG under test to see if any overlapping or overflowing elements
     * have been found
     *
     * @param alignmentConstraints the set of constraints to analyse
     */
    public void detectOverflowOrOverlap(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {

        // Iterate through all constraints
        for (AlignmentConstraint ac : alignmentConstraints.values()) {
            // We only need to look at the sibling ones
            if (ac.getType() == Type.SIBLING) {

                // Only continue analysis if the "overlapping" attribute label is true
                if (ac.getAttributes()[10]) {
                    boolean collision = false;
                    AlignmentConstraint next = getPreviousOrNextConstraint(ac, false, false);

                    // Now, investigate whether the two elements were NOT overlapping at the wider range
                    if (next != null && next.getType() == Type.SIBLING) {
                        // Check if elements overlapping in next constraint
                        if (!next.getAttributes()[10]) {
                            // If they weren't then report a collision failure
                            CollisionFailure oe = new CollisionFailure(ac);
                            errors.add(oe);
                            collision = true;
                        }
                    }

                    // Only continue checking if the current constraint was not identified as a collision failure
                    if (!collision) {
                        // Get the ancestry of the two nodes, so we can see if the overlap is due to an overflow
                        HashSet<Node> n1Ancestry = getAncestry(ac.getNode1(), ac.getMax() + 1);
                        HashSet<Node> n2Ancestry = getAncestry(ac.getNode2(), ac.getMax() + 1);

                        // If node2 in ancestry of node1 or vice verse, it's an overflow
                        if (n1Ancestry.contains(ac.getNode2())) {
                            ElementProtrusionFailure ofe = new ElementProtrusionFailure(ac.getNode1(), ac);
                            errors.add(ofe);
                        } else if (n2Ancestry.contains(ac.getNode1())) {
                            ElementProtrusionFailure ofe = new ElementProtrusionFailure(ac.getNode2(), ac);
                            errors.add(ofe);
                        }
                    }
                }
            }
        }
    }

    public void checkForSmallRanges(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
        //TODO
    }

    private void checkForWrappingElements() {
        //TODO
    }

    /**
     * This method searches for the constraint either immediately before or after a given constraint
     *
     * @param ac        the constraint we're investigating
     * @param i         whether we want the preceding (true) or following (false) constraint
     * @param matchType whether we care about matching the type as well as the nodes
     * @return the matched constraint, if one was found
     */
    private AlignmentConstraint getPreviousOrNextConstraint(AlignmentConstraint ac, boolean i, boolean matchType) {
        String ac1xp = ac.getNode1().getxPath();
        String ac2xp = ac.getNode2().getxPath();

        for (AlignmentConstraint con : responsiveLayoutGraph.getAlignmentConstraints().values()) {
//
            String con1xp = con.getNode1().getxPath();
            String con2xp = con.getNode2().getxPath();
            if ((ac1xp.equals(con1xp) && ac2xp.equals(con2xp)) || (ac1xp.equals(con2xp) && ac2xp.equals(con1xp))) {
                if (i) {
                    if (con.getMax() == ac.getMin() - 1) {
                        if (!matchType) {
                            return con;
                        } else {
                            if (con.getType() == ac.getType()) {
                                return con;
                            }
                        }
                    }
                } else {
                    if (con.getMin() == ac.getMax() + 1) {
                        if (!matchType) {
                            return con;
                        } else {
                            if (con.getType() == ac.getType()) {
                                return con;
                            }
                        }
                    }
                }
            }
//            }
        }
        return null;
    }

    public void writeReport(String url, ArrayList<ResponsiveLayoutFailure> errors, String ts) {
        PrintWriter output = null;
        PrintWriter output2 = null;
        PrintWriter output3 = null;
        try {
            File outputFile = null;
            if (!url.contains("www.") && (!url.contains("http://"))) {
                String[] splits = url.split("/");
                String webpage = splits[0];
                String mutant = "index-" + ts;
                //                    splits[1];
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (url.contains("http://")) {
                String[] splits = url.split("http://");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new java.io.File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String[] splits = url.split("www.");
                String webpage = splits[1];
                String mutant = ts;
                try {
                    outputFile = new File(new File(".").getCanonicalPath() + "/../reports/" + webpage + "/" + mutant + "/");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileUtils.forceMkdir(outputFile);
            File dir = new File(outputFile + "/fault-report.txt");
//            File countDir = new File(outputFile + "/error-count.txt");
//            File typeFile = new File(outputFile + "/error-types.txt");
            File classification = new File(outputFile + "/classification.txt");
            File actualFaultsFile = new File(outputFile + "/../actual-fault-count.txt");
//            classification.createNewFile();
//            actualFaultsFile.createNewFile();
            output = new PrintWriter(dir);
//            output2 = new PrintWriter(countDir);
//            output3 = new PrintWriter(typeFile);
            if (errors.size() > 0) {
//                output2.append(Integer.toString(errors.size()));
                for (ResponsiveLayoutFailure rle : errors) {
                    output.append(rle.toString() + "\n\n");
//                    output3.append(errorToKey(rle) + "\n");
                }
            } else {
                output.append("NO FAULTS DETECTED.");
//                output2.append("0");
            }

            output.close();
//            output2.close();
//            output3.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * This method traverses the RLG to obtain a set of ancestors for a node at a given viewport width.
     *
     * @param node1 the node whose ancestry we want
     * @param i     the viewport width at which to traverse
     * @return
     */
    private HashSet<Node> getAncestry(Node node1, int i) {
        HashSet<Node> ancestors = new HashSet<>();

        // Initialise the worklist and add the initial node
        ArrayList<Node> workList = new ArrayList<>();
        workList.add(node1);

        // Keeping track of the nodes we've analysed.
        ArrayList<Node> analysed = new ArrayList<>();
        try {
            // While there's still nodes left.
            while (!workList.isEmpty()) {
                // Take the next node from the list
                Node n = workList.remove(0);

                // Get the parent constraints for the current node
                ArrayList<AlignmentConstraint> cons = n.getParentConstraints();
                for (AlignmentConstraint ac : cons) {
                    // Check if this constraint is true at the desired width
                    if (ac.getMin() <= i && ac.getMax() >= i) {
                        // Add the parent to the list of ancestors and the worklist
                        ancestors.add(ac.getNode1());
                        if (!analysed.contains(ac.getNode1())) {
                            workList.add(ac.getNode1());
                        }
                    }
                }
                analysed.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ancestors;
    }


}
