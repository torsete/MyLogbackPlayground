package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import torsete.gslogback.GSLogbackConfigurator;
import torsete.gslogback.GSLogbackProperties;

/**
 * Created by Torsten on 28.11.2017.
 */
public class LogbackConfigurationDemo {
    private final static Logger log = LoggerFactory.getLogger(LogbackConfigurationDemo.class);

    public static void main(String... args) {
        log.info("HostName=" + GSLogbackProperties.GSLOG_HOST_NAME.getValue());
        log.info("ContextName=" + GSLogbackProperties.GSLOG_CONTEXT_NAME.getValue());
        log.info("Url=" + ConfigurationWatchListUtil.getMainWatchURL((LoggerContext) LoggerFactory.getILoggerFactory()));

        new LogbackConfigurationDemo().run();
    }

    private void run() {
//        runPlain();
        runLongPlain();
//        runLongPlainWithScan();
//        runConfigurationWithUserName();
//        runWithConfiguration();
    }

    private void runPlain() {
        doSomeWork(2);
    }

    private void runLongPlain() {
        doSomeWork(20000);
    }

    private void runLongPlainWithScan() {
//        new GSLogConfigurator().configure(true);
        doSomeWork(20000);
    }

    private void runConfigurationWithUserName() {
        log.info("Brugernavn skal lige bestemmes... ");

        GSLogbackProperties.GSLOG_USER.setValue("USRxxx");
        new GSLogbackConfigurator().reset();

        doSomeWork(2);
    }

    private void runWithConfiguration() {
        GSLogbackProperties.GSLOG_HOME.setValue("myhome");
        GSLogbackProperties.GSLOG_VERSION.setValue("myVersion");
        GSLogbackProperties.GSLOG_ENVIRONMENT.setValue("myenvironment");
        GSLogbackProperties.GSLOG_DATABASE.setValue("mydatabase");
        GSLogbackProperties.GSLOG_SYSTEM.setValue("mysystem");
        GSLogbackProperties.GSLOG_APPLICATION.setValue(getClass().getSimpleName());
        new GSLogbackConfigurator().reset();

        log.info("Brugernavn skal lige bestemmes... ");

        GSLogbackProperties.GSLOG_USER.setValue("USRxxx");
        new GSLogbackConfigurator().reset();
        log.info("logfil: " + GSLogbackProperties.getLogFile().getAbsolutePath());
        log.info("Settings:\n" + GSLogbackProperties.toStrings());

        doSomeWork(2);
    }


    private void doSomeWork(int repeat) {
        for (int i = 0; i < repeat; i++) {
            log.debug("Some debugging... " + i);
            log.info("Some info... " + i);

            if (i % 10 == 0) {
                log.error("Some errors... " + i);
            }
            pause(500);
        }
    }

    private void pause(long millis) {
        try {
            Thread.sleep(millis);
            for (int n = 0; n < 50000; n++) {
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
