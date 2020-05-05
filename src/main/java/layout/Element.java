package layout;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Element {

    String xpath;
    int x1, x2, y1, y2;
    Element parent;
    ArrayList<Element> children;
    HashMap<String, String> styles;
    int[] boundingCoordinates;
    int[] contentCoordinates;

    /**
     * A Rectangle specifies an area in a coordinate space that is enclosed by the Rectangle object's upper-left point
     * (x,y) in the coordinate space, its width, and its height.
     */
    Rectangle boundingRectangle;
    Rectangle contentRectangle;

    public Element(String x, int x1, int y1, int x2, int y2) {
        this.xpath = x;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        boundingCoordinates = new int[] {x1,y1,x2,y2};
        this.boundingRectangle = new Rectangle(x1, y1, x2-x1, y2 - y1);
    }

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public int getX1() {
        return x1;
    }

    public void setX1(int x1) {
        this.x1 = x1;
    }

    public int getX2() {
        return x2;
    }

    public void setX2(int x2) {
        this.x2 = x2;
    }

    public int getY1() {
        return y1;
    }

    public void setY1(int y1) {
        this.y1 = y1;
    }

    public int getY2() {
        return y2;
    }

    public void setY2(int y2) {
        this.y2 = y2;
    }

    public Element getParent() {
        return parent;
    }

    public void setParent(Element parent) {
        this.parent = parent;
    }

    public ArrayList<Element> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Element> children) {
        this.children = children;
    }

    public HashMap<String, String> getStyles() {
        return styles;
    }

    public void setStyles(HashMap<String, String> styles) {
        this.styles = styles;
    }

    public Rectangle getRectangle() {
        return boundingRectangle;
    }

    public void setRectangle(Rectangle rectangle) {
        this.boundingRectangle = rectangle;
    }

    public int[] getBoundingCoordinates() {
        return boundingCoordinates;
    }

    public void setBoundingCoordinates(int[] boundingCoordinates) {
        this.boundingCoordinates = boundingCoordinates;
    }

    public int[] getContentCoordinates() {
        return contentCoordinates;
    }

    public void setContentCoordinates(int[] contentCoordinates) {
        this.contentCoordinates = contentCoordinates;
    }
}
