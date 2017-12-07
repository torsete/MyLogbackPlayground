package torsete.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ansvar: Facade til de settings vi kan bruge til Logback-konfiguration
 */
public class GSLogbackSettings {
    private final static Logger log = LoggerFactory.getLogger(GSLogbackSettings.class);

    public GSLogbackSettings setDatabase(String database) {
        return set("gslog.database", database);
    }

    public String getDatabase() {
        return get("gslog.database");
    }

    public GSLogbackSettings setEnvironment(String environment) {
        return set("gslog.environment", environment);
    }

    public String getEnvironment() {
        return get("gslog.environment");
    }

    public GSLogbackSettings setVersion(String version) {
        return set("gslog.version", version);
    }

    public String getVersion() {
        return get("gslog.version");
    }

    public GSLogbackSettings setApplication(String application) {
        return set("gslog.application", application);
    }

    public String getApplication() {
        return get("gslog.application");
    }

    public GSLogbackSettings setAdditionalFilename(String additionalFilename) {
        return set("gslog.additionalFilename", additionalFilename);
    }

    public String getAdditionalFilename() {
        return get("gslog.additionalFilename");
    }

    public GSLogbackSettings setSystem(String system) {
        return set("gslog.system", system);
    }

    public String getSystem() {
        return get("gslog.system");
    }

    public GSLogbackSettings setTimestamp(String timestamp) {
        return set("gslog.timestamp", timestamp);
    }

    public String getTimestamp() {
        return get("gslog.timestamp");
    }

    public GSLogbackSettings setUser(String user) {
        return set("gslog.user", user);
    }

    public String getUser() {
        return get("gslog.user");
    }

    public GSLogbackSettings setHome(String home) {
        return set("gslog.home", home);
    }

    public String getHome() {
        return get("gslog.home");
    }

    public GSLogbackSettings setServer(String server) {
        return set("gslog.server", server);
    }

    public String getServer() {
        return get("gslog.server");
    }

    public GSLogbackSettings setAbsoluteFilename(String absoluteFilename) {
        return set("gslog.absoluteFilename", absoluteFilename);
    }

    public String getAbsoluteFilename() {
        return get("gslog.absoluteFilename");
    }

    public GSLogbackSettings setErrorFilenameSuffix(String errorFilenameSuffix) {
        return set("gslog.errorFilenameSuffix", errorFilenameSuffix);
    }

    public String getErrorFilenameSuffix() {
        return get("gslog.errorFilenameSuffix");
    }

    public GSLogbackSettings setFilenameSuffix(String filenameSuffix) {
        return set("gslog.filenameSuffix", filenameSuffix);
    }

    public String getFilenameSuffix() {
        return get("gslog.filenameSuffix");
    }

//    public boolean isInitial() {
//        return get("gslog.started") == null;
//    }
//
//    public GSLogSettings setInitial(boolean isInitial) {
//        return set("gslog.started", isInitial ? null : "no");
//    }

    private GSLogbackSettings set(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
        return this;
    }

    private String get(String name) {
        return System.getProperty(name);
    }


}
