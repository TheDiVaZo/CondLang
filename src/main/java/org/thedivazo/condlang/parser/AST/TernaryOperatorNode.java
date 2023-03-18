package org.thedivazo.condlang.parser.AST;

import org.thedivazo.condlang.parser.Node;

public class TernaryOperatorNode extends OperatorNode {
    public TernaryOperatorNode(String nodeName) { super(nodeName); }

    @Override
    public boolean setNodes(Node... nodes) {
        if(nodes.length!=3) throw new IllegalArgumentException(String.format("A ternary operator \"%s\" must have 3 arguments.", nodeName));
        return super.setNodes(nodes);
    }
}
