package org.thedivazo.dicesystem.parserexpression.exception;

public class ConditionException extends CompileException{
    public ConditionException(String message) {
        super(message);
    }

    public ConditionException(String message, int position, String invalidCode) {
        super(message, position, invalidCode);
    }
}
