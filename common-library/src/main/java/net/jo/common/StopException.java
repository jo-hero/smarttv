package net.jo.common;

public class StopException extends Exception {
    private static final long serialVersionUID = 2130103808005760998L;

    public StopException(String str) {
        super(str);
    }
}
