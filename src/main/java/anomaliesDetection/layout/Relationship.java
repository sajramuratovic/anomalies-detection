package anomaliesDetection.layout;

public abstract class Relationship {

    public abstract Element getNode1();

    public abstract Element getNode2();

    /**
     * Check is a approximately equal to b with the offset delta
     *
     * @param a
     * @param b
     * @param delta
     * @return boolean value
     */
    protected boolean equals(int a, int b, int delta) {
        if (a <= b + delta && a >= b - delta) {
            return true;
        }
        return false;
    }
}
