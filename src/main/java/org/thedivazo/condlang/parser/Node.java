package org.thedivazo.condlang.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@RequiredArgsConstructor
public abstract class Node implements Serializable {
    @Getter
    protected final String nodeName;

    /**
     * Устанавливает дочерние узлы.
     * @param nodes множество узлов.
     * @return Возвращает статус установки узла. Если все узлы установлены, возвращает true
     */
    public abstract boolean setNodes(Node... nodes);


    /**
     * @return Возвращает неизменяемый список узлов, в котором гарантированно нет одинаковых элементов.
     */
    public abstract List<Node> getChildrenNodes();

    @Override
    public String toString() {
        return nodeName;
    }
}
