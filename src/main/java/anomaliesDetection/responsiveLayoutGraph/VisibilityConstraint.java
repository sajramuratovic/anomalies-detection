package anomaliesDetection.responsiveLayoutGraph;

public class VisibilityConstraint {

    public int appear;
    public int disappear;

    /**
     * A visibility constraint vc, for some node e ∈ E, is a pair (x1, x2) representing an inclusive range of viewport widths
     * Constructs a new visibility constraint which can be added to a node
     * @param a     the lower bound/appear point of the element
     * @param d     the upper bound/disappear point of the element
     */
    public VisibilityConstraint(int a, int d) {
        this.appear = a;
        this.disappear = d;
    }

    public int getAppear() {
        return appear;
    }

    public void setAppear(int appear) {
        this.appear = appear;
    }

    public int getDisappear() {
        return disappear;
    }

    public void setDisappear(int disappear) {
        this.disappear = disappear;
    }

    @Override
    public String toString() {
        return "VisibilityConstraint{" +
                "x1=" + appear +
                ", x2=" + disappear +
                '}';
    }
}
