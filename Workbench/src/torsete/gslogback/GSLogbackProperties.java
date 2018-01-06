package torsete.gslogback;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Ansvar: Facade til de settings vi kan bruge til Logback-konfiguration
 */
public enum GSLogbackProperties {
    LOGBACK_CONFIGURATUTON_FILE(true, "logback.configurationFile"),
    GSLOG_CONFIGURATION_FOLDER(true, "gslog.configurationFolder"),
    GSLOG_HOST_NAME(false, "gslog.hostName"),
    GSLOG_ROOT_LEVEL(true, "gslog.rootLevel"),
    GSLOG_CONTEXT_NAME(false, "gslog.contextName"),
    GSLOG_DATABASE(true, "gslog.database"),
    GSLOG_ENVIRONMENT(true, "gslog.environment"),
    GSLOG_VERSION(true, "gslog.version"),
    GSLOG_APPLICATION(true, "gslog.application"),
    GSLOG_ADDITIONAL_FILE_NAME(true, "gslog.additionalFileName"),
    GSLOG_FILE_NAME_TIMSTAMP(true, "gslog.fileNameTimestamp"),
    GSLOG_SYSTEM(true, "gslog.system"),
    GSLOG_USER(true, "gslog.user"),
    GSLOG_HOME(true, "gslog.home"),
    GSLOG_SERVER(true, "gslog.server"),
    GSLOG_FILE_NAME(true, "gslog.fileName"),
    GSLOG_FOLDER_NAME(true, "gslog.folderName"),
    GSLOG_ABSOLUTE_FILE_NAME(false, "gslog.absoluteFileName"),
    GSLOG_FILE_NAME_EXTENSION(true, "gslog.fileNameExtension"),
    GSLOG_ERROR_FILE_NAME_EXTENSION(true, "gslog.errorFileNameExtension"),
    GSLOG_PATTERN(true, "gslog.pattern");

    private String propertyKey;
    private String defaultValue;
    private boolean isApplicationConfigurable; // False hvis det er en beregnet værdi

    GSLogbackProperties(boolean isApplicationConfigurable, String propertyKey, String defaultValue) {
        this.isApplicationConfigurable = isApplicationConfigurable;
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
    }

    GSLogbackProperties(String propertyKey, String defaultValue) {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
    }

    GSLogbackProperties(boolean isApplicationConfigurable, String propertyKey) {
        this.isApplicationConfigurable = isApplicationConfigurable;
        this.propertyKey = propertyKey;
    }

    public GSLogbackProperties setValue(String value) {
        if (!isApplicationConfigurable) {
            throw new IllegalArgumentException(name() + " er en beregnet værdi og må ikke ændres her");
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

    public GSLogbackProperties resetValue() {
        return defaultValue == null ? clearValue() : setValue(defaultValue);
    }

    public boolean isApplicationConfigurable() {
        return isApplicationConfigurable;
    }

    @Override
    public String toString() {
        return getKey() + "=" + getValue();
    }

    public static void clearAll() {
        Arrays.stream(values()).forEach(v -> v.clearValue());

    }

    public static void resetAll() {
        Arrays.stream(values()).forEach(v -> v.resetValue());
    }

    public static String toStrings() {
        return Arrays.stream(values())
                .map(v -> v.toString())
                .collect(Collectors.joining("\n"));
    }


    public static String toVmArguments() {
        return Arrays.stream(values())
                .filter(v -> v.isApplicationConfigurable)
                .map(v -> "-D" + v.getKey() + "=" + (v.getValue() == null ? "" : v.getValue()))
                .collect(Collectors.joining("\n"));
    }

    public static void set(String propertyKey, String value) {
        Optional<GSLogbackProperties> first = Arrays.stream(values()).filter(v -> v.getKey().equals(propertyKey)).findFirst();
        GSLogbackProperties v = first.get();
        System.setProperty(v.getKey(), value);
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