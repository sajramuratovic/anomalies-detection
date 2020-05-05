package layout;


import com.infomatiq.jsi.rtree.RTree;
import gnu.trove.TIntProcedure;
import com.infomatiq.jsi.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Layout {

    private RTree rtree;
    HashMap<Integer, Rectangle> rectangles;
    HashMap<Integer, String> xpaths;
    HashMap<String, Element> elements;
    HashMap<String, Relationship> relationships;

    public Layout(RTree rtree, HashMap<Integer, Rectangle> rectangles, HashMap<Integer, String> xpaths, HashMap<String, Element> elements) {
        this.rtree = rtree;
        this.rectangles = rectangles;
        this.xpaths = xpaths;
        this.elements = elements;
        relationships = new HashMap<>();

        extract();
    }

    private void extract() {

        HashMap<Integer, Integer> parentMap = new HashMap<>();
        for (int c = 0; c < elements.size(); c++) {
            java.util.List<Integer> cIDs = getChildren(c);
            for (int i : cIDs) {
                // Check new parent is smaller than existing
                if (parentMap.containsKey(i)) {
                    int currentParent = parentMap.get(i);
                    java.util.List<Integer> tempIds = getChildren(currentParent);
                    if (tempIds.contains(c)) {
                        parentMap.put(i, c);
                    }
                } else {
                    parentMap.put(i, c);
                }

            }
        }

        HashMap<Element, ArrayList<Element>> parents = new HashMap<>();

        for (Integer x : parentMap.keySet()) {
            Integer pId = parentMap.get(x);
            Element p = elements.get(xpaths.get(pId));
            Element c = elements.get(xpaths.get(x));
            if (p != null & c != null) {

                ParentChild pc = new ParentChild(p, c);

                c.setParent(p);
                relationships.put(pc.getKey(), pc);

                if (!parents.containsKey(p)) {
                    parents.put(p, new ArrayList<>());
                }
                parents.get(p).add(c);

            }
        }

        for (ArrayList<Element> sib : parents.values()) {
            List<Element> sibCopy = (ArrayList<Element>) sib.clone();
            while (sibCopy.size() > 0) {
                Element node = sibCopy.remove(0);
                for (Element n : sibCopy) {
                    Sibling sibling = new Sibling(node, n);
                    relationships.put(sibling.getKey(), sibling);
                }
            }
        }
    }

    public List<Integer> getChildren(final int elementId) {
        final ArrayList<Integer> children = new ArrayList<>();
        rtree.contains(rectangles.get(elementId), new TIntProcedure() {
            @Override
            public boolean execute(int i) {
                if (i != elementId) {
                    // Check for children same size as parents
                    if (!xpaths.get(elementId).contains(xpaths.get(i))) {
                        children.add(i);
                    }

                }
                return true;
            }
        });
        return children;
    }

    public RTree getRtree() {
        return rtree;
    }

    public void setRtree(RTree rtree) {
        this.rtree = rtree;
    }

    public HashMap<Integer, Rectangle> getRectangles() {
        return rectangles;
    }

    public void setRectangles(HashMap<Integer, Rectangle> rectangles) {
        this.rectangles = rectangles;
    }

    public HashMap<Integer, String> getXpaths() {
        return xpaths;
    }

    public void setXpaths(HashMap<Integer, String> xpaths) {
        this.xpaths = xpaths;
    }

    public HashMap<String, Element> getElements() {
        return elements;
    }

    public void setElements(HashMap<String, Element> elements) {
        this.elements = elements;
    }

    public HashMap<String, Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(HashMap<String, Relationship> relationships) {
        this.relationships = relationships;
    }
}
