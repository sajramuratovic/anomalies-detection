package anomaliesDetection.layout;

import java.awt.*;

public class Sibling extends Relationship {

    Element node1, node2;
    int deltaW = 3, deltaH = 3;
    boolean leftOf;
    boolean rightOf;
    boolean above;
    boolean below;
    boolean leftEdge;
    boolean rightEdge;
    boolean topEdge;
    boolean bottomEdge;
    boolean overlapping;
    boolean xMid;
    boolean yMid;

    public Element getNode1() {
        return node1;
    }

    public Element getNode2() {
        return node2;
    }

    public Sibling(Element n1, Element n2) {
        node1 = n1;
        node2 = n2;
        assignAttributes();
    }

    private void assignAttributes() {

        int midx1 = (node1.x2 + node1.x1) / 2;
        int midx2 = (node2.x2 + node2.x1) / 2;
        int midy1 = (node1.y2 + node1.y1) / 2;
        int midy2 = (node2.y2 + node2.y1) / 2;

        if (equals(node1.x1, node2.x1, deltaW)) {
            setLeftEdge(true);
        }
        if (equals(node1.x2, node2.x2, deltaW)) {
            setRightEdge(true);
        }
        if (equals(node1.y1, node2.y1, deltaH)) {
            setTopEdge(true);
        }
        if (equals(node1.y2, node2.y2, deltaH)) {
            setBottomEdge(true);
        }
        if (equals(midx1, midx2, deltaW)) {
            setxMid(true);
        }
        if (equals(midy1, midy2, deltaH)) {
            setyMid(true);
        }
        if ((midx1 <= node2.x1) && (node1.x2 < node2.x2)) {
            setLeftOf(true);
        }
        if ((node2.x2 <= midx1) && (node1.x1 > node2.x1)) {
            setRightOf(true);
        }
        if (node1.y2 <= midy2) {
            setAbove(true);
        }
        if (node2.y2 <= midy1) {
            setBelow(true);
        }
        setOverLapping(node1, node2);
    }

    public void setOverLapping(Element node1, Element node2) {
        Rectangle r1 = new Rectangle(node1.x1, node1.y1, node1.x2 - node1.x1, node1.y2 - node1.y1);
        Rectangle r2 = new Rectangle(node2.x1, node2.y1, node2.x2 - node2.x1, node2.y2 - node2.y1);
        Rectangle intersection = r1.intersection(r2);

        if (r1.intersects(r2)) {
            if ((intersection.width > 1) && (intersection.height > 1)) {
                setOverlapping(true);
            }
        }
    }

    public String getKey() {
        return node1.getXpath() + " sibling of " + node2.getXpath() + generateAttributeSet();
    }

    public String generateAttributeSet() {

        String result = " {";

        if (above) {
            result += "above";
        }
        if (below) {
            result += "below";
        }
        if (leftOf) {
            result += "leftOf";
        }
        if (rightOf) {
            result += "rightOf";
        }
        if (topEdge) {
            result += "topAlign";
        }
        if (bottomEdge) {
            result += "bottomAlign";
        }
        if (yMid) {
            result += "yMidAlign";
        }
        if (leftEdge) {
            result += "leftAlign";
        }
        if (rightEdge) {
            result += "rightAlign";
        }
        if (xMid) {
            result += "xMidAlign";
        }
        if (overlapping) {
            result += "overlapping";
        }

        return result + "}";
    }

    public String getFlipKey() {
        return node2.getXpath() + " sibling of " + node1.getXpath() + generateFlipAttributeSet();
    }

    private String generateFlipAttributeSet() {

        String result = " {";

        if (above) {
            result += "below";
        }
        if (below) {
            result += "above";
        }
        if (leftOf) {
            result += "rightOf";
        }
        if (rightOf) {
            result += "leftOf";
        }
        if (topEdge) {
            result += "topAlign";
        }
        if (bottomEdge) {
            result += "bottomAlign";
        }
        if (yMid) {
            result += "yMidAlign";
        }
        if (leftEdge) {
            result += "leftAlign";
        }
        if (rightEdge) {
            result += "rightAlign";
        }
        if (xMid) {
            result += "xMidAlign";
        }
        if (overlapping) {
            result += "overlapping";
        }

        return result + "}";
    }

    public boolean[] generateAttributeArray() {
        return new boolean[]{above, below, leftOf, rightOf, topEdge, bottomEdge, yMid, leftEdge, rightEdge, xMid, overlapping};
    }

    public String toString() {
        return getKey();
    }

    public void setNode1(Element node1) {
        this.node1 = node1;
    }

    public void setNode2(Element node2) {
        this.node2 = node2;
    }

    public int getDeltaW() {
        return deltaW;
    }

    public void setDeltaW(int deltaW) {
        this.deltaW = deltaW;
    }

    public int getDeltaH() {
        return deltaH;
    }

    public void setDeltaH(int deltaH) {
        this.deltaH = deltaH;
    }

    public boolean isLeftOf() {
        return leftOf;
    }

    public void setLeftOf(boolean leftOf) {
        this.leftOf = leftOf;
    }

    public boolean isRightOf() {
        return rightOf;
    }

    public void setRightOf(boolean rightOf) {
        this.rightOf = rightOf;
    }

    public boolean isAbove() {
        return above;
    }

    public void setAbove(boolean above) {
        this.above = above;
    }

    public boolean isBelow() {
        return below;
    }

    public void setBelow(boolean below) {
        this.below = below;
    }

    public boolean isLeftEdge() {
        return leftEdge;
    }

    public void setLeftEdge(boolean leftEdge) {
        this.leftEdge = leftEdge;
    }

    public boolean isRightEdge() {
        return rightEdge;
    }

    public void setRightEdge(boolean rightEdge) {
        this.rightEdge = rightEdge;
    }

    public boolean isTopEdge() {
        return topEdge;
    }

    public void setTopEdge(boolean topEdge) {
        this.topEdge = topEdge;
    }

    public boolean isBottomEdge() {
        return bottomEdge;
    }

    public void setBottomEdge(boolean bottomEdge) {
        this.bottomEdge = bottomEdge;
    }

    public boolean isOverlapping() {
        return overlapping;
    }

    public void setOverlapping(boolean overlapping) {
        this.overlapping = overlapping;
    }

    public boolean isxMid() {
        return xMid;
    }

    public void setxMid(boolean xMid) {
        this.xMid = xMid;
    }

    public boolean isyMid() {
        return yMid;
    }

    public void setyMid(boolean yMid) {
        this.yMid = yMid;
    }
}
