package kala.template;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;

/**
 * The core class of the template processing engine.
 * <p>
 * See <a href="https://github.com/Glavo/kala-template">https://github.com/Glavo/kala-template</a> for detailed documentation.
 */
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

    /**
     * The core method of the template engine.
     * <p>
     * It reads the template from the {@code input} and outputs the generated content to the {@code output}.
     * The {@code mapper} is used to map the marker to the content in the output.
     * <p>
     * The whole process is streaming and does not read the entire template into memory.
     * <p>
     * For ease of use, this method provides many overloads.
     * You can simply use a {@link String} as input, or get a {@link String} directly by omitting to provide output.
     * You can also use {@link Map} or {@link ResourceBundle} instead of mapper, which will automatically use the marker as the key to find the corresponding value.
     *
     * @throws IOException              throws when an exception occurs in input or output
     * @throws TemplateProcessException when a begin tag appears, but there is no corresponding end tag;
     *                                  if the {@link #errorMode} is {@link ErrorMode#THROW}, it will also throw when mapper returns {@code null}
     */
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
                throw new TemplateProcessException("missing the end tag");
            }
        }
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(Reader input, Appendable output, Map<? super String, ?> markerTable) throws IOException {
        process(input, output, markerTable::get);
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(Reader input, Appendable output, ResourceBundle resourceBundle) throws IOException {
        process(input, output, resourceBundle::getObject);
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(String input, Appendable output, Function<? super String, ?> mapper) throws IOException {
        process(new StringReader(input), output, mapper);
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(String input, Appendable output, Map<? super String, ?> markerTable) throws IOException {
        process(new StringReader(input), output, markerTable);
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(String input, Appendable output, ResourceBundle resourceBundle) throws IOException {
        process(new StringReader(input), output, resourceBundle);
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(Path input, Path output, Function<? super String, ?> mapper) throws IOException {
        try (Reader reader = Files.newBufferedReader(input);
             Writer writer = Files.newBufferedWriter(output)) {
            process(reader, writer, mapper);
        }
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(Path input, Path output, Map<? super String, ?> markerTable) throws IOException {
        try (Reader reader = Files.newBufferedReader(input);
             Writer writer = Files.newBufferedWriter(output)) {
            process(reader, writer, markerTable);
        }
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public void process(Path input, Path output, ResourceBundle resourceBundle) throws IOException {
        try (Reader reader = Files.newBufferedReader(input);
             Writer writer = Files.newBufferedWriter(output)) {
            process(reader, writer, resourceBundle);
        }
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
    public String process(String input, Function<? super String, ?> mapper) {
        StringBuilder res = new StringBuilder();
        try {
            process(new StringReader(input), res, mapper);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return res.toString();
    }

    /**
     * @see #process(Reader, Appendable, Function)
     */
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
     * @see #process(Reader, Appendable, Function)
     */
    public String process(String input, ResourceBundle resourceBundle) {
        StringBuilder res = new StringBuilder();
        try {
            process(new StringReader(input), res, resourceBundle);
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

    private static void printHelpMessage(PrintStream writer) throws IOException {
        writer.println("Usage: kala-template [options] <input-file> <output-file>");
        writer.println();
        writer.println("Options:");
        writer.println("  -?, -h, --help    Print this help message");
        writer.println(" --stdin            Use standard  input instead of  input file");
        writer.println(" --stdout           Use standard output instead of output file");
        writer.println("  --begin-tag   <begin tag>");
        writer.println("                    Specify the begin tag, defaults to '${'");
        writer.println("  --end-tag     <end tag>");
        writer.println("                    Specify the end tag,   defaults to  '}'");
        writer.println("  --input-encoding      <encoding>");
        writer.println("                    Specify character encoding used by input file, defaults to UTF-8");
        writer.println("  --output-encoding     <encoding>");
        writer.println("                    Specify character encoding used by output file, defaults to UTF-8");
        writer.println("  --properties-encoding <encoding>");
        writer.println("                    Specify character encoding used by properties file, defaults to UTF-8");
        writer.println("  -e --encoding         <encoding>");
        writer.println("                    Specify character encoding for input file, output file and properties files");
        writer.println("  -D<name>=<value>  set a property");
        writer.println("  -p --properties-file <properties file>");
        writer.println("                    Load properties from the specified properties file");
        writer.println("  -np --no-system-properties");
        writer.println("                    Do not use JVM system properties");
        writer.println("  -ne --no-environment-variables");
        writer.println("                    Do not use environment variables");
    }

    private static String readOptionValue(String opt, Iterator<String> it) {
        if (!it.hasNext()) {
            System.err.println("error: option '" + opt + "' requires an argument");
            System.exit(1);
        }
        return it.next();
    }

    private static Charset nativeCharset() {
        String nativeEncoding = System.getProperty("native.encoding");
        if (nativeEncoding != null) {
            return Charset.forName(nativeEncoding);
        } else {
            return Charset.defaultCharset();
        }
    }

    public static void main(String[] args) throws IOException {
        Iterator<String> it = Arrays.asList(args).iterator();
        if (!it.hasNext()) {
            printHelpMessage(System.err);
            System.exit(1);
        }

        String beginTag = DEFAULT_BEGIN_TAG;
        String endTag = DEFAULT_END_TAG;
        ErrorMode errorMode = ErrorMode.DEFAULT;
        Charset propertiesEncoding = StandardCharsets.UTF_8;
        Charset inputEncoding = null;
        Charset outputEncoding = null;
        boolean stdin = false;
        boolean stdout = false;
        Path inputFile = null;
        Path outputFile = null;
        boolean useSystemProperties = true;
        boolean useEnvironmentVariables = true;

        LinkedHashMap<String, Object> defines = new LinkedHashMap<>();

        while (it.hasNext()) {
            String opt = it.next();
            switch (opt) {
                case "-h":
                case "-help":
                case "--help":
                case "-?":
                    printHelpMessage(System.out);
                    return;
                case "-e":
                case "-encoding":
                case "--encoding":
                    inputEncoding = Charset.forName(readOptionValue(opt, it));
                    outputEncoding = inputEncoding;
                    propertiesEncoding = inputEncoding;
                    break;
                case "-input-encoding":
                case "--input-encoding":
                    inputEncoding = Charset.forName(readOptionValue(opt, it));
                    break;
                case "-output-encoding":
                case "--output-encoding":
                    outputEncoding = Charset.forName(readOptionValue(opt, it));
                    break;
                case "-properties-encoding":
                case "--properties-encoding":
                    propertiesEncoding = Charset.forName(readOptionValue(opt, it));
                    break;
                case "-p":
                case "-properties-file":
                case "--properties-file":
                    Path file = Paths.get(readOptionValue(opt, it));
                    if (!Files.isRegularFile(file)) {
                        System.err.println("error: properties file '" + file + "' not exists");
                        System.exit(1);
                    }

                    try (Reader reader = Files.newBufferedReader(file, propertiesEncoding)) {
                        Properties properties = new Properties();
                        properties.load(reader);
                        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                            defines.put((String) entry.getKey(), entry.getValue());
                        }
                    }
                    break;
                case "-np":
                case "-no-system-properties":
                case "--no-system-properties":
                    useSystemProperties = false;
                    break;
                case "-ne":
                case "-no-environment-variables":
                case "--no-environment-variables":
                    useEnvironmentVariables = false;
                    break;
                case "-error-mode":
                case "--error-mode":
                    String mode = readOptionValue(opt, it);
                    try {
                        errorMode = ErrorMode.valueOf(mode.toUpperCase(Locale.ROOT));
                    } catch (IllegalArgumentException e) {
                        System.err.println("error: unknown error mode '" + mode + "'");
                        System.exit(1);
                    }
                    break;
                case "-begin-tag":
                case "--begin-tag":
                    beginTag = readOptionValue(opt, it);
                    break;
                case "-end-tag":
                case "--end-tag":
                    endTag = readOptionValue(opt, it);
                    break;
                case "-stdin":
                case "--stdin":
                    stdin = true;
                    break;
                case "-stdout":
                case "--stdout":
                    stdout = true;
                    break;
                default:
                    if (opt.startsWith("-D")) {
                        String value = opt.substring("-D".length());
                        int idx = value.indexOf('=');
                        if (idx >= 0) {
                            defines.put(value.substring(0, idx), value.substring(idx + 1));
                        } else {
                            defines.put(value, "");
                        }
                    } else if (inputFile == null && !stdin) {
                        inputFile = Paths.get(opt);
                        if (!Files.isRegularFile(inputFile)) {
                            System.err.println("error: input file not exists");
                            System.exit(1);
                        }
                    } else if (outputFile == null && !stdout) {
                        outputFile = Paths.get(opt);
                    } else {
                        System.err.println("error: unknown option '" + opt + "'");
                        System.exit(1);
                    }
            }
        }

        if (inputFile == null && !stdin) {
            System.err.println("error: missing input file");
            System.exit(1);
        }

        if (outputFile == null && !stdout) {
            System.err.println("error: missing output file");
            System.exit(1);
        }

        final boolean finalUseSystemProperties = useSystemProperties;
        final boolean finalUseEnvironmentVariables = useEnvironmentVariables;
        Function<String, Object> mapper = key -> {
            Object value = defines.get(key);
            if (value == null && finalUseSystemProperties) {
                value = System.getProperty(key);
            }
            if (value == null && finalUseEnvironmentVariables) {
                value = System.getenv(key);
            }
            return value;
        };

        TemplateEngine engine = TemplateEngine.builder()
                .tag(beginTag, endTag)
                .errorMode(errorMode)
                .build();

        if (inputEncoding == null) {
            inputEncoding = stdin ? nativeCharset() : StandardCharsets.UTF_8;
        }

        if (outputEncoding == null) {
            outputEncoding = stdout ? nativeCharset() : StandardCharsets.UTF_8;
        }

        Reader reader = null;
        Writer writer = null;

        try {
            reader = stdin ? new InputStreamReader(System.in, inputEncoding) : Files.newBufferedReader(inputFile, inputEncoding);
            writer = stdout ? new OutputStreamWriter(System.out, outputEncoding) : Files.newBufferedWriter(outputFile, outputEncoding);

            engine.process(reader, writer, mapper);
        } catch (TemplateProcessException e) {
            System.err.println("error: " + e.getMessage());
            System.exit(1);
        } finally {
            if (reader != null && !stdin) {
                try {
                    reader.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            if (writer != null) {
                try {
                    if (stdout)
                        writer.flush();
                    else
                        writer.close();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
