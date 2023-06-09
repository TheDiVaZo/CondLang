package org.thedivazo.condlang.parser.AST;

import org.thedivazo.condlang.parser.Node;

public class UnaryOperatorNode extends OperatorNode {

    public UnaryOperatorNode(String nodeName) {
        super(nodeName);
    }

    @Override
    public boolean setNodes(Node... nodes) {
        if(nodes.length!=1) throw new IllegalArgumentException(String.format("A unary operator \"%s\" must have 1 arguments.", nodeName));
        return super.setNodes(nodes);
    }
}
