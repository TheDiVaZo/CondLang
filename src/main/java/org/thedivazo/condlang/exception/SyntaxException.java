package org.thedivazo.dicesystem.parserexpression.exception;

public class SyntaxException extends CompileException {


    /**
     * @param message Сообщение ошибки.
     * @param position Позиция данного токена в коде
     * @param invalidCode Код, где содержится неизвестный токен.
     */
    public SyntaxException(String message, int position, String invalidCode) {
        super(message, position, invalidCode);
    }

}
