package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.function.Consumer;

/**
 * Ansvar:
 * 1) At indkapsle programmatisk brud af Logback-konfiguration
 * 2) At være mediator for vores log settings og Logback
 */
public class GSLogConfigurator {
    private final static Logger log = LoggerFactory.getLogger(GSLogConfigurator.class);

    private GSLogSettings logSettings;

    public GSLogConfigurator() {
        logSettings = new GSLogSettings();
    }

    public GSLogSettings getLogSettings() {
        return logSettings;
    }

    public GSLogConfigurator setLogSettings(GSLogSettings logSettings) {
        this.logSettings = logSettings;
        return this;
    }

    public GSLogConfigurator acceptSettings(Consumer<GSLogSettings> consumer) {
        consumer.accept(logSettings);
        return this;
    }

    public GSLogConfigurator configure(boolean reset) {
        URL url = ConfigurationWatchListUtil.getMainWatchURL((LoggerContext) LoggerFactory.getILoggerFactory());
        return configure(reset, url);
    }

    public GSLogConfigurator configure() {
        return configure(false);
    }

    public GSLogConfigurator configure(boolean reset, URL url) {
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(url);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return configure(reset, configuratorConsumer);
    }

    public GSLogConfigurator configure(boolean reset, File file) {
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(file);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return configure(reset, configuratorConsumer);
    }

    public GSLogConfigurator configure(boolean reset, FileInputStream fileInputStream) {
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(fileInputStream);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        return configure(reset, configuratorConsumer);
    }

    /**
     * Skal altid kaldes fra Tomcat når "contextDestroyed" (for at undgå memory leak)
     */
    public void stop() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.stop();
    }

    GSLogConfigurator configure(boolean reset, Consumer<JoranConfigurator> configuratorConsumer) {
        log.info("Log configuration is changing");

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        if (reset) {
            context.reset();
        }
        configuratorConsumer.accept(configurator);
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);

        log.info("Log configuration has changed");
        return this;
    }


}
