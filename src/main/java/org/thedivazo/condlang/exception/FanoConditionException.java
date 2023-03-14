package org.thedivazo.dicesystem.parserexpression.exception;

public class FanoConditionException extends CompileException {
    public FanoConditionException(String regEx1, String regEx2) {
        super(String.format("RegEx '%s' and regEx '%s' do not satisfy the Fano condition.", (regEx1), (regEx2)));
    }
}
