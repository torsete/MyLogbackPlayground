package torsete.gslogback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import torsete.util.GSDocument;
import torsete.util.GSXMLContext;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ansvar:
 * 1) At indkapsle programmatisk brud af Logback-konfiguration
 * 2) At være mediator for vores log settings og Logback
 * Facade to Logback configuration (essentially by this method: {@link #configure(boolean, InputStream)})
 * <p>
 * The intent is to isolate all programtically configuration and the like of Logback to one class
 * <p>
 * The class is intentionally stateless (except for setting of {@link #isVerbose})
 * /
 */
public class GSLogbackConfigurator {
    private final static Logger log = LoggerFactory.getLogger(GSLogbackConfigurator.class);

    /**
     * True if the Logback configuration process should be verbose (ie log the configuration process on System.out).
     */
    private boolean isVerbose;

    public GSLogbackConfigurator setVerbose(boolean verbose) {
        isVerbose = verbose;
        return this;
    }

    /**
     * An xml variant of the classic way to programmatically configure Logback
     *
     * @param reset    True to clear any previous configuration, e.g. default configuration. False for multi-step configuration
     * @param document Configuration expressed in xml
     */
    public GSLogbackConfigurator configure(boolean reset, Document document) {
        GSXMLContext gsxmlContext = new GSXMLContext(document);

        if (isVerbose) {
            System.out.println(gsxmlContext.prettyprint());
        }
//        return configure(reset, gsxmlContext.serializeAsFile(new File("temp")));
        return configure(reset, gsxmlContext.getInputStream());
    }

    /**
     * See {@link #configure(boolean, Document)}
     */
    public GSLogbackConfigurator modify(GSDocument gsDocument) {
        return configure(false, gsDocument.getDocument());
    }

    /**
     * Gets the Logback Status manager
     */
    public StatusManager getStatusManager() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getStatusManager();
    }

    /**
     * Flush (and ignore) non reported errors and warnings
     */
    public GSLogbackConfigurator flushRemainingErrorsOrWarnings() {
        getStatusManager().clear();
        return this;
    }

    /**
     * Gets the url of the current configuration file
     *
     * @return Null if such a file is not present (ie. in case of a purely programmatically cobfiguration)
     */
    public URL getCurrentConfigurationUrl() {
        return ConfigurationWatchListUtil.getMainWatchURL((LoggerContext) LoggerFactory.getILoggerFactory());
    }

    public GSLogbackConfigurator configure(boolean reset) {
        URL url = ConfigurationWatchListUtil.getMainWatchURL((LoggerContext) LoggerFactory.getILoggerFactory());
        return configure(reset, url);
    }

    public GSLogbackConfigurator configure() {
        return configure(true);
    }

    public GSLogbackConfigurator modify() {
        return configure(false);
    }


    public GSLogbackConfigurator configure(boolean reset, URL url) {
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(url);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        configure(reset, configuratorConsumer);
        log.info("Log configuration is changed by url. Current configuration url=" + getCurrentConfigurationUrl());
        return this;
    }

    public GSLogbackConfigurator configure(boolean reset, File file) {
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(file);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        configure(reset, configuratorConsumer);
        log.info("Log configuration is changed by file. Current configuration url=" + getCurrentConfigurationUrl());
        return this;
    }

    public GSLogbackConfigurator configure(boolean reset, InputStream inputStream) {
        Consumer<JoranConfigurator> configuratorConsumer = c -> {
            try {
                c.doConfigure(inputStream);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        };
        configure(reset, configuratorConsumer);
        log.info("Log configuration is changed by input stream. Current configuration url=" + getCurrentConfigurationUrl());
        return this;
    }

//    public GSLogbackConfigurator disableLogging() {
//        GSLogbackConfigurationDocument configurationDocument = newConfigurationDocument();
//        configurationDocument.addRootLevel("OFF");
//        new GSLogbackConfigurator().setVerbose(isVerbose).configure(true, configurationDocument);
//        return this;
//    }

    /**
     * Skal altid kaldes fra Tomcat når "contextDestroyed" (for at undgå memory leak)
     */
    public void stop() {
        closeAllAppenders();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

    }

    GSLogbackConfigurator configure(boolean reset, Consumer<JoranConfigurator> configuratorConsumer) {
//        log.info("Log configuration is changing");
        StatusListener statusListener = null;

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        if (isVerbose) {
            statusListener = status ->
                    System.out.println(getClass().getSimpleName() +
                            ": " + status.getLevel() + "/" + status.getEffectiveLevel() +
                            " " + status.getMessage() +
                            " (" + status.getOrigin() + ")"
                    );

            context.getStatusManager().add(statusListener);
        }

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        if (reset) {
            context.reset();
        }
        configuratorConsumer.accept(configurator);
        if (!isVerbose) {
            StatusPrinter.printInCaseOfErrorsOrWarnings(context);
        }

        context.getStatusManager().remove(statusListener);

        return this;
    }

    private void closeAllAppenders() {
        traverseAppenders(a -> a.stop());
    }

    private void closeConsoleAppendersByLoggerName(String name) {
        traverseAppendersByLoggerName(appender -> {
                    if (appender instanceof ConsoleAppender) {
                        appender.stop();
                    }
                }
                , name);
    }

    private void traverseAppendersByLoggerName(Consumer<Appender> appenderConsumer, String loggerName) {

        LoggerContext logCtx = (LoggerContext) LoggerFactory.getILoggerFactory();
        List<ch.qos.logback.classic.Logger> currentLoggers = logCtx.getLoggerList();

        for (ch.qos.logback.classic.Logger logger : currentLoggers) {
            Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
//            boolean b = appenderIterator.hasNext();
            if (loggerName == null || logger.getName().compareToIgnoreCase(loggerName) == 0) {
                for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                    Appender<ILoggingEvent> appender = index.next();
                    appenderConsumer.accept(appender);
                }
            }

        }
        ch.qos.logback.classic.Logger logger = getRootLogger();
        if (loggerName == null || logger.getName().compareToIgnoreCase(loggerName) == 0) {
            for (Iterator<Appender<ILoggingEvent>> index = logger.iteratorForAppenders(); index.hasNext(); ) {
                Appender<ILoggingEvent> appender = index.next();
                appenderConsumer.accept(appender);
            }
        }
    }


    private void traverseAppenders(Consumer<Appender> appenderConsumer) {
        traverseAppendersByLoggerName(appenderConsumer, null);
    }

    public ch.qos.logback.classic.Logger getRootLogger() {
        return (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
    }

}
