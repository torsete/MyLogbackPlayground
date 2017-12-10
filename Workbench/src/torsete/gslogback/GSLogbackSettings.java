package torsete.gslogback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ansvar: Facade til de settings vi kan bruge til Logback-konfiguration
 */
public class GSLogbackSettings {
    private final static Logger log = LoggerFactory.getLogger(GSLogbackSettings.class);

    public static final String GSLOG_HOST_NAME = "gslog.hostName";
    public static final String GSLOG_CONTEXT_NAME = "gslog.contextName";
    public static final String GSLOG_DATABASE = "gslog.database";
    public static final String GSLOG_ENVIRONMENT = "gslog.environment";
    public static final String GSLOG_VERSION = "gslog.version";
    public static final String GSLOG_APPLICATION = "gslog.application";
    public static final String GSLOG_ADDITIONAL_FILE_NAME = "gslog.additionalFileName";
    public static final String GSLOG_FILE_NAME_TIMSTAMP = "gslog.FileNameTimestamp";
    public static final String GSLOG_SYSTEM = "gslog.system";
    public static final String GSLOG_USER = "gslog.user";
    public static final String GSLOG_HOME = "gslog.home";
    public static final String GSLOG_SERVER = "gslog.server";
    public static final String GSLOG_FILE_NAME = "gslog.fileName";
    public static final String GSLOG_FOLDER_NAME = "gslog.folderName";
    public static final String GSLOG_ABSOLUTE_FILE_NAME = "gslog.absoluteFileName";
    public static final String GSLOG_FILE_NAME_EXTENSION = "gslog.fileNameExtension";
    public static final String GSLOG_ERROR_FILE_NAME_EXTENSION = "gslog.errorFileNameExtension";


    public String getHostName() {
        return get(GSLOG_HOST_NAME);
    }

    public String getContextName() {
        return get(GSLOG_CONTEXT_NAME);
    }

    public String getFileName() {
        return get(GSLOG_FILE_NAME);
    }

    public String getFolderName() {
        return get(GSLOG_FOLDER_NAME);
    }

    public GSLogbackSettings setDatabase(String database) {
        return set(GSLOG_DATABASE, database);
    }

    public GSLogbackSettings resetDatabase() {
        return reset(GSLOG_DATABASE);
    }

    public String getDatabase() {
        return get(GSLOG_DATABASE);
    }

    public GSLogbackSettings setEnvironment(String environment) {
        return set(GSLOG_ENVIRONMENT, environment);
    }

    public String getEnvironment() {
        return get(GSLOG_ENVIRONMENT);
    }

    public GSLogbackSettings setVersion(String version) {
        return set(GSLOG_VERSION, version);
    }

    public String getVersion() {
        return get(GSLOG_VERSION);
    }

    public GSLogbackSettings setApplication(String application) {
        return set(GSLOG_APPLICATION, application);
    }

    public String getApplication() {
        return get(GSLOG_APPLICATION);
    }

    public GSLogbackSettings setAdditionalFilename(String additionalFilename) {
        return set(GSLOG_ADDITIONAL_FILE_NAME, additionalFilename);
    }

    public String getAdditionalFilename() {
        return get(GSLOG_ADDITIONAL_FILE_NAME);
    }

    public GSLogbackSettings setSystem(String system) {
        return set(GSLOG_SYSTEM, system);
    }

    public String getSystem() {
        return get(GSLOG_SYSTEM);
    }

    public GSLogbackSettings setFileNameTimestamp(String timestamp) {
        return set(GSLOG_FILE_NAME_TIMSTAMP, timestamp);
    }

    public GSLogbackSettings resetFileNameTimestamp() {
        return reset(GSLOG_FILE_NAME_TIMSTAMP);
    }

    public String getFileNameTimestamp() {
        return get(GSLOG_FILE_NAME_TIMSTAMP);
    }

    public GSLogbackSettings setUser(String user) {
        return set(GSLOG_USER, user);
    }

    public String getUser() {
        return get(GSLOG_USER);
    }

    public GSLogbackSettings resetUser() {
        return reset(GSLOG_USER);
    }


    public GSLogbackSettings setHome(String home) {
        return set(GSLOG_HOME, home);
    }

    public String getHome() {
        return get(GSLOG_HOME);
    }

    public GSLogbackSettings setServer(String server) {
        return set(GSLOG_SERVER, server);
    }

    public String getServer() {
        return get(GSLOG_SERVER);
    }


    public GSLogbackSettings setAbsoluteFilename(String absoluteFileName) {
        return set(GSLOG_ABSOLUTE_FILE_NAME, absoluteFileName);
    }

    public String getAbsoluteFilename() {
        return get(GSLOG_ABSOLUTE_FILE_NAME);
    }

    public GSLogbackSettings setErrorFilenameExtension(String errorFilenameExtension) {
        return set(GSLOG_ERROR_FILE_NAME_EXTENSION, errorFilenameExtension);
    }

    public String getErrorFilenameExtension() {
        return get(GSLOG_ERROR_FILE_NAME_EXTENSION);
    }

    public GSLogbackSettings setFilenameExtension(String filenameExtension) {
        return set(GSLOG_FILE_NAME_EXTENSION, filenameExtension);
    }

    public String getFilenameExtension() {
        return get(GSLOG_FILE_NAME_EXTENSION);
    }

    GSLogbackSettings set(String name, String value) {
        if (value == null || value.trim().length() == 0) {
            throw new IllegalArgumentException("Property '" + name + "' is assigned an empty value. You may use 'reset' to assign the default value");
        }
        System.setProperty(name, value);
        return this;
    }

    GSLogbackSettings reset(String name) {
        System.clearProperty(name);
        return this;
    }

    String get(String name) {
        return System.getProperty(name);
    }

}
