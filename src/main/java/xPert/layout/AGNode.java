package xPert.layout;

import xPert.DomNode;

import java.util.ArrayList;

public class AGNode {
    int x1, y1, x2, y2;
    long area;
    DomNode domNode;
    AGNode parent;
    ArrayList<Contains> childrenEdges;

    public AGNode(DomNode domNode) {
        this.domNode = domNode;
        int[] c = domNode.getCoords();
        x1 = c[0];
        y1 = c[1];
        x2 = c[2];
        y2 = c[3];
        area = (x2 - x1) * (y2 - y1);
        childrenEdges = new ArrayList<>();
    }

    public long getArea() {
        return area;
    }

    public boolean contains(AGNode n) {
        if (this.x1 <= n.x1 && this.y1 <= n.y1
                && this.x2 >= n.x2 && this.y2 >= n.y2) {
            return true;
        }
        return false;
    }

    public String toString() {
        return domNode.getxPath();
    }

    ;
}
