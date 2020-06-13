package anomaliesDetection.analyse;

import anomaliesDetection.anomaliesReporting.CollisionAnomaly;
import anomaliesDetection.anomaliesReporting.ElementProtrusionAnomaly;
import anomaliesDetection.anomaliesReporting.ResponsiveLayoutAnomaly;
import anomaliesDetection.anomaliesReporting.ViewportProtrusionAnomaly;
import anomaliesDetection.layout.LayoutFactory;
import anomaliesDetection.utils.PDFUtils;
import anomaliesDetection.responsiveLayoutGraph.*;
import com.google.common.collect.HashBasedTable;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class RLGAnalyser {

    ResponsiveLayoutGraph responsiveLayoutGraph;
    ArrayList<ResponsiveLayoutAnomaly> errors;
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

    public ArrayList<ResponsiveLayoutAnomaly> analyse() {

        detectElementCollisionAndProtrusionAnomalies(responsiveLayoutGraph.getAlignmentConstraints());
        detectViewportProtrusionAnomalies(responsiveLayoutGraph.getNodes());

        return errors;
    }

    public void detectElementCollisionAndProtrusionAnomalies(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
        // iterating through all alignment constraints in the RLG until it finds one for a pair of elements in a sibling relationship
        alignmentConstraints.values().forEach(alignmentConstraint -> {
            if (alignmentConstraint.getType() == Type.SIBLING) {
                // inspects its attribute set for the overlapping attribute
                if (alignmentConstraint.getAttributes()[10]) {

                    boolean collision = checkElementCollision(alignmentConstraint);

                    // continue checking if the current constraint was not identified as a collision anomaly
                    if (!collision) {
                        checkElementProtrusion(alignmentConstraint);
                    }
                }
            }
        });
    }

    private boolean checkElementCollision(AlignmentConstraint alignmentConstraint){
        boolean collision = false;
        AlignmentConstraint next = getPreviousOrNextConstraint(alignmentConstraint, false, false);
        // investigate whether the two elements were not overlapping at the wider range
        if (next != null && next.getType() == Type.SIBLING) {
            // If the attribute is not present, it signifies the elements are no longer overlapping
            if (!next.getAttributes()[10]) {
                // report an element collision anomaly
                errors.add(new CollisionAnomaly(alignmentConstraint));
                collision = true;
            }
        }
        return collision;
    }

    private void checkElementProtrusion(AlignmentConstraint alignmentConstraint) {
        // get the ancestry of the two nodes
        HashSet<Node> n1Ancestry = getAncestry(alignmentConstraint.getNode1(), alignmentConstraint.getMax() + 1);
        HashSet<Node> n2Ancestry = getAncestry(alignmentConstraint.getNode2(), alignmentConstraint.getMax() + 1);
        //check the ancestry sets. If node1 is an ancestor of node2, or vice versa, it reports an element protrusion anomaly
        if (n1Ancestry.contains(alignmentConstraint.getNode2())) {
            errors.add(new ElementProtrusionAnomaly(alignmentConstraint.getNode1(), alignmentConstraint));
        } else if (n2Ancestry.contains(alignmentConstraint.getNode1())) {
            errors.add(new ElementProtrusionAnomaly(alignmentConstraint.getNode2(), alignmentConstraint));
        }
    }

    public void detectViewportProtrusionAnomalies(HashMap<String, Node> nodes) {

        nodes.values().forEach(node ->{

            if (!node.getxPath().equals("/HTML/BODY")) {

                ArrayList<AlignmentConstraint> parentConstraints = node.getParentConstraints();

                TreeMap<Integer, Integer> conBounds = new TreeMap<>();

                parentConstraints.forEach(parentConstraint -> {
                    conBounds.put(parentConstraint.getMin(), parentConstraint.getMax());
                });

                if (parentConstraints.size() > 0) {
                    int gmin = vmin;

                    for (Map.Entry e : conBounds.entrySet()) {
                        int gmax = (int) e.getKey() - 1;
                        if (gmin < gmax) {
                            String key = isVisible(node, gmin, gmax);
                            if (!key.equals("")) {
                                int repMin = getNumberFromKey(key, 0);
                                int repMax = getNumberFromKey(key, 1);
                                errors.add(new ViewportProtrusionAnomaly(node, repMin, repMax));
                            }
                        }
                        gmin = (int) e.getValue() + 1;
                    }
                    if (gmin < vmax && !isVisible(node, gmin, vmax).equals("")) {
                        errors.add(new ViewportProtrusionAnomaly(node, gmin, vmax));
                    }
                }
            }
        });
    }

    /**
     * This method investigates whether a node, n, is visible at any viewport widths within a range
     *
     * @param n    the node being investigated
     * @param gmin the lower bound of the range
     * @param gmax the upper bound of the range
     * @return
     */
    private String isVisible(Node n, int gmin, int gmax) {
        // Get the visibility constraints of n
        ArrayList<VisibilityConstraint> vcons = n.getVisibilityConstraints();

        // Iterate through each one
        for (VisibilityConstraint vc : vcons) {
            int visMin = vc.appear;
            int visMax = vc.disappear;

            // Check if the constraint intersects the range
            if (gmax >= visMin && gmax <= visMax) {
                // If so, return the range of widths within the range at which n is visible
                if (visMin <= gmin) {
                    return gmin + ":" + gmax;
                } else {
                    return visMin + ":" + gmax;
                }
            }
        }
        return "";
    }

    /**
     * Simple utility to extract a numeric bound from a string key
     *
     * @param key The string key to extract from
     * @param i   The bound we want (either 0 or 1)
     * @return The extracted bound
     */
    private int getNumberFromKey(String key, int i) {
        String[] splits = key.split(":");
        return Integer.valueOf(splits[i]);
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
        }
        return null;
    }

    public void writeReport(String url, ArrayList<ResponsiveLayoutAnomaly> errors, String ts) {
        PrintWriter output = null;
        try {
            File outputFile = null;
            if (!url.contains("www.") && (!url.contains("http://"))) {
                String[] splits = url.split("/");
                String webpage = splits[0];
                String mutant = "index-" + ts;
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
            PDFUtils.generatePDFReport(outputFile, errors);

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
