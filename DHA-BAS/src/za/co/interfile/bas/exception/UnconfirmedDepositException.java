package za.co.interfile.bas.exception;

public class UnconfirmedDepositException extends Exception {

    static final long serialVersionUID = 0L;
    private String message = null;

    public UnconfirmedDepositException(String message) {
        this.message = message;
    }

    public static long getSerialVersionUID() {
        return 0L;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
