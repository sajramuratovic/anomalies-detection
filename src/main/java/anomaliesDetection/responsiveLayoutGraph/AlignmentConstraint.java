package anomaliesDetection.responsiveLayoutGraph;

/**
 * Given a pair of RLG nodes e1, e2 ∈ E, an alignment constraint ac is a 4-tuple (x1, x2, t, P) representing that between
 * the inclusive viewport widths of x1 and x2, e1 and e2 are laid out in a relationship of type t, with alignment attributes P.
 */
public class AlignmentConstraint {

    public Node node1, node2;
    public Type type;
    int min, max;
    //initialize all the attributes elements to boolean false
    boolean[] attributes;
    boolean[] fills;

    public AlignmentConstraint(Node node1, Node node2, Type type, int min, int max, boolean[] attributes, boolean[] fills) {
        this.node1 = node1;
        this.node2 = node2;
        this.type = type;
        this.min = min;
        this.max = max;
        if (type == Type.PARENT_CHILD) {
            this.attributes = new boolean[6];
            this.fills = new boolean[2];
            this.fills = fills;
        } else {
            this.attributes = new boolean[11];
        }
        this.attributes = attributes;
    }

    /**
     * Utility function to generate a full key representing the constraints and it's attributes
     *
     * @return the full key representing the constraint
     */
    public String generateKey() {
        String t = "";
        try {
            if (type == Type.PARENT_CHILD) {
                t = " contains ";
                return node1.xPath + t + node2.xPath + generateLabelling();
            } else {
                t = " sibling of ";
                return node1.xPath + t + node2.xPath + generateLabelling();
            }
        } catch (NullPointerException e) {
        }
        return "";
    }

    /**
     * Utility function to generate a basic key to represent the constraint
     *
     * @return the basic key
     */
    public String generateKeyWithoutLabels() {
        String t = "";
        if (type == Type.PARENT_CHILD) {
            t = " contains ";
            return node2.xPath + t + node1.xPath;
        } else {
            t = " sibling of ";
            return node1.xPath + t + node2.xPath;
        }
    }

    /**
     * Utility function which uses the set of alignment attributes to generate the labelling string for the key.
     * Labeling is used to describe the location of one element in relation to the other and how borders of each element align with each other.
     *
     * @return the labelling string
     */
    public String generateLabelling() {
        String result = " {";
        if (type == Type.PARENT_CHILD) {
            result += setAttributesForPARENT_CHILD(attributes);
        } else {
            result += setAttributesForSIBLINGS(attributes);
        }
        return result + "}";
    }

    /**
     * Child elements may be left (leftJust LF), right (rightJust RJ) or centre-justified (centered CJ)
     * within their parents and may also be top (TJ), bottom (BJ) or middle (MJ).
     *
     * @param attributes
     * @return result
     */
    private String setAttributesForPARENT_CHILD(boolean[] attributes) {
        String result = "";
        if (attributes[0]) {
            result += "centered";
        }
        if (attributes[1]) {
            result += "leftJust";
        }
        if (attributes[2]) {
            result += "rightJust";
        }
        if (attributes[3]) {
            result += "middle";
        }
        if (attributes[4]) {
            result += "top";
        }
        if (attributes[5]) {
            result += "bottom";
        }
        return result;
    }

    /**
     * Sibling attributes fall into two categories, positioning and alignment. Positioning labels describe the
     * location of one element in relation to the other, while alignment labels describe how the borders of each
     * element align with each other.
     *
     * @param attributes
     * @return result
     */
    private String setAttributesForSIBLINGS(boolean[] attributes) {
        String result = "";
        if (attributes[0]) {
            result += "above";
        }
        if (attributes[1]) {
            result += "below";
        }
        if (attributes[2]) {
            result += "leftOf";
        }
        if (attributes[3]) {
            result += "rightOf";
        }
        if (attributes[4]) {
            result += "topAlign";
        }
        if (attributes[5]) {
            result += "bottomAlign";
        }
        if (attributes[6]) {
            result += "yMidAlign";
        }
        if (attributes[7]) {
            result += "leftAlign";
        }
        if (attributes[8]) {
            result += "rightAlign";
        }
        if (attributes[9]) {
            result += "xMidAlign";
        }
        if (attributes[10]) {
            result += "overlapping";
        }
        return result;
    }

    public Node getNode1() {
        return node1;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public Node getNode2() {
        return node2;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean[] getAttributes() {
        return attributes;
    }

    public void setAttributes(boolean[] attributes) {
        this.attributes = attributes;
    }

    public String toString() {
        try {
            return node1.xPath + " , " + node2.xPath + " , " + type + " , " + min + " , " + max + " , " + this.generateLabelling();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return "";
    }
}
