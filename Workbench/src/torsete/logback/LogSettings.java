package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogSettings {

    public LogSettings() {
        setTimestamp(new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date()));
    }

    public LogSettings setDatabase(String database) {
        return set("gslog.database", database);
    }

    public String getDatabase() {
        return get("gslog.database");
    }

    public LogSettings setApplication(String application) {
        return set("gslog.application", application);
    }

    public String getApplication() {
        return get("gslog.application");
    }

    public LogSettings setAdditionalFilename(String additionalFilename) {
        return set("gslog.additionalFilename", additionalFilename);
    }

    public String getAdditionalFilename() {
        return get("gslog.additionalFilename");
    }

    public LogSettings setSystem(String system) {
        return set("gslog.system", system);
    }

    public String getSystem() {
        return get("gslog.system");
    }

    public LogSettings setTimestamp(String timestamp) {
        return set("gslog.timestamp", timestamp);
    }

    public String getTimestamp() {
        return get("gslog.timestamp");
    }

    public LogSettings setUser(String user) {
        return set("gslog.user", user);
    }

    public String getUser() {
        return get("gslog.user");
    }


    public LogSettings activate(boolean reset) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        URL mainWatchURL = ConfigurationWatchListUtil.getMainWatchURL(context);
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            if (reset) {
                context.reset();
            }
            configurator.doConfigure(mainWatchURL);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        return this;
    }

    private LogSettings set(String name, String value) {
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
