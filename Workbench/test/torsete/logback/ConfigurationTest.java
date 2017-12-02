package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
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
        System.setProperty("gslog.filename", "test1");
        System.setProperty("database", "xxx");
//        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        Logger log = LoggerFactory.getLogger(getClass());
        refreshConfiguration(true);
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
        System.setProperty("gslog.filename", "test2");
        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");

        new TestClass1().log("hello");
        new TestClass2().log("hello");
        Logger log = LoggerFactory.getLogger(getClass());
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");

    }

    @Test
    public void test3() {
        System.setProperty("gslog.filename", "test2");
        new TestClass1().log("hello");
        new TestClass2().log("hello");
        new TestClass3().log("hello skal komme på logfil ");
        new TestClass3().logWarn("hello");

        Logger log = LoggerFactory.getLogger(getClass());
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");

        copyFile("test/logback-gs-standard-unittest.xml", "temp");
        pause(1000);
        copyFile("temp", "test/logback-gs-standard-unittest.xml");

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        refreshConfiguration(false);

        new TestClass1().log("hello");
        new TestClass2().log("hello");
        new TestClass3().log("hello må ikke komme på logfil ");
        new TestClass3().logWarn("hello");

        refreshConfiguration(false);

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
        System.setProperty("gslog.filename", "test4");
        Logger log = LoggerFactory.getLogger(getClass());

        doLogging(log, 10);

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettingsINFO.xml");
        refreshConfiguration(false);


        doLogging(log, 10);

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettingsDEBUG.xml");
        refreshConfiguration(false);


        doLogging(log, 10);


    }

    public void testMDC() {
        Logger log = LoggerFactory.getLogger(getClass());

        MDC.put("database", "xxx");


    }

    @Test
    public void test7() {
        System.setProperty("gslog.filename", "test7");
        System.setProperty("database", "xxx");
        refreshConfiguration(true);
        Logger log = LoggerFactory.getLogger(getClass());

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");
    }


    private ConfigurationTest doLogging(Logger log, int number) {
        for (int i = 0; i < number; i++) {
            log.debug("----------------------->");
            log.info("--->");
//            staticLog.debug("=======================>");
//            staticLog.info("===>");
//            pause(250);
        }
        return this;
    }

    private void refreshConfiguration(boolean reset) {
        LogSettings activate = LogSettings.activate(true, c -> {
        });
        String timestamp = activate.getTimestamp();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        String gstimestamp = context.getProperty("gslog.timestamp");
//        System.setProperty("GSTIMESTAMP", gstimestamp);
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

    private String logFilename() {
        return "gslog_" + timestamp() + "_";
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
    }
}
