package torsete.logback;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class LogSettingsTest {
    private static Logger staticLog = LoggerFactory.getLogger(LogSettingsTest.class);

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
        LogSettings settings = new LogSettings()
                .setSystem("SYSTEM42")
                .setApplication("TestccccApp")
                .setAdditionalFilename("xxx")
                .setDatabase("BB1")
                .activate(true);

        Logger log = LoggerFactory.getLogger(getClass());
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");

        settings.setUser("USRxxx").activate(true);

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");
    }

}
