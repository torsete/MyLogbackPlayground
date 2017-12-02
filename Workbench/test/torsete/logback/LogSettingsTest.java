package torsete.logback;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void test2() {

        Logger log = LoggerFactory.getLogger(getClass());

        log.debug("log debug initial");
        log.info("log info initial");
        log.warn("log warn initial");
        log.error("log error initial");
        staticLog.debug("staticLog debug initial");
        staticLog.info("staticLog info initial");
        staticLog.warn("staticLog warn initial");
        staticLog.error("staticLog error initial");

//        new LogSettings(s -> s.setSystem("SystemXxx")
//                .setApplication("App42")
//                .setDatabase("BB1"));

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");

        new LogSettings(s->s.setUser("USRxxx"));

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");
    }  @Test
    public void test3() {

        Logger log = LoggerFactory.getLogger(getClass());

        log.debug("log debug initial");
        log.info("log info initial");
        log.warn("log warn initial");
        log.error("log error initial");
        staticLog.debug("staticLog debug initial");
        staticLog.info("staticLog info initial");
        staticLog.warn("staticLog warn initial");
        staticLog.error("staticLog error initial");

        new LogSettings(s -> s.setSystem("SystemXxx")
                .setApplication("App42")
                .setDatabase("ABk"));

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");

        new LogSettings(s->s.setUser("USRxxx"));

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
