package sillelien.tutum;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TutumException extends RuntimeException {

    public TutumException() {
        super();
    }

    public TutumException(String message) {
        super(message);
    }

    public TutumException(String message, Throwable cause) {
        super(message, cause);
    }

    public TutumException(Throwable cause) {
        super(cause);
    }

    protected TutumException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TutumException(String message, int status) {
        super(status + ": " + message);
    }
}
