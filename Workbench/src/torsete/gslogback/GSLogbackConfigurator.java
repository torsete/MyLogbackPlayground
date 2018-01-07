package torsete.gslogback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.status.StatusListener;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import torsete.util.GSDocument;
import torsete.util.GSXMLContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Facade to Logback configuration (essentially by this method: {@link #configure(boolean, InputStream)})
 * <p>
 * The intent is to isolate all programtically configuration and the like of Logback to one class
 * <p>
 * The class is intentionally stateless (except for {@link #logListeners})
 * /
 */
public class GSLogbackConfigurator {
    private final static Logger log = LoggerFactory.getLogger(GSLogbackConfigurator.class);

    private List<Consumer<String>> logListeners;
    private Consumer<String> systemOutListener = s -> System.out.println(s);

    public GSLogbackConfigurator() {
        init();
    }

    void init() {
        logListeners = new ArrayList<>();
        StatusPrinter.setPrintStream(new StatusPrinterPrintStream());
        setReportToSystemOut(false);
    }


    public GSLogbackConfigurator add(GSDocument gsDocument) {
        return configure(false, gsDocument.getDocument());
    }

    public GSLogbackConfigurator reset(GSDocument gsDocument) {
        return configure(true, gsDocument.getDocument());
    }

    public GSLogbackConfigurator add(URL url) {
        return configure(false, url);
    }

    public GSLogbackConfigurator reset(URL url) {
        return configure(true, url);
    }

    public GSLogbackConfigurator add(File file) {
        return configure(false, file);
    }

    public GSLogbackConfigurator reset(File file) {
        return configure(true, file);
    }

    public GSLogbackConfigurator reset() {
        return configure(true);
    }

    public GSLogbackConfigurator add() {
        return configure(false);
    }


    private GSLogbackConfigurator configure(boolean reset) {
        return configure(reset, getCurrentConfigurationUrl());
    }


    interface ConfiguratorConsumer extends Consumer<JoranConfigurator> {
        @Override
        default void accept(JoranConfigurator joranConfigurator) {
            try {
                acceptThrowsJoranException(joranConfigurator);
            } catch (JoranException e) {
                // StatusPrinter will handle this
            }
        }

        void acceptThrowsJoranException(JoranConfigurator joranConfigurator) throws JoranException;
    }

    private GSLogbackConfigurator configure(boolean reset, Document document) {
        GSXMLContext gsxmlContext = new GSXMLContext(document);
        configure(reset, gsxmlContext.getInputStream());
        logListeners.forEach(l -> l.accept(gsxmlContext.prettyprint()));
        return this;
    }

    private GSLogbackConfigurator configure(boolean reset, URL url) {
        configure(reset, c -> c.doConfigure(url));
        logListeners.forEach(l -> l.accept("Log configuration is changed by url. Current configuration url=" + getCurrentConfigurationUrl()));
        GSXMLContext gsxmlContext = new GSXMLContext();
        gsxmlContext.parseFile(new File(url.getFile()));
        logListeners.forEach(l -> l.accept(gsxmlContext.prettyprint()));
        return this;
    }

    private GSLogbackConfigurator configure(boolean reset, File file) {
        configure(reset, c -> c.doConfigure(file));
        logListeners.forEach(l -> l.accept("Log configuration is changed by file. Current configuration url=" + getCurrentConfigurationUrl()));
        GSXMLContext gsxmlContext = new GSXMLContext();
        gsxmlContext.parseFile(file);
        logListeners.forEach(l -> l.accept(gsxmlContext.prettyprint()));
        return this;
    }

    private GSLogbackConfigurator configure(boolean reset, InputStream inputStream) {
        configure(reset, c -> c.doConfigure(inputStream));
        logListeners.forEach(l -> l.accept("Log configuration is changed by input stream. Current configuration url=" + getCurrentConfigurationUrl()));
        return this;
    }

    private GSLogbackConfigurator configure(boolean reset, ConfiguratorConsumer configuratorConsumer) {
        StatusListener statusListener = null;
        printRemainingErrorsOrWarnings();
        flushRemainingInfosErrorsOrWarnings();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        statusListener = status ->
                logListeners.forEach(l -> l.accept(getClass().getSimpleName() +
                        ": " + status.getLevel() + "/" + status.getEffectiveLevel() +
                        " " + status.getMessage() +
                        " (" + status.getOrigin() + ")"
                ));
        context.getStatusManager().add(statusListener);

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        if (reset) {
            context.reset();
        }
        configuratorConsumer.accept(configurator);

        printRemainingErrorsOrWarnings();
        flushRemainingInfosErrorsOrWarnings();

        context.getStatusManager().remove(statusListener);

        return this;
    }

    /**
     * Flush (and ignore) non reported loggings (infos, errors and warnings)
     */
    public GSLogbackConfigurator flushRemainingInfosErrorsOrWarnings() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getStatusManager().clear();
        return this;
    }

    /**
     * Print non reported errors and warnings
     */
    public GSLogbackConfigurator printRemainingErrorsOrWarnings() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
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

    class StatusPrinterPrintStream extends PrintStream {
        public StatusPrinterPrintStream() {
            super(new ByteArrayOutputStream(), true);
        }

        @Override
        public void print(String s) {
            Arrays.stream(s.split("\n")).forEach(line -> logListeners.forEach(l -> l.accept("StatusPrinter: " + line)));
        }
    }

    public GSLogbackConfigurator setReportToSystemOut(boolean enabled) {
        removeLogListner(systemOutListener);
        if (enabled) {
            logListeners.add(systemOutListener);
        }

        return this;
    }

    public GSLogbackConfigurator addLogListner(Consumer<String> logListener) {
        logListeners.add(logListener);
        return this;
    }

    public GSLogbackConfigurator removeLogListner(Consumer<String> logListener) {
        logListeners.remove(logListener);
        return this;
    }
}
