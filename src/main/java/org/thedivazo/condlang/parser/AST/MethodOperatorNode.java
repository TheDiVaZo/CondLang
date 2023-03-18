package org.thedivazo.condlang.parser.AST;

import lombok.Getter;
import lombok.Setter;
import org.thedivazo.condlang.parser.Node;

public class MethodOperatorNode extends FunctionOperatorNode {

    @Getter
    @Setter
    protected Node context;

    public MethodOperatorNode(String nodeName) {
        super(nodeName);
    }

    @Override
    public String toString() {
        return super.toString() + "." + context.toString();
    }
}
