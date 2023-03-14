package org.thedivazo.dicesystem.parserexpression.exception;

/**
 * Родоначальник всех ошибок в моем парсере.
 */
public class CompileException extends Exception {


    public CompileException(String message) {
        super(message);
    }

    /**
     * Эта функция просто ставит перед знаком "^" нужное кол-во пробелов.
     * @param countSpace кол-во пробелов, которое нужно поставить
     * @return Возвращает строку типа "     ^" которая в контексте ошибки должна указывать на место, где произошла ошибка
     */
    protected static String getSpace(int countSpace) {
        String format = "%"+(countSpace+1)+"s^";
        return String.format(format,"");
    }


    /**
     * @param message Дополнительная информация.
     * @param position Позиция символа в коде, на котором произошла ошибка
     * @param invalidCode Код, в котором произошла ошибка
     */
    public CompileException(String message, int position, String invalidCode) {
        super(
                message + "\n"
                + invalidCode + "\n"
                + getSpace(position)
        );
    }

}
