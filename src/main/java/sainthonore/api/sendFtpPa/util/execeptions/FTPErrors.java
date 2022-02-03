package sainthonore.api.sendFtpPa.util.execeptions;

public class FTPErrors extends Exception {

    private ErrorMessage errorMessage;

    public FTPErrors(ErrorMessage errorMessage) {
        super(errorMessage.getErrormessage());
    }

    public ErrorMessage getErrorMessage() {
        return errorMessage;
    }
}
