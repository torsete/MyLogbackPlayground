package torsete.gslogback;

import org.junit.Before;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static junit.framework.TestCase.*;

/**
 * Created by Torsten on 28.11.2017.
 */
public class GSLogbackSettingsTest {
    private GSLogbackSettings settings;
    private int testCount;

    @Before
    public void before() {
        testCount++;
        settings = new GSLogbackSettings();
    }

    @Test
    public void testSetAndGetAndReset0() {
        assertEquals(settings, settings.set("gslog.xxx", "value"));
        assertEquals("value", settings.get("gslog.xxx"));
        assertNull(settings.reset("gslog.xxx").get("gslog.xxx"));
        try {
            settings.set("gslog.xxx", null);
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
        try {
            settings.set("gslog.xxx", "");
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
        try {
            settings.set("gslog.xxx", " ");
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }

    }

    @Test
    public void testPropertyDatabase0() {
        System.clearProperty(GSLogbackSettings.GSLOG_DATABASE);
        assertEquals(settings, settings.setDatabase("database"));
        assertEquals("database", settings.getDatabase());
        assertNull(settings.resetDatabase().getDatabase());

        System.clearProperty(GSLogbackSettings.GSLOG_DATABASE);
        assertEquals(settings, settings.set(GSLogbackSettings.GSLOG_DATABASE, "database"));
        assertEquals("database", settings.get(GSLogbackSettings.GSLOG_DATABASE));
        assertEquals(settings, settings.reset(GSLogbackSettings.GSLOG_DATABASE));
        assertNull(settings.getDatabase());
        try {
            settings.setDatabase(null);
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
        try {
            settings.setDatabase("");
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
        try {
            settings.setDatabase(" ");
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
    }

    @Test
    public void testSetAndGetAndReset() {
        String propertyValue = testCount + "";
        testPropertySet("gslog.xxx", propertyValue, (s, v) -> s.set("gslog.xxx", v));
        testPropertyGet("gslog.xxx", propertyValue, s -> s.get("gslog.xxx"));
        testPropertyReset("gslog.xxx", s -> s.reset("gslog.xxx"));
    }

    @Test
    public void testTest() {
        String propertyValue = testCount + "";
        try {
            testPropertySet(GSLogbackSettings.GSLOG_USER, propertyValue, (s, v) -> s.setDatabase(v));
            fail("Forventer en AssertionError");
        } catch (AssertionError t) {
            // Forventer denne Exception
        }
        try {
            testPropertyGet(GSLogbackSettings.GSLOG_DATABASE, propertyValue, s -> s.getUser());
            fail("Forventer en AssertionError");
        } catch (AssertionError t) {
            // Forventer denne Exception
        }

    }


    @Test
    public void testPropertyDatabase() {
        String propertyValue = testCount + "";
        testPropertySet(GSLogbackSettings.GSLOG_DATABASE, propertyValue, (s, v) -> s.setDatabase(v));
        testPropertyReset(GSLogbackSettings.GSLOG_DATABASE, s -> s.resetDatabase());
    }

    @Test
    public void testPropertyUser() {
        String propertyValue = testCount + "";
        testPropertySet(GSLogbackSettings.GSLOG_USER, propertyValue, (s, v) -> s.setUser(v));
        testPropertyGet(GSLogbackSettings.GSLOG_USER, propertyValue, s -> s.getUser());
        testPropertyReset(GSLogbackSettings.GSLOG_USER, s -> s.resetUser());
    }

    void testPropertySet(String propertyKey, String propertyvalue, BiFunction<GSLogbackSettings, String, GSLogbackSettings> setFunction) {
        System.clearProperty(propertyKey);
        assertEquals(settings, setFunction.apply(settings, propertyvalue));
        assertEquals(propertyvalue, System.getProperty(propertyKey));

        System.clearProperty(propertyKey);
        assertEquals(settings, settings.set(propertyKey, propertyvalue));
        assertEquals(propertyvalue, System.getProperty(propertyKey));

        try {
            setFunction.apply(settings, null);
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
        try {
            setFunction.apply(settings, "");
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
        try {
            setFunction.apply(settings, " ");
            fail("Forventer en IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Forventer denne Exception
        }
    }

    void testPropertyGet(String propertyKey, String propertyvalue, Function<GSLogbackSettings, String> getFunction) {
        System.setProperty(propertyKey, propertyvalue);
        assertEquals(propertyvalue, getFunction.apply(settings));
    }

    void testPropertyReset(String propertyKey, Function<GSLogbackSettings, GSLogbackSettings> resetFunction) {
        String value = resetFunction.toString(); // bare et eller andet unikt
        System.setProperty(propertyKey, value);
        assertEquals(settings, resetFunction.apply(settings));
        assertNull(settings.get(propertyKey));
        assertNull(System.getProperty(propertyKey));

        System.setProperty(propertyKey, value);
        assertEquals(settings, settings.reset(propertyKey));
        assertNull(settings.get(propertyKey));
        assertNull(System.getProperty(propertyKey));
    }
}
