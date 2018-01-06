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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.function.Consumer;

/**
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

    public GSLogbackConfigurator modify(GSDocument gsDocument) {
        return configure(false, gsDocument.getDocument());
    }

    public GSLogbackConfigurator reset(GSDocument gsDocument) {
        return configure(true, gsDocument.getDocument());
    }


    public GSLogbackConfigurator modify(URL url) {
        return configure(false, url);
    }

    public GSLogbackConfigurator reset(URL url) {
        return configure(true, url);
    }

    public GSLogbackConfigurator modify(File file) {
        return configure(false, file);
    }

    public GSLogbackConfigurator reset(File file) {
        return configure(true, file);
    }

    public GSLogbackConfigurator reset() {
        return configure(true);
    }

    public GSLogbackConfigurator modify() {
        return configure(false);
    }

    /**
     * Flush (and ignore) non reported errors and warnings
     */
    public GSLogbackConfigurator flushRemainingErrorsOrWarnings() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getStatusManager().clear();
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

    private GSLogbackConfigurator configure(boolean reset) {
        return configure(reset, getCurrentConfigurationUrl());
    }

    private GSLogbackConfigurator configure(boolean reset, Document document) {
        GSXMLContext gsxmlContext = new GSXMLContext(document);
        if (isVerbose) {
            System.out.println(gsxmlContext.prettyprint());
        }
        return configure(reset, gsxmlContext.getInputStream());
    }

    private GSLogbackConfigurator configure(boolean reset, URL url) {
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

    private GSLogbackConfigurator configure(boolean reset, File file) {
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

    private GSLogbackConfigurator configure(boolean reset, InputStream inputStream) {
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

    private GSLogbackConfigurator configure(boolean reset, Consumer<JoranConfigurator> configuratorConsumer) {
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

}
