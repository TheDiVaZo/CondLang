package org.thedivazo.condlang.parser.AST;

import org.thedivazo.condlang.parser.Node;

import java.util.List;

public class ConditionNode extends Node {

    public ConditionNode(String nodeName) {
        super(nodeName);
    }


    /**
     * @param nodes множество узлов.
     * @return Всегда возвращает {@link UnsupportedOperationException}, так как узел условий хранит только свое имя, которые и является набором условий.
     */
    @Override
    public boolean setNodes(Node... nodes) {
        throw new UnsupportedOperationException("You cannot add nodes to a condition.");
    }

    /**
     * @return Всегда возвращает {@link UnsupportedOperationException}, так как узел условий хранит только свое имя, которые и является набором условий.
     */
    @Override
    public List<Node> getChildrenNodes() {
        throw new UnsupportedOperationException("You cannot get nodes to a condition.");
    }
}
