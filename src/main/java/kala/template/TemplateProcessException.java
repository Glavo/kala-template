package kala.template;

public class TemplateProcessException extends RuntimeException {
    public TemplateProcessException() {
    }

    public TemplateProcessException(String message) {
        super(message);
    }

    public TemplateProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateProcessException(Throwable cause) {
        super(cause);
    }

    public TemplateProcessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
