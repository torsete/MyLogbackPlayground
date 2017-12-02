package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LogSettings {

    private final static Logger log = LoggerFactory.getLogger(LogSettings.class);

    private LogSettings() {
    }

    public static LogSettings activate(boolean reset, Consumer<LogSettings> consumer) {
        LogSettings logSettings = new LogSettings();
        consumer.accept(logSettings);
        URL url = ConfigurationWatchListUtil.getMainWatchURL((LoggerContext) LoggerFactory.getILoggerFactory());
        return activate(reset, url);
    }

    public static LogSettings activate(boolean reset, URL url) {
        LogSettings logSettings = new LogSettings();
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(url);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return logSettings.configure(reset, configuratorConsumer);
    }

    public static LogSettings activate(boolean reset, File file) {
        LogSettings logSettings = new LogSettings();
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(file);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return logSettings.configure(reset, configuratorConsumer);
    }

    public static LogSettings activate(boolean reset, String filename) {
        LogSettings logSettings = new LogSettings();
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(filename);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return logSettings.configure(reset, configuratorConsumer);
    }

    public static LogSettings activate(boolean reset, InputStream inputStream) {
        LogSettings logSettings = new LogSettings();
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(inputStream);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return logSettings.configure(reset, configuratorConsumer);
    }


    public LogSettings setDatabase(String database) {
        return set("gslog.database", database);
    }

    public String getDatabase() {
        return get("gslog.database");
    }

    public LogSettings setEnvironment(String environment) {
        return set("gslog.environment", environment);
    }

    public String getEnvironment() {
        return get("gslog.environment");
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

    public LogSettings setHome(String home) {
        return set("gslog.home", home);
    }

    public String getHome() {
        return get("gslog.home");
    }

    public LogSettings setServer(String server) {
        return set("gslog.server", server);
    }

    public String getServer() {
        return get("gslog.server");
    }

    public LogSettings setAbsoluteFilename(String absoluteFilename) {
        return set("gslog.absoluteFilename", absoluteFilename);
    }

    public String getAbsoluteFilename() {
        return get("gslog.absoluteFilename");
    }

    public LogSettings setErrorFilenameSuffix(String errorFilenameSuffix) {
        return set("gslog.errorFilenameSuffix", errorFilenameSuffix);
    }

    public String getErrorFilenameSuffix() {
        return get("gslog.errorFilenameSuffix");
    }

    public LogSettings setFilenameSuffix(String filenameSuffix) {
        return set("gslog.filenameSuffix", filenameSuffix);
    }

    public String getFilenameSuffix() {
        return get("gslog.filenameSuffix");
    }

    public boolean isInitial() {
        return get("gslog.started") == null;
    }

    LogSettings configure(boolean reset, Consumer<JoranConfigurator> configuratorConsumer) {
        boolean wasInitial = isInitial();
        String newSettingsMessage = "New Settings: " + System.getProperties().entrySet().stream()
                .filter(es -> es.getKey().toString().startsWith("gslog."))
                .filter(es -> !es.getKey().toString().equals("gslog.folder"))
                .filter(es -> !es.getKey().toString().equals("gslog.filename"))
                .filter(es -> !es.getKey().toString().equals("gslog.absoluteFilename"))
                .map(es -> es.getKey() + "=" + es.getValue()).collect(Collectors.joining("\n\t", "\n\t", ""));
        log.info(newSettingsMessage);
        log.error(newSettingsMessage);
        String absoluteFilename = getAbsoluteFilename();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        setTimestamp(getTimestamp());
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        if (reset) {
            context.reset();
        }
        configuratorConsumer.accept(configurator);
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        if (!wasInitial) {
            String newAbsoluteFilename = getAbsoluteFilename();
            if (absoluteFilename != null || newAbsoluteFilename != null) {
                log.info("Previous log: " + (absoluteFilename == null ? "Console" : absoluteFilename + getFilenameSuffix()));
                log.error("Previous log: " + (absoluteFilename == null ? "Console" : absoluteFilename + getErrorFilenameSuffix()));
            }
        }
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

    public interface ConsumerWithCatchException<T> extends Consumer<T> {
        void acceptWithCatchExceptionx(T t) throws Exception;

        default void accept(T t) {
            try {
                acceptWithCatchExceptionx(t);
            } catch (Exception e) {
                // StatusPrinter should handle this
            }
        }
    }

}
