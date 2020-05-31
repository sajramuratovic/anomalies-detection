package anomaliesDetection.responsiveLayoutGraph;

import anomaliesDetection.layout.*;
import anomaliesDetection.main.AutomaticAnomaliesDetectionController;
import anomaliesDetection.utils.StopwatchFactory;
import com.google.common.collect.HashBasedTable;
import org.openqa.selenium.WebDriver;
import xPert.layout.AlignmentGraphFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ResponsiveLayoutGraph {

    ArrayList<AlignmentGraphFactory> restOfGraphs;
    ArrayList<LayoutFactory> restOfLayouts;
    ArrayList<LayoutFactory> layouts;

    HashMap<String, Node> nodes = new HashMap<>();
    HashMap<String, VisibilityConstraint> visCons = new HashMap<>();
    HashMap<Integer, LayoutFactory> lFactories;
    HashMap<Integer, AlignmentGraphFactory> factories;
    static HashSet<Integer> alreadyGathered;

    AlignmentGraphFactory first, last;
    LayoutFactory firstLF, lastLF;
    private HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints = HashBasedTable.create();

    boolean binarySearch;
    StopwatchFactory swf;
    String url;
    int[] widths;
    int[] restOfWidths;
    WebDriver wdriver;
    int sleep;

    public ResponsiveLayoutGraph() {
        alreadyGathered = new HashSet<>();
        last = null;
    }

    public ResponsiveLayoutGraph(ArrayList<LayoutFactory> layouts, int[] stringWidths, String url, HashMap<Integer, LayoutFactory> facts, boolean bs, WebDriver driver, StopwatchFactory swf, int sl) throws InterruptedException {
        this.swf = swf;
        this.layouts = layouts;
        this.firstLF = layouts.get(0);
        this.lastLF = layouts.get(layouts.size() - 1);
        this.wdriver = driver;
        this.sleep = sl;
        binarySearch = bs;
        restOfGraphs = new ArrayList<AlignmentGraphFactory>();
        restOfLayouts = new ArrayList<>();
        factories = new HashMap<Integer, AlignmentGraphFactory>();
        lFactories = new HashMap<>();
        for (LayoutFactory lf : layouts) {
            restOfLayouts.add(lf);
            lFactories.put(stringWidths[layouts.indexOf(lf)], lf);
        }
        restOfLayouts.remove(0);
        this.url = url;
        this.lFactories = facts;
        alreadyGathered = new HashSet<Integer>();
        restOfWidths = new int[stringWidths.length - 1];
        this.widths = stringWidths;
        for (int i = 0; i < stringWidths.length; i++) {
            int s = stringWidths[i];
            if (i > 0) {
                restOfWidths[i - 1] = s;
            }
            alreadyGathered.add(s);
        }
        extractVisibilityConstraints();
        extractAlignmentConstraints();
    }

    public static HashSet<Integer> getAlreadyGathered() {
        return alreadyGathered;
    }

    public static void setAlreadyGathered(HashSet<Integer> alreadyGathered) {
        ResponsiveLayoutGraph.alreadyGathered = alreadyGathered;
    }

    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    public void setNodes(HashMap<String, Node> nodes) {
        this.nodes = nodes;
    }

    /**
     * Extracts all the visibility constraints for each node on the webpage by inspecting which elements are visible at
     * which resolutions.
     *
     * @throws InterruptedException
     */
    private void extractVisibilityConstraints() throws InterruptedException {

        HashMap<String, Element> curr = firstLF.getElementMap();
        HashMap<String, Element> prev = firstLF.getElementMap();
        HashMap<String, Element> prevToMatch, currToMatch;

        setUpVisibilityConstraints(curr, visCons);

        for (LayoutFactory lf : restOfLayouts) {
            prevToMatch = (HashMap<String, Element>) prev.clone();
            curr = (HashMap<String, Element>) lf.getElementMap();
            currToMatch = (HashMap<String, Element>) curr.clone();

            // Matches the nodes seen at the last sample point to those seen at the current one
            checkForNodeMatch(prev, curr, prevToMatch, currToMatch);

            // Handle any disappearing elements
            updateDisappearingNode(prevToMatch, visCons, lf);

            // Handle any appearing elements
            updateAppearingNode(currToMatch, visCons, lf);

            // Update the previousMap variable to keep track of last set of nodes
            prev = (HashMap<String, Element>) lf.getElementMap();
        }

        // Update visibility constraints of everything still visible
        updateRemainingNodes(visCons, lastLF);

        // Attach constraints to the nodes
        attachVisConsToNodes(visCons);
    }

    /**
     * Takes a map of visibility constraints and adds them to the relevant nodes
     *
     * @param visCons the map of constraints to be added
     */
    public void attachVisConsToNodes(HashMap<String, VisibilityConstraint> visCons) {
        for (String x : this.nodes.keySet()) {
            Node n = this.nodes.get(x);
            VisibilityConstraint vc = visCons.get(x);
            n.addVisibilityConstraint(vc);
        }
    }

    /**
     * Updates the visibility constraints of any nodes visible at the final sample point
     *
     * @param visCons the visibility constraints that may need to be updated
     * @param last    the data from the final sample point
     */
    public void updateRemainingNodes(HashMap<String, VisibilityConstraint> visCons, LayoutFactory last) {
        for (String stilVis : last.layout.getElements().keySet()) {
            VisibilityConstraint vc = visCons.get(stilVis);
            if (vc.getDisappear() == 0) {
                vc.setDisappear(widths[widths.length - 1]);
            }
        }
    }

    /**
     * Takes a set of nodes appearing at a given sample point and created visibility constraints and node objects for each
     *
     * @param tempToMatch the set of appearing nodes
     * @param visCons     the map of visibility constraints to add to
     * @param lf          the LayoutFactory containing the appearing nodes
     */
    public void updateAppearingNode(HashMap<String, Element> tempToMatch, HashMap<String, VisibilityConstraint> visCons, LayoutFactory lf) {
        // Iterate through all appearing nodes
        for (String currUM : tempToMatch.keySet()) {
            int appearPoint = 0;
            try {
                // Find the point at which it appears
                if (binarySearch) {
                    appearPoint = searchForLayoutChange(currUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], true, "", true);
                } else {
                    appearPoint = widths[restOfLayouts.indexOf(lf) + 1];
                }
            } catch (InterruptedException e) {

            }

            // Create a node object and a matching visibility constraint for the appearing element
            nodes.put(currUM, new Node(currUM));
            visCons.put(currUM, new VisibilityConstraint(appearPoint, 0));
        }
    }

    /**
     * Takes a set of disappearing elements and updates the visibility constraints linked to them
     *
     * @param previousToMatch the set of disappearing nodes
     * @param visCons         the map of visibility constraints to update
     * @param lf              the LayoutFactory containing the disappearing nodes
     */
    public void updateDisappearingNode(HashMap<String, Element> previousToMatch, HashMap<String, VisibilityConstraint> visCons, LayoutFactory lf) {
        // Iterate through all disappearing nodes
        for (String prevUM : previousToMatch.keySet()) {
            int disappearPoint = 0;
            try {
                // Find the point at which it disappears
                disappearPoint = searchForLayoutChange(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], true, "", false);
            } catch (InterruptedException e) {
            }

            // Get the existing visibility constraint for the node and update it with the disappearPoint
            VisibilityConstraint vc = visCons.get(prevUM);
            vc.setDisappear(disappearPoint - 1);
        }
    }

    /**
     * Takes the set of nodes from two consecutive sample points and matches the nodes
     *
     * @param previousMap     the previous set of nodes
     * @param temp            the current set of nodes
     * @param previousToMatch a copy of previousMap used for matching
     * @param tempToMatch     a copy of temp used for matching
     */
    public void checkForNodeMatch(HashMap<String, Element> previousMap, HashMap<String, Element> temp, HashMap<String, Element> previousToMatch, HashMap<String, Element> tempToMatch) {
        // Iterate through all nodes in the previous map
        for (String s : previousMap.keySet()) {
            // See if that node is visible in the current map
            if (temp.get(s) != null) {
                // If so, remove the matched nodes from their respective sets
                previousToMatch.remove(s);
                tempToMatch.remove(s);
            }
        }
    }

    /**
     * Sets up visibility constraints for all nodes visible at the first sample point
     *
     * @param elements the map of elements visible at the first sample point
     * @param cons     the map into which to add the new constraints
     */
    public void setUpVisibilityConstraints(HashMap<String, Element> elements, HashMap<String, VisibilityConstraint> cons) {
        // Iterate through all elements
        for (Element e : elements.values()) {

            // Add each node to overall set
            String xpath = e.getXpath();
            nodes.put(xpath, new Node(xpath));

            // Create visibility constraint for each one
            cons.put(xpath, new VisibilityConstraint(widths[0], 0));
        }
    }

    /**
     * Extracts the alignment constraints for all the nodes on the webpage.
     *
     * @throws InterruptedException
     */
    private void extractAlignmentConstraints() throws InterruptedException {
        HashMap<String, Relationship> prev = firstLF.getRelationships();
        HashMap<String, Relationship> curr = firstLF.getRelationships();
        HashMap<String, AlignmentConstraint> alCons = new HashMap<String, AlignmentConstraint>();
        // Add initial edges to set.
        setUpAlignmentConstraints(prev, alCons);
        int currentWidth = this.widths[0];
        int prevWidth = this.widths[0];

        HashMap<String, Relationship> prevToMatch, currToMatch;

        for (LayoutFactory lf : restOfLayouts) {
            currentWidth = this.widths[restOfLayouts.indexOf(lf) + 1];
            prevToMatch = (HashMap<String, Relationship>) prev.clone();
            curr = lf.getRelationships();
            currToMatch = (HashMap<String, Relationship>) curr.clone();

            // Match the edges visible at both sample points
            checkForEdgeMatch(prev, prevToMatch, curr, currToMatch);

            // Pair any unmatched edges and update the alignment constraints
            HashMap<Relationship, Relationship> matchedChangingEdges = pairUnmatchedEdges(prevToMatch, currToMatch);
            updatePairedEdges(matchedChangingEdges, getAlignmentConstraints(), alCons, lf);

            // If there are still some disappearing edges left
            if (prevToMatch.size() != 0) {
                // Check whether the edge has disappeared because one of the nodes has
                checkForNodeBasedDisappearances(prevToMatch, getAlignmentConstraints(), prevWidth, currentWidth);

                // Update any remaining disappearing edges
                updateDisappearingEdges(prevToMatch, getAlignmentConstraints(), lf);
            }

            // If there are still appearing edges left
            if (currToMatch.size() != 0) {
                // Check whether the edge has appeared because one of the nodes has
                checkForNodeBasedAppearances(currToMatch, getAlignmentConstraints(), alCons, prevWidth, currentWidth);

                // Update any remaining appearing edges
                updateAppearingEdges(currToMatch, getAlignmentConstraints(), alCons, lf);
            }
            prev = lf.getRelationships();
            prevWidth = currentWidth;
        }

        // Update alignment constraints of everything still visible
        LayoutFactory last = restOfLayouts.get(restOfLayouts.size() - 1);
        updateRemainingEdges(alCons, last);
        addParentConstraintsToNodes();
    }


    private HashMap<Relationship, Relationship> pairUnmatchedEdges(HashMap<String, Relationship> previous, HashMap<String, Relationship> curr) {
        HashMap<String, Relationship> previousToMatch = (HashMap<String, Relationship>) previous.clone();
        HashMap<String, Relationship> currToMatch = (HashMap<String, Relationship>) curr.clone();

        HashMap<Relationship, Relationship> paired = new HashMap<>();
        for (String s : previousToMatch.keySet()) {
            Relationship r = previousToMatch.get(s);

            Element e1 = r.getNode1();
            Element e2 = r.getNode2();
            for (String s2 : currToMatch.keySet()) {
                Relationship r2 = currToMatch.get(s2);
                Element e1m = r2.getNode1();
                Element e2m = r2.getNode2();

                // Checks to see if both node1 and node2 are the same.
                if ((e1.getXpath().equals(e1m.getXpath())) && (e2.getXpath().equals(e2m.getXpath())) && (r.getClass() == r2.getClass())) {
                    paired.put(r, r2);
                    previous.remove(s);
                    curr.remove(s2);
                    // Check for the flipped sibling node match
                } else if ((e1.getXpath().equals(e2m.getXpath())) && (e2.getXpath().equals(e1m.getXpath())) && (r.getClass() == r2.getClass()) && (r instanceof Sibling)) {
                    paired.put(r, r2);
                    previous.remove(s);
                    curr.remove(s2);
                    // Check for matching child, but differing parents
                } else if (((!e1.getXpath().equals(e1m.getXpath())) && (e2.getXpath().equals(e2m.getXpath())) && (r instanceof ParentChild) && (r2 instanceof ParentChild))) {
                    paired.put(r, r2);
                    previous.remove(s);
                    curr.remove(s2);
                }
            }
        }
        return paired;
    }

    private void updatePairedEdges(HashMap<Relationship, Relationship> matchedChangingEdges, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, LayoutFactory lf) {
        for (Relationship e : matchedChangingEdges.keySet()) {
            String pairedkey1 = LayoutFactory.generateKey(e);
            Relationship matched = matchedChangingEdges.get(e);

            int disappearPoint = 0;
            String flip = "";

            if (e instanceof Sibling) {
                Sibling s2 = (Sibling) e;
                flip = s2.getNode2().getXpath() + " sibling of " + s2.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s2);
            }
            try {
                disappearPoint = searchForLayoutChange(pairedkey1, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, flip, false);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(pairedkey1);
            Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);

            if (cons.size() > 0) {
                updateAlignmentConstraint(cons, disappearPoint - 1);
            }
            if ((cons2.size() > 0)) {
                updateAlignmentConstraint(cons2, disappearPoint - 1);
            }

            // Create the appearing AC (matched)
            AlignmentConstraint ac;
            Type t;
            if (matched instanceof ParentChild) {
                t = Type.PARENT_CHILD;
                ParentChild c = (ParentChild) matched;
                ac = new AlignmentConstraint(this.nodes.get(matched.getNode1().getXpath()), this.nodes.get(matched.getNode2().getXpath()), t, disappearPoint, 0,
                        new boolean[]{c.isCentreJust(), c.isLeftJust(), c.isRightJust(), c.isMiddleJust(), c.isTopJust(), c.isBottomJust()}, new boolean[]{c.ishFill(), c.isvFill()});
            } else {
                t = Type.SIBLING;
                Sibling s2 = (Sibling) matched;
                ac = new AlignmentConstraint(this.nodes.get(matched.getNode1().getXpath()), this.nodes.get(matched.getNode2().getXpath()), t, disappearPoint, 0,
                        new boolean[]{s2.isAbove(), s2.isBelow(), s2.isLeftOf(), s2.isRightOf(), s2.isTopEdge(), s2.isBottomEdge(), s2.isyMid(), s2.isLeftEdge(), s2.isRightEdge(), s2.isxMid(), s2.isOverlapping()}, null);

            }
            alCons.put(ac.generateKey(), ac);
            alignmentConstraints.put(ac.generateKey(), new int[]{disappearPoint, 0}, ac);
        }

    }

    public void updateRemainingEdges(HashMap<String, AlignmentConstraint> alCons, LayoutFactory last) {
        HashMap<String, Relationship> rels = last.getRelationships();
        for (String stilVis : rels.keySet()) {
            Relationship r = rels.get(stilVis);
            if (r instanceof ParentChild) {
                ParentChild cTemp = (ParentChild) r;
                Map<int[], AlignmentConstraint> cons = getAlignmentConstraints().row(stilVis);
                if (cons.size() != 0) {
                    updateAlignmentConstraint(cons, widths[widths.length - 1]);
                }
            } else {
                Sibling s = (Sibling) r;

                String flipped = s.getNode2().getXpath() + " sibling of " + s.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s);

                Map<int[], AlignmentConstraint> cons = getAlignmentConstraints().row(stilVis);
                Map<int[], AlignmentConstraint> cons2 = getAlignmentConstraints().row(flipped);

                if (cons.size() != 0) {
                    updateAlignmentConstraint(cons, widths[widths.length - 1]);
                } else if (cons2.size() != 0) {
                    updateAlignmentConstraint(cons2, widths[widths.length - 1]);
                }
            }
        }
    }

    private void updateAlignmentConstraint(Map<int[], AlignmentConstraint> cons, int disappearPoint) {
        for (int[] pair : cons.keySet()) {
            // Get the one without a max value
            if (pair[1] == 0) {
                AlignmentConstraint aCon = cons.get(pair);
                aCon.setMax(disappearPoint);
                pair[1] = disappearPoint;
            }
        }
    }

    public void updateAppearingEdges(HashMap<String, Relationship> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, LayoutFactory lf) {
        for (String currUM : tempToMatch.keySet()) {
            Relationship e = tempToMatch.get(currUM);
            int appearPoint = 0;
            Type t;
            AlignmentConstraint ac;
            if (e instanceof ParentChild) {
                ParentChild c = (ParentChild) e;
                try {
                    if (binarySearch) {
                        appearPoint = searchForLayoutChange(currUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, "", true);
                    } else {
                        appearPoint = widths[restOfLayouts.indexOf(lf) + 1];
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                t = Type.PARENT_CHILD;
                ac = new AlignmentConstraint(this.nodes.get(e.getNode1().getXpath()), this.nodes.get(e.getNode2().getXpath()), t, appearPoint, 0,
                        new boolean[]{c.isCentreJust(), c.isLeftJust(), c.isRightJust(), c.isMiddleJust(), c.isTopJust(), c.isBottomJust()}, new boolean[]{c.ishFill(), c.isvFill()});
            } else {
                t = Type.SIBLING;
                Sibling s2 = (Sibling) e;
                String flip = s2.getNode2().getXpath() + " sibling of " + s2.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s2);
                try {
                    if (binarySearch) {
                        appearPoint = searchForLayoutChange(currUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, flip, true);
                    } else {
                        appearPoint = widths[restOfLayouts.indexOf(lf) + 1];
                    }
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                ac = new AlignmentConstraint(this.nodes.get(e.getNode1().getXpath()), this.nodes.get(e.getNode2().getXpath()), t, appearPoint, 0,
                        new boolean[]{s2.isAbove(), s2.isBelow(), s2.isLeftOf(), s2.isRightOf(), s2.isTopEdge(), s2.isBottomEdge(), s2.isyMid(), s2.isLeftEdge(), s2.isRightEdge(), s2.isxMid(), s2.isOverlapping()}, null);

            }
            if (ac != null) {
                alCons.put(ac.generateKey(), ac);
                alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint, 0}, ac);
            }

        }
    }

    public void updateDisappearingEdges(HashMap<String, Relationship> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, LayoutFactory lf) {
        for (String prevUM : previousToMatch.keySet()) {
            Relationship e = previousToMatch.get(prevUM);
            int disappearPoint = 0;
            String flip = "";
            if (e instanceof ParentChild) {
                try {
                    disappearPoint = searchForLayoutChange(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, "", false);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } else {
                Sibling s2 = (Sibling) e;
                flip = s2.getNode2().getXpath() + " sibling of " + s2.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s2);
                try {
                    disappearPoint = searchForLayoutChange(prevUM, widths[restOfLayouts.indexOf(lf)], widths[restOfLayouts.indexOf(lf) + 1], false, flip, false);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(prevUM);
            Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);
            if (cons.size() > 0) {
                updateAlignmentConstraint(cons, disappearPoint - 1);
            } else if (cons2.size() > 0) {
                updateAlignmentConstraint(cons2, disappearPoint - 1);
            } else {
                System.out.println("Couldn't find existing constraint for " + prevUM);
            }
        }
    }

    private void checkForNodeBasedDisappearances(HashMap<String, Relationship> previousToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, int min, int max) {
        Element node1, node2;
        String flip;
        HashMap<String, Relationship> tempMap = (HashMap<String, Relationship>) previousToMatch.clone();
        for (String s : tempMap.keySet()) {
            flip = "";
            Relationship rel = tempMap.get(s);
            if (rel instanceof Sibling) {
                Sibling s2 = (Sibling) rel;
                flip = s2.getNode2().getXpath() + " sibling of " + s2.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s2);
            }
            node1 = rel.getNode1();
            node2 = rel.getNode2();
            if ((nodeDisappears(node1, min, max)) || (nodeDisappears(node2, min, max))) {
                // Get VC of disappearing node
                VisibilityConstraint vc = this.nodes.get(node1.getXpath()).getVisibilityConstraints().get(0);
                int disappearPoint = vc.getDisappear();

                // Update with correct value
                Map<int[], AlignmentConstraint> cons = alignmentConstraints.row(s);
                Map<int[], AlignmentConstraint> cons2 = alignmentConstraints.row(flip);
                if (cons.size() > 0) {
                    updateAlignmentConstraint(cons, disappearPoint);
                } else if (cons2.size() > 0) {
                    updateAlignmentConstraint(cons2, disappearPoint);
                }
            }
        }
    }

    private void checkForNodeBasedAppearances(HashMap<String, Relationship> tempToMatch, HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints, HashMap<String, AlignmentConstraint> alCons, int min, int max) {
        Element node1, node2;
        String flip;
        HashMap<String, Relationship> tempMap = (HashMap<String, Relationship>) tempToMatch.clone();
        for (String s : tempMap.keySet()) {
            flip = "";
            Relationship edge = tempMap.get(s);
            if (edge instanceof Sibling) {
                Sibling s2 = (Sibling) edge;
                flip = s2.getNode2().getXpath() + " sibling of " + s2.getNode1().getXpath() + LayoutFactory.generateFlippedLabelling(s2);
            }
            node1 = edge.getNode1();
            node2 = edge.getNode2();
            if ((nodeAppears(node1, min, max)) || (nodeAppears(node2, min, max))) {
                // Get VC of disappearing node
                try {
                    VisibilityConstraint vc = this.nodes.get(node1.getXpath()).getVisibilityConstraints().get(0);
                    int appearPoint = vc.getAppear();

                    Type t = null;
                    AlignmentConstraint ac = null;
                    if (edge instanceof ParentChild) {
                        ParentChild c = (ParentChild) edge;
                        t = Type.PARENT_CHILD;
                        ac = new AlignmentConstraint(this.nodes.get(edge.getNode1().getXpath()), this.nodes.get(edge.getNode2().getXpath()), t, appearPoint, 0,
                                new boolean[]{c.isCentreJust(), c.isLeftJust(), c.isRightJust(), c.isMiddleJust(), c.isTopJust(), c.isBottomJust()}, new boolean[]{c.ishFill(), c.isvFill()});

                    } else {
                        t = Type.SIBLING;
                        Sibling s2 = (Sibling) edge;

                        ac = new AlignmentConstraint(this.nodes.get(edge.getNode1().getXpath()), this.nodes.get(edge.getNode2().getXpath()), t, appearPoint, 0,
                                new boolean[]{s2.isAbove(), s2.isBelow(), s2.isLeftOf(), s2.isRightOf(), s2.isTopEdge(), s2.isBottomEdge(), s2.isyMid(), s2.isLeftEdge(), s2.isRightEdge(), s2.isxMid(), s2.isOverlapping()}, null);

                    }
                    if (ac != null) {
                        alCons.put(ac.generateKey(), ac);
                        alignmentConstraints.put(ac.generateKey(), new int[]{appearPoint, 0}, ac);
                        tempToMatch.remove(s);
                        tempToMatch.remove(flip);
                    }
                } catch (NullPointerException e) {
                }
            }
        }
    }

    private boolean nodeAppears(Element node1, int min, int max) {
        Node n = this.nodes.get(node1.getXpath());
        if (n != null) {
            ArrayList<VisibilityConstraint> vcs = n.getVisibilityConstraints();
            for (VisibilityConstraint vc : vcs) {
                int ap = vc.getAppear();
                if ((ap > min) && (ap < max)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean nodeDisappears(Element node1, int min, int max) {
        Node n = this.nodes.get(node1.getXpath());
        if (n != null) {
            ArrayList<VisibilityConstraint> vcs = n.getVisibilityConstraints();
            for (VisibilityConstraint vc : vcs) {
                int dp = vc.getDisappear();
                if ((dp > min) && (dp < max)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void checkForEdgeMatch(HashMap<String, Relationship> previousMap, HashMap<String, Relationship> previousToMatch, HashMap<String, Relationship> temp, HashMap<String, Relationship> tempToMatch) {
        String key = "", key2 = "";
        for (String sKey : previousMap.keySet()) {
            Relationship r = previousMap.get(sKey);

            if (r instanceof ParentChild) {
                ParentChild cTemp = (ParentChild) r;

                key = cTemp.getKey();

                if (temp.get(key) != null) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key);
                }
            } else {
                Sibling s = (Sibling) r;
                key = s.getKey();
                key2 = s.getFlipKey();
                if ((temp.get(key) != null)) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key);
                } else if (temp.get(key2) != null) {
                    previousToMatch.remove(key);
                    tempToMatch.remove(key2);
                }
            }

        }

    }

    public void setUpAlignmentConstraints(HashMap<String, Relationship> previousMap, HashMap<String, AlignmentConstraint> alCons) {
        for (String sKey : previousMap.keySet()) {
            try {
                Relationship r = previousMap.get(sKey);
                if (r instanceof ParentChild) {
                    ParentChild pc = (ParentChild) r;
                    AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(pc.getNode1().getXpath()), this.nodes.get(pc.getNode2().getXpath()), Type.PARENT_CHILD, this.widths[0], 0,
                            new boolean[]{pc.isCentreJust(), pc.isLeftJust(), pc.isRightJust(), pc.isMiddleJust(), pc.isTopJust(), pc.isBottomJust()}, new boolean[]{pc.ishFill(), pc.isvFill()});
                    alCons.put(con.generateKey(), con);
                    getAlignmentConstraints().put(con.generateKey(), new int[]{this.widths[0], 0}, con);
                } else {
                    Sibling s = (Sibling) r;
                    AlignmentConstraint con = new AlignmentConstraint(this.nodes.get(s.getNode1().getXpath()), this.nodes.get(s.getNode2().getXpath()), Type.SIBLING, this.widths[0], 0,
                            new boolean[]{s.isAbove(), s.isBelow(), s.isLeftOf(), s.isRightOf(), s.isTopEdge(), s.isBottomEdge(), s.isyMid(), s.isLeftEdge(), s.isRightEdge(), s.isxMid(), s.isOverlapping()}, null);
                    alCons.put(con.generateKey(), con);
                    getAlignmentConstraints().put(con.generateKey(), new int[]{this.widths[0], 0}, con);
                }
            } catch (Exception e) {

            }
        }
    }

    public LayoutFactory getLayoutFactory(int width) {
        LayoutFactory lf = lFactories.get(width);
        if (lf != null) {
            return lf;
        } else {
            if (!lFactories.containsKey(width)) {
                AutomaticAnomaliesDetectionController.capturePageModel(url, new int[]{width}, false, wdriver, lFactories, new HashMap<>());
                alreadyGathered.add(width);
            }
            return lFactories.get(width);
        }
    }

    /**
     * Goes through the full set of alignment constraints and adds the parent-child constraints to the node representing
     * the child element, for use in the width constraint extraction
     */
    public void addParentConstraintsToNodes() {
        for (AlignmentConstraint ac : this.getAlignmentConstraints().values()) {
            try {
                if (ac.type == Type.PARENT_CHILD) {
                    Node child = this.nodes.get(ac.node2.getxPath());
                    child.addAlignmentConstraint(ac);
                }
            } catch (NullPointerException e) {
                System.out.println("Tried adding parent constraint with " + ac);
            }
        }
    }

    /**
     * Returns the viewport width at which a particular node or edge comes into view
     *
     * @param searchKey     the search key to look for. Can be a node's XPath or a custom edge key
     * @param min           the lower bound of the search
     * @param max           the upper bound of the search
     * @param searchForNode whether the search is for a node or an edge, as the code is different for each
     * @param flippedKey    an alternate key for searching for sibling edges
     * @return the viewport width at which the object represented by the key comes into view
     * @throws InterruptedException
     */
    public int searchForLayoutChange(String searchKey, int min, int max, boolean searchForNode, String flippedKey, boolean appear) throws InterruptedException {
        if (max - min == 1) {
            return max;
        } else {
            int mid = (max + min) / 2;
            int[] extraWidths = new int[]{mid};
            captureExtraDoms(extraWidths);

            LayoutFactory extra = getLayoutFactory(mid);
            boolean found = checkLayoutForKey(extra, searchForNode, searchKey, flippedKey);
            if ((found && appear) || (!found && !appear)) {
                return searchForLayoutChange(searchKey, min, mid, searchForNode, flippedKey, appear);
            } else {
                return searchForLayoutChange(searchKey, mid, max, searchForNode, flippedKey, appear);
            }
        }
    }

    private boolean checkLayoutForKey(LayoutFactory lf1, boolean searchForNode, String searchKey, String flippedKey) {
        if (searchForNode) {
            HashMap<String, Element> n1 = lf1.getElementMap();
            return n1.get(searchKey) != null;
        } else {
            HashMap<String, Relationship> e1 = lf1.getRelationships();
            return (e1.get(searchKey) != null) || (e1.get(flippedKey) != null);
        }
    }

    public void captureExtraDoms(int[] widths) {
        if (widths.length == 2) {
            if ((!alreadyGathered.contains(widths[0])) && (!alreadyGathered.contains(widths[1]))) {
                AutomaticAnomaliesDetectionController.capturePageModel(url, widths, false, wdriver, lFactories, new HashMap<>());
                alreadyGathered.add(widths[0]);
                alreadyGathered.add(widths[1]);
            } else if (!alreadyGathered.contains(widths[0])) {
                AutomaticAnomaliesDetectionController.capturePageModel(url, new int[]{widths[0]}, false, wdriver, lFactories, new HashMap<>());
                alreadyGathered.add(widths[0]);
            } else if (!alreadyGathered.contains(widths[1])) {
                AutomaticAnomaliesDetectionController.capturePageModel(url, new int[]{widths[1]}, false, wdriver, lFactories, new HashMap<>());
                alreadyGathered.add(widths[1]);
            }
        } else if (widths.length == 1) {
            if (!alreadyGathered.contains(widths[0])) {
                AutomaticAnomaliesDetectionController.capturePageModel(url, new int[]{widths[0]}, false, wdriver, lFactories, new HashMap<>());
                alreadyGathered.add(widths[0]);
            }
        }
    }

    public HashBasedTable<String, int[], AlignmentConstraint> getAlignmentConstraints() {
        return alignmentConstraints;
    }

    public void setAlignmentConstraints(HashBasedTable<String, int[], AlignmentConstraint> alignmentConstraints) {
        this.alignmentConstraints = alignmentConstraints;
    }
}

