package torsete.logback;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;

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
        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        configure("test/logback-test.xml");
        Logger log = LoggerFactory.getLogger(getClass());

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
    }

    @Test
    public void test2() {
        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettings.xml");
        configure("test/logback-test.xml");

        new TestClass1().log("hello");
        new TestClass2().log("hello");
        Logger log = LoggerFactory.getLogger(getClass());
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");

    }

    @Test
    public void test3() {
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
//        configure("test/logback-test.xml");

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
        configure("test/logback-test.xml");


        doLogging(log, 10);

        System.setProperty("gslog.resource", getClass().getSimpleName() + "-logsettingsDEBUG.xml");
        configure("test/logback-test.xml");


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

    private void configure(String filename) {
        // assume SLF4J is bound to logback in the current environment
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            // Call context.reset() to clear any previous configuration, e.g. default
            // configuration. For multi-step configuration, omit calling context.reset().
            context.reset();
            configurator.doConfigure(filename);
        } catch (JoranException je) {
            // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
//        StatusPrinter.print(context);
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
