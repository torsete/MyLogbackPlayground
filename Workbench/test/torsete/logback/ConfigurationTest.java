package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

/**
 * Created by Torsten on 28.11.2017.
 */
public class ConfigurationTest {
    private static Logger staticLog = LoggerFactory.getLogger(ConfigurationTest.class);

    class TestClass1 {
        private Logger log = LoggerFactory.getLogger(getClass());

        public void log(String s) {
            log.info(s);
        }
    }

    class TestClass2 {
        private Logger log = LoggerFactory.getLogger(getClass());

        public void log(String s) {
            log.info(s);
        }
    }

    class TestClass3 {
        private Logger log = LoggerFactory.getLogger(getClass());

        public void log(String s) {
            log.info(s);
        }

        public void logWarn(String s) {
            log.warn(s);
        }
    }

    @Test
    public void test1() {
//        System.setProperty("gslog.filename", "gslog_" + new Date().getTime());
        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        refreshConfiguration();
        Logger log = LoggerFactory.getLogger(getClass());

        Properties properties = System.getProperties();
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
    }

    @Test
    public void test2() {
        System.setProperty("gslog.filename", "gslog_" + new Date().getTime());
        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        refreshConfiguration();

        new TestClass1().log("hello");
        new TestClass2().log("hello");
        Logger log = LoggerFactory.getLogger(getClass());
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");

    }

    @Test
    public void test3() {
        System.setProperty("gslog.filename", "gslog_" + new Date().getTime());
        new TestClass1().log("hello");
        new TestClass2().log("hello");
        new TestClass3().log("hello skal komme på logfil ");
        new TestClass3().logWarn("hello");

        Logger log = LoggerFactory.getLogger(getClass());
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");

        copyFile("test/logback-test.xml", "temp");
        pause(1000);
        copyFile("temp", "test/logback-test.xml");

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        refreshConfiguration();

        new TestClass1().log("hello");
        new TestClass2().log("hello");
        new TestClass3().log("hello må ikke komme på logfil ");
        new TestClass3().logWarn("hello");


        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");

    }

    @Test
    public void test4() {
        Logger log = LoggerFactory.getLogger(getClass());

        doLogging(log, 10);

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettingsINFO.xml");
        refreshConfiguration();


        doLogging(log, 10);

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettingsDEBUG.xml");
        refreshConfiguration();


        doLogging(log, 10);


    }


    private ConfigurationTest doLogging(Logger log, int number) {
        for (int i = 0; i < number; i++) {
            log.debug("----------------------->");
            log.info("--->");
//            staticLog.debug("=======================>");
//            staticLog.info("===>");
            pause(250);
        }
        return this;
    }

    private void refreshConfiguration() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        URL mainWatchURL = ConfigurationWatchListUtil.getMainWatchURL(context);
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(mainWatchURL);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private ConfigurationTest pause(long millis) {
        try {
            Thread.sleep(millis);
            for (int n = 0; n < 50000; n++) {
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }


    private ConfigurationTest copyFile(String source, String dest) {

        return copyFile(new File(source), new File(dest));
    }

    private ConfigurationTest copyFile(File source, File dest) {
        try {
            InputStream is = null;
            OutputStream os = null;
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            try {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                is.close();
                os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(">" + new Date(dest.lastModified()) + " " + dest.getAbsolutePath());
        return this;
    }
}
