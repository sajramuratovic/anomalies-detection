package anomaliesDetection.layout;

public class ParentChild extends Relationship {

    Element parent, child;
    boolean topJust;
    boolean middleJust;
    boolean bottomJust;
    boolean leftJust;
    boolean centreJust;
    boolean rightJust;
    boolean hFill, vFill;
    int dH_delta = 5, dW_delta = 5;

    public Element getNode1() {
        return parent;
    }

    public Element getNode2() {
        return child;
    }

    public ParentChild(Element parent, Element child) {
        this.parent = parent;
        this.child = child;
        assignAttributes();
    }

    private void assignAttributes() {

        int px = (parent.x1 + parent.x2) / 2;
        int py = (parent.y1 + parent.y2) / 2;
        int cx = (child.x1 + child.x2) / 2;
        int cy = (child.y1 + child.y2) / 2;

        int dW = 3;
        int dH = 3;

        if (equals(px, cx, dW_delta) && equals(child.x1, parent.x1, dW_delta) && equals(child.x2, parent.x2, dW_delta)) {
            sethFill(true);
        } else {
            if (equals(px, cx, dW)) {
                setCentreJust(true);
            } else if (equals(child.x1, parent.x1, dW)) {
                setLeftJust(true);
            } else if (equals(child.x2, parent.x2, dW)) {
                setRightJust(true);
            }
        }

        if (equals(py, cy, dH_delta) && equals(child.y1, parent.y1, dH_delta) && equals(child.y2, parent.y2, dH_delta)) {
            setvFill(true);
        } else {
            if (equals(py, cy, dH)) {
                setMiddleJust(true);
            } else if (equals(child.y1, parent.y1, dH)) {
                setTopJust(true);
            } else if (equals(child.y2, parent.y2, dH)) {
                setBottomJust(true);
            }
        }
    }

    public String getKey() {
        return parent.getXpath() + " contains " + child.getXpath() + generateAttributeSet();
    }

    private String generateAttributeSet() {

        String result = " {";

        if (isCentreJust()) {
            result += "centered";
        }
        if (isLeftJust()) {
            result += "leftJust";
        }
        if (isRightJust()) {
            result += "rightJust";
        }
        if (isMiddleJust()) {
            result += "middle";
        }
        if (isTopJust()) {
            result += "top";
        }
        if (isBottomJust()) {
            result += "bottom";
        }

        return result + "}";
    }

    public boolean[] generateAttributeArray() {
        return new boolean[]{centreJust, leftJust, rightJust, middleJust, topJust, bottomJust};
    }

    public String toString() {
        return getKey();
    }

    public boolean ishFill() {
        return hFill;
    }

    public void sethFill(boolean hFill) {
        this.hFill = hFill;
    }

    public boolean isvFill() {
        return vFill;
    }

    public void setvFill(boolean vFill) {
        this.vFill = vFill;
    }

    public boolean isTopJust() {
        return topJust;
    }

    public void setTopJust(boolean topJust) {
        this.topJust = topJust;
    }

    public boolean isMiddleJust() {
        return middleJust;
    }

    public void setMiddleJust(boolean middleJust) {
        this.middleJust = middleJust;
    }

    public boolean isBottomJust() {
        return bottomJust;
    }

    public void setBottomJust(boolean bottomJust) {
        this.bottomJust = bottomJust;
    }

    public boolean isLeftJust() {
        return leftJust;
    }

    public void setLeftJust(boolean leftJust) {
        this.leftJust = leftJust;
    }

    public boolean isCentreJust() {
        return centreJust;
    }

    public void setCentreJust(boolean centreJust) {
        this.centreJust = centreJust;
    }

    public boolean isRightJust() {
        return rightJust;
    }

    public void setRightJust(boolean rightJust) {
        this.rightJust = rightJust;
    }
}
