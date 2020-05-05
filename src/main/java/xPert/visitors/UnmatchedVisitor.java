package xPert.visitors;

import xPert.DomNode;

import java.util.ArrayList;
import java.util.List;

public class UnmatchedVisitor extends DomVisitor {

    public UnmatchedVisitor() {
        init();
    }

    List<DomNode> unmatched;

    public void init() {
        unmatched = new ArrayList<>();
    }

    @Override
    public void visit(DomNode node) {
        if (!node.isMatched()) {
            unmatched.add(node);
        }
    }

    public List<DomNode> getUnmatched() {
        return unmatched;
    }

}
