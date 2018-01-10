package torsete.gslogback;

import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by Torsten on 28.11.2017.
 */
public class GSLogbackPropertiesTest {
    private static Logger log = LoggerFactory.getLogger(GSLogbackPropertiesTest.class);
    private int testCount;
    private Properties defaultSettings;

    private static GSLogbackTestUtil testUtil;

    @BeforeClass
    public static void beforeClass() {
        testUtil = new GSLogbackTestUtil().open();
        testUtil.configureInitialLogging();
    }

    @AfterClass
    public static void afterClass() {
        testUtil.close();
    }

    @Before
    public void before() {
        defaultSettings = GSLogbackProperties.exportProperties();
        testCount++;
    }

    @After
    public void after() {
        GSLogbackProperties.clear();
        GSLogbackProperties.importProperties(defaultSettings);
    }

    @Test
    public void testToStrings() {
        String s = GSLogbackProperties.toStrings();
        log.info("\n" + s);
        String[] split = s.split("\n");
        assertEquals(GSLogbackProperties.values().length, split.length);
        for (int i = 0; i < GSLogbackProperties.values().length; i++) {
            assertTrue(split[i].startsWith(GSLogbackProperties.values()[i].getKey() + "="));
        }
    }

    @Test
    public void testToStringsAll() {
        String s = GSLogbackProperties.toStringsAll();
        log.info("\n" + s);
        String[] split = s.split("\n");
        assertEquals(GSLogbackProperties.values().length, split.length);
        for (int i = 0; i < GSLogbackProperties.values().length; i++) {
            assertTrue(split[i].startsWith(GSLogbackProperties.values()[i].getKey() + "="));
        }

        System.setProperty("gslog.xxx", "yyy");
        s = GSLogbackProperties.toStringsAll();
        log.info("\n" + s);
        split = s.split("\n");
        assertEquals(GSLogbackProperties.values().length + 1, split.length);
        assertEquals(GSLogbackProperties.values().length + 1, GSLogbackProperties.toStringsAll().split("\n").length);
        assertEquals(GSLogbackProperties.values().length, GSLogbackProperties.toStrings().split("\n").length);

    }

    @Test
    public void testToVmArguments() {
        String s = GSLogbackProperties.toVmArguments();
        log.info("\n" + s);
        String[] split = s.split("\n");
        assertTrue(GSLogbackProperties.values().length >= split.length);
        int j = 0;
        for (int i = 0; i < GSLogbackProperties.values().length; i++) {
            if (!GSLogbackProperties.values()[i].isValid()) {
                continue;
            }
            assertTrue(split[j].startsWith("-D" + GSLogbackProperties.values()[i].getKey() + "="));
            if (GSLogbackProperties.values()[i].getValue() == null) {
                assertTrue(split[j].endsWith("="));
            }
            j++;
        }
        assertEquals(split.length, j);
    }

    @Test
    public void testProperties() {
        String s = GSLogbackProperties.toStrings();
        Properties properties = GSLogbackProperties.exportProperties();
        GSLogbackProperties.importProperties(properties);
        assertEquals(s, GSLogbackProperties.toStrings());

        properties.put(GSLogbackProperties.GSLOG_APPLICATION.getKey(), "abc");
        GSLogbackProperties.importProperties(properties);
        assertEquals("abc", GSLogbackProperties.GSLOG_APPLICATION.getValue());

        properties = new Properties();
        properties.put(GSLogbackProperties.GSLOG_FILE_NAME.getKey(), "def");
        GSLogbackProperties.importProperties(properties);
        assertEquals("abc", GSLogbackProperties.GSLOG_APPLICATION.getValue());
        assertEquals("def", GSLogbackProperties.GSLOG_FILE_NAME.getValue());

        log.info("\n" + GSLogbackProperties.toStrings());
    }
}
