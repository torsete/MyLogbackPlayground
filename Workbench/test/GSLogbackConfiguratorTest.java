import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import torsete.logback.GSLogbackConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Torsten on 03.12.2017.
 */
public class GSLogbackConfiguratorTest {
    private static Logger log = LoggerFactory.getLogger(GSLogbackConfiguratorTest.class);

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

    }

    @Test
    public void test() {
        log.info("test starter");
        TestClass1 testClass1 = new TestClass1();
        TestClass2 testClass2 = new TestClass2();
        TestClass3 testClass3 = new TestClass3();
        testClass1.log("test1");
        testClass2.log("test2");
        testClass3.log("test3");
        File file = createFile(
                "<configuration>\n" +
                        "    <appender name='APPENDER1' class='ch.qos.logback.core.FileAppender'>\n" +
                        "        <file>${gslog.absoluteFilename}_testFile1${gslog.filenameSuffix}</file>\n" +
                        "        <encoder>\n" +
                        "            <pattern>${gslog.pattern}</pattern>\n" +
                        "        </encoder>\n" +
                        "    </appender>\n" +
                        "    <logger name='" + testClass1.getClass().getName() + "' level='debug'>\n" +
                        "        <appender-ref ref='APPENDER1'/>\n" +
                        "    </logger>\n" +
                        "    <appender name='APPENDER2' class='ch.qos.logback.core.FileAppender'>\n" +
                        "        <file>${gslog.absoluteFilename}_testFile2${gslog.filenameSuffix}</file>\n" +
                        "        <encoder>\n" +
                        "            <pattern>${gslog.pattern}</pattern>\n" +
                        "        </encoder>\n" +
                        "    </appender>\n" +
                        "    <logger name='" + testClass2.getClass().getName() + "' level='debug'>\n" +
                        "        <appender-ref ref='APPENDER2'/>\n" +
                        "    </logger>\n" +
                        "    <logger name='" + testClass3.getClass().getName() + "' level='debug'>\n" +
                        "        <appender-ref ref='APPENDER2'/>\n" +
                        "    </logger>\n" +
                        "</configuration>\n" +
                        "");

        new GSLogbackConfigurator()
                .configure(false, file);

        testClass1.log("test1");
        testClass2.log("test2");
        testClass3.log("test3");
        log.info("test slutter");
    }

    @Test
    public void test2() {
        TestClass1 testClass1 = new TestClass1();
        TestClass2 testClass2 = new TestClass2();
        TestClass3 testClass3 = new TestClass3();
        File file = createFile(
                "<configuration>\n" +
                        "    <include resource='logback-gs-standard-properties.xml'/>\n" +
                        "    <include resource='logback-gs-standard-file-appenders.xml'/>\n" +
                        "    <logger name='" + testClass1.getClass().getName() + "' level='debug'>\n" +
                        "        <appender-ref ref='gslog.fileAppender'/>\n" +
                        "    </logger>\n" +
                        "    <logger name='" + testClass2.getClass().getName() + "' level='debug'>\n" +
                        "        <appender-ref ref='gslog.fileAppender'/>\n" +
                        "    </logger>\n" +
                        "</configuration>\n" +
                        "");

        new GSLogbackConfigurator()
                .configure(true, file);

        testClass1.log("test1");
        testClass2.log("test2");
        testClass3.log("test3");
        log.info("test slutter");
    }

    private File createFile(String content) {
        try {
            PrintWriter out = new PrintWriter("temp1.xml");
            out.append(content);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File("temp1.xml");

    }


}
