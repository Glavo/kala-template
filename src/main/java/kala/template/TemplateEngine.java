package kala.template;

import java.util.Objects;

public final class TemplateEngine {

    public static final String DEFAULT_BEGIN_MARKER = "${";
    public static final String DEFAULT_END_MARKER = "}";

    private String beginMarker = DEFAULT_BEGIN_MARKER;
    private String endMarker = DEFAULT_END_MARKER;
    private ErrorMode errorMode = ErrorMode.DEFAULT;

    TemplateEngine(String beginMarker, String endMarker, ErrorMode errorMode) {
        this.beginMarker = beginMarker;
        this.endMarker = endMarker;
        this.errorMode = errorMode;
    }

    private static final TemplateEngine defaultEngine = new TemplateEngine(DEFAULT_BEGIN_MARKER, DEFAULT_END_MARKER, ErrorMode.DEFAULT);

    public static TemplateEngine defaultEngine() {
        return defaultEngine;
    }

    public static TemplateEngine.Builder builder() {
        return new Builder();
    }

    /**
     * Used to handle unknown tag.
     */
    public enum ErrorMode {

        /**
         * Throw an exception and abort the process.
         */
        THROW,

        /**
         * Transfer unprocessed marks directly to the output.
         */
        SANITIZE,

        /**
         * Ignore the unknown mark.
         */
        STRIP;

        public static final ErrorMode DEFAULT = THROW;
    }

    public static final class Builder {
        private String beginMarker = DEFAULT_BEGIN_MARKER;
        private String endMarker = DEFAULT_END_MARKER;
        private ErrorMode errorMode = ErrorMode.DEFAULT;

        public Builder beginMarker(String marker) {
            if (marker.isEmpty()) {
                throw new IllegalArgumentException("Mark cannot be empty");
            }

            this.beginMarker = marker;
            return this;
        }

        public Builder endMarker(String marker) {
            if (marker.isEmpty()) {
                throw new IllegalArgumentException("Mark cannot be empty");
            }

            this.endMarker = marker;
            return this;
        }

        /**
         * A convenient way to set the {@link #beginMarker} and {@link #endMarker}.
         *
         * @see #beginMarker(String)
         * @see #endMarker(String)
         */
        public Builder marker(String beginMarker, String endMarker) {
            if (beginMarker.isEmpty() || endMarker.isEmpty()) {
                throw new IllegalArgumentException("Mark cannot be empty");
            }

            this.beginMarker = beginMarker;
            this.endMarker = endMarker;
            return this;
        }


        public Builder errorMode(ErrorMode errorMode) {
            Objects.requireNonNull(errorMode);

            this.errorMode = errorMode;
            return this;
        }

        public TemplateEngine build() {
            return new TemplateEngine(beginMarker, endMarker, errorMode);
        }
    }
}
