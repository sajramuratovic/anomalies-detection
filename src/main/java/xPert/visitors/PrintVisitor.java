package xPert.visitors;

import org.apache.commons.lang3.StringUtils;
import xPert.DomNode;

public class PrintVisitor extends DomVisitor {
    int level = 0;

    @Override
    public void visit(DomNode node) {
        String pad = StringUtils.repeat(" ", level);
        System.out.println(pad + "-" + node);
        level++;
    }

    @Override
    public void endVisit(DomNode node) {
        level--;
        super.endVisit(node);
    }

}
