package torsete.gslogback;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Ansvar: Facade til de settings vi kan bruge til Logback-konfiguration
 */
public enum GSLogbackProperties {
    LOGBACK_CONFIGURATION_FILE("logback.configurationFile"),
    GSLOG_ROOT_LEVEL("gslog.rootLevel"),
    GSLOG_CONSOLE("gslog.console", v -> v.equals("true") || v.equals("false")),
    GSLOG_FILE("gslog.file", v -> v.equals("true") || v.equals("false")),
    GSLOG_DATABASE("gslog.database"),
    GSLOG_ENVIRONMENT("gslog.environment"),
    GSLOG_VERSION("gslog.version"),
    GSLOG_APPLICATION("gslog.application"),
    GSLOG_ADDITIONAL_FILE_NAME("gslog.additionalFileName"),
    GSLOG_FILE_NAME_TIMSTAMP("gslog.fileNameTimestamp"),
    GSLOG_SYSTEM("gslog.system"),
    GSLOG_USER("gslog.user"),
    GSLOG_HOME("gslog.home"),
    GSLOG_INCIDENT_HOME("gslog.incidentHome"),
    GSLOG_SERVER("gslog.server"),
    GSLOG_FILE_NAME_EXTENSION("gslog.fileNameExtension"),
    GSLOG_ERROR_FILE_NAME_EXTENSION("gslog.errorFileNameExtension"),
    GSLOG_INCIDENT_FILE_NAME_EXTENSION("gslog.incidentFileNameExtension"),
    GSLOG_PATTERN("gslog.pattern"),
    GSLOG_HOST_NAME("gslog.hostName", v -> false),
    GSLOG_CONTEXT_NAME("gslog.contextName", v -> false),
    GSLOG_FILE_NAME("gslog.fileName", v -> false),
    GSLOG_FOLDER_NAME("gslog.folderName", v -> false),
    GSLOG_INCIDENT_FOLDER_NAME("gslog.incidentFolderName", v -> false);


    private String propertyKey;
    private Predicate<String> isValidPredicate;


    GSLogbackProperties(String propertyKey, Predicate<String> isValidPredicate) {
        this.propertyKey = propertyKey;
        this.isValidPredicate = isValidPredicate;
    }

    GSLogbackProperties(String propertyKey) {
        this(propertyKey, v -> true);
    }


    public GSLogbackProperties setValue(String value) {
        if (!isValidPredicate.test(value)) {
            throw new IllegalArgumentException(name() + " kan ikke tildeles værdier '" + value + "'");
        }
        if (value == null || value.trim().length() == 0) {
            System.clearProperty(propertyKey);
        } else {
            System.setProperty(propertyKey, value);
        }
        return this;
    }

    public String getValue() {
        return System.getProperty(propertyKey);
    }

    public String getKey() {
        return propertyKey;
    }

    public GSLogbackProperties clearValue() {
        System.clearProperty(propertyKey);
        return this;
    }

    public boolean isValid() {
        return isValidPredicate.test(getValue());
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }


    public static File getLogFile() {
        return new File(GSLOG_FOLDER_NAME.getValue() + File.separator + GSLOG_FILE_NAME.getValue() + GSLOG_FILE_NAME_EXTENSION.getValue());
    }

    public static File getErrorLogFile() {
        return new File(GSLOG_FOLDER_NAME.getValue() + File.separator + GSLOG_FILE_NAME.getValue() + GSLOG_ERROR_FILE_NAME_EXTENSION.getValue());
    }

    public static void clear() {
        Arrays.stream(values()).forEach(v -> v.clearValue());

    }

    public static String toStrings() {
        return Arrays.stream(values())
                .map(v -> v.toString())
                .collect(Collectors.joining("\n"));
    }

    public static String toStringsAll() {
        return toStrings() + "\n"
                + System.getProperties().entrySet().stream()
//                .filter(e -> e.getKey().toString().startsWith("gslog.") || e.getKey().toString().startsWith("logback."))
                .filter(e -> get(e.getKey().toString()) == null)
                .map(e -> "Unknown property: " + e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()))
                .collect(Collectors.joining("\n"));
    }

    public static String toVmArguments() {
        return Arrays.stream(values())
                .filter(v -> v.isValid())
                .map(v -> "-D" + v.getKey() + "=" + (v.getValue() == null ? "" : v.getValue()))
                .collect(Collectors.joining("\n"));
    }

    public static void set(String propertyKey, String value) {
        GSLogbackProperties k = get(propertyKey);
        System.setProperty(k.getKey(), value); // If null - NPE Exception
    }

    public static GSLogbackProperties get(String propertyKey) {
        Optional<GSLogbackProperties> first = Arrays.stream(values()).filter(v -> v.getKey().equals(propertyKey)).findFirst();
        return first.isPresent() ? first.get() : null;
    }

    public static Properties exportProperties() {
        Properties properties = new Properties();
        Arrays.stream(values()).filter(v -> v.getValue() != null).forEach(v -> properties.put(v.getKey(), v.getValue()));
        return properties;
    }

    public static void importProperties(Properties properties) {
        properties.entrySet().stream().forEach(e -> set(e.getKey().toString(), e.getValue().toString()));
    }

}