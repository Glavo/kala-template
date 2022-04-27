package kala.template;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public final class TemplateEngine {

    public static final String DEFAULT_BEGIN_TAG = "${";
    public static final String DEFAULT_END_TAG = "}";

    private final String beginTag;
    private final String endTag;
    private final ErrorMode errorMode;

    TemplateEngine(String beginTag, String endTag, ErrorMode errorMode) {
        this.beginTag = beginTag;
        this.endTag = endTag;
        this.errorMode = errorMode;
    }

    private static final TemplateEngine defaultEngine =
            new TemplateEngine(DEFAULT_BEGIN_TAG, DEFAULT_END_TAG, ErrorMode.DEFAULT);

    public static TemplateEngine getDefault() {
        return defaultEngine;
    }

    public static TemplateEngine.Builder builder() {
        return new Builder();
    }

    private boolean search(Reader input, Appendable output, String tag, char tag0) throws IOException {
        mainLoop:
        while (true) {
            char ch;
            {
                int c = input.read();
                if (c == -1) {
                    break;
                }

                ch = (char) c;
            }

            if (ch == tag0) {
                int n = 1;

                while (n < tag.length()) {
                    int c = input.read();
                    if (c == -1 || c != tag.charAt(n)) {
                        output.append(tag, 0, n);
                        if (c != -1) {
                            output.append((char) c);
                        }
                        continue mainLoop;
                    }
                    n++;
                }

                return true;
            }

            output.append(ch);
        }

        return false;
    }

    public void process(Reader input, Appendable output, Function<? super String, ?> mapper) throws IOException, TemplateProcessException {
        final char beginTag0 = beginTag.charAt(0);
        final char endTag0 = endTag.charAt(0);

        StringBuilder markerBuilder = new StringBuilder();

        while (search(input, output, beginTag, beginTag0)) {
            if (search(input, markerBuilder, endTag, endTag0)) {
                String marker = markerBuilder.toString();
                markerBuilder.setLength(0);

                Object v = mapper.apply(marker);
                if (v != null) {
                    output.append(v.toString());
                } else {
                    switch (errorMode) {
                        case THROW:
                            throw new TemplateProcessException("unknown tag: " + marker);
                        case SANITIZE:
                            output.append(beginTag).append(marker).append(endTag);
                            break;
                        case STRIP:
                            // do nothing
                            break;
                        default:
                            throw new AssertionError();
                    }
                }
            } else {
                throw new TemplateProcessException("Missing the end tag");
            }
        }
    }

    public void process(Reader input, Appendable output, Map<? super String, ?> markerTable) throws IOException {
        process(input, output, markerTable::get);
    }

    public void process(String input, Appendable output, Function<? super String, ?> mapper) throws IOException {
        process(new StringReader(input), output, mapper);
    }

    public void process(String input, Appendable output, Map<? super String, ?> markerTable) throws IOException {
        process(new StringReader(input), output, markerTable);
    }

    public void process(Path input, Path output, Function<? super String, ?> mapper) throws IOException {
        try (Reader reader = Files.newBufferedReader(input);
             Writer writer = Files.newBufferedWriter(output)) {
            process(reader, writer, mapper);
        }
    }

    public void process(Path input, Path output, Map<? super String, ?> markerTable) throws IOException {
        try (Reader reader = Files.newBufferedReader(input);
             Writer writer = Files.newBufferedWriter(output)) {
            process(reader, writer, markerTable);
        }
    }

    public String process(String input, Function<? super String, ?> mapper) {
        StringBuilder res = new StringBuilder();
        try {
            process(new StringReader(input), res, mapper);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return res.toString();
    }

    public String process(String input, Map<? super String, ?> markerTable) {
        StringBuilder res = new StringBuilder();
        try {
            process(new StringReader(input), res, markerTable);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return res.toString();
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
        private String beginTag = DEFAULT_BEGIN_TAG;
        private String endTag = DEFAULT_END_TAG;
        private ErrorMode errorMode = ErrorMode.DEFAULT;

        public Builder beginTag(String marker) {
            if (marker.isEmpty()) {
                throw new IllegalArgumentException("Tag cannot be empty");
            }

            this.beginTag = marker;
            return this;
        }

        public Builder endTag(String marker) {
            if (marker.isEmpty()) {
                throw new IllegalArgumentException("Tag cannot be empty");
            }

            this.endTag = marker;
            return this;
        }

        /**
         * A convenient way to set the {@link #beginTag} and {@link #endTag}.
         *
         * @see #beginTag(String)
         * @see #endTag(String)
         */
        public Builder tag(String beginTag, String endTag) {
            if (beginTag.isEmpty() || endTag.isEmpty()) {
                throw new IllegalArgumentException("Tag cannot be empty");
            }

            this.beginTag = beginTag;
            this.endTag = endTag;
            return this;
        }

        public Builder errorMode(ErrorMode errorMode) {
            Objects.requireNonNull(errorMode);

            this.errorMode = errorMode;
            return this;
        }

        public TemplateEngine build() {
            return new TemplateEngine(beginTag, endTag, errorMode);
        }
    }
}
