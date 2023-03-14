package org.thedivazo.dicesystem.parserexpression.parser.AST;

import org.thedivazo.dicesystem.parserexpression.parser.Node;

public class BinaryOperatorNode extends OperatorNode {

    public BinaryOperatorNode(String nodeName) {
        super(nodeName);
    }

    @Override
    public boolean setNodes(Node... nodes) {
        if(nodes.length!=2) throw new IllegalArgumentException("A binary operator must have 2 arguments.");
        return super.setNodes(nodes);
    }
}
