package responsiveLayoutGraph;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Nodes represent individual web page element and are arranged in a tree structure, connected by edges.
 * Visibility constraints are added to their respective nodes, while alignment constraints are mapped onto
 * the edges connecting the nodes whose layout they describe.
 */
public class Node {

    String xPath;
    ArrayList<AlignmentConstraint> alignmentConstraints;
    ArrayList<VisibilityConstraint> visibilityConstraints;

    public Node(String xPath) {
        this.xPath = xPath;
        this.alignmentConstraints = new ArrayList<AlignmentConstraint>();
        this.visibilityConstraints = new ArrayList<VisibilityConstraint>();
    }

    /**
     * Adds a visibility constraint to the node
     *
     * @param ac the alignment constraint to add
     */
    public void addAlignmentConstraint(AlignmentConstraint ac) {
        alignmentConstraints.add(ac);
    }

    /**
     * Adds a visibility constraint to the node
     *
     * @param vc the visibility constraint to add
     */
    public void addVisibilityConstraint(VisibilityConstraint vc) {
        visibilityConstraints.add(vc);
    }

    public String toString() {
        String result = this.xPath;
        for (VisibilityConstraint visibilityConstraint : visibilityConstraints) {
            result += "\n\tVisibility: " + visibilityConstraint;
        }
        for (AlignmentConstraint alignmentConstraint : alignmentConstraints) {
            result += "\n\t" + alignmentConstraint;
        }
        return result;
    }

    public String getxPath() {
        return xPath;
    }

    public void setxPath(String xPath) {
        this.xPath = xPath;
    }

    public ArrayList<AlignmentConstraint> getAlignmentConstraints() {
        return alignmentConstraints;
    }

    public void setAlignmentConstraints(ArrayList<AlignmentConstraint> alignmentConstraints) {
        this.alignmentConstraints = alignmentConstraints;
    }

    public ArrayList<VisibilityConstraint> getVisibilityConstraints() {
        return visibilityConstraints;
    }

    public void setVisibilityConstraints(ArrayList<VisibilityConstraint> visibilityConstraints) {
        this.visibilityConstraints = visibilityConstraints;
    }
}
