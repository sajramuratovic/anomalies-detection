package xPert.visitors;

import xPert.DomNode;

public abstract class DomVisitor {

    public abstract void visit(DomNode node);

    public void endVisit(DomNode node) {
        // Do nothing
    }
}
