package torsete.logback.drafts;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

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

//        log.debug(
// "log debug initial");
//        log.info("log info initial");
//        log.warn("log warn initial");
//        log.error("log error initial");
//        staticLog.debug("staticLog debug initial");
//        staticLog.info("staticLog info initial");
//        staticLog.warn("staticLog warn initial");
//        staticLog.error("staticLog error initial");

        LogSettings.activate(true, s -> s.setSystem("SystemXxx")
                .setApplication("App42")
                .setDatabase("BB1"));

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");

        LogSettings.activate(true, s -> s.setUser("USRxxx"));

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");
    }

    @Test
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

        LogSettings.activate(true, s -> s
                .setServer("testlogserver/")
                .setHome("logs/vX.XX")
                .setEnvironment("K")
                .setSystem("SystemXxx")
                .setApplication("App42")
                .setDatabase("ABk"));

        for (int i = 0; i < 10000; i++) {
            log.debug("log debug");
            log.info("log info " + new Date());
            pause(1000);
        }
        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");

        LogSettings.activate(true, s -> s.setUser("USRxxx"));

        log.debug("log debug");
        log.info("log info");
        log.warn("log warn");
        log.error("log error");
        staticLog.debug("staticLog debug");
        staticLog.info("staticLog info");
        staticLog.warn("staticLog warn");
        staticLog.error("staticLog error");
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
