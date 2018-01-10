package torsete.gslogback;

import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * Created by Torsten on 28.11.2017.
 */
public class GSLogbackConfiguratorTest {
    private static Logger log = LoggerFactory.getLogger(GSLogbackConfiguratorTest.class);
    private static URL initialConfigurationUrl;
    private GSLogbackConfigurator configurator;
    @Rule
    public TestName testName = new TestName();
    static GSLogbackTestUtil testUtil;

    @BeforeClass
    public static void beforeClass() {
        testUtil = new GSLogbackTestUtil().open();
        testUtil.configureInitialLogging("..\\");
        GSLogbackProperties.clear();
        GSLogbackProperties.GSLOG_SYSTEM.setValue("JUnit");
        GSLogbackProperties.GSLOG_APPLICATION.setValue(GSLogbackConfiguratorTest.class.getSimpleName());
        initialConfigurationUrl = new GSLogbackConfigurator().reset().getCurrentConfigurationUrl();
    }


    @AfterClass
    public static void afterClass() {
        testUtil.close();
    }

    @Before
    public void before() {
        GSLogbackProperties.clear();
        GSLogbackProperties.GSLOG_SYSTEM.setValue("JUnit");
        GSLogbackProperties.GSLOG_APPLICATION.setValue(getClass().getSimpleName() + "." + testName.getMethodName());
        configurator = new GSLogbackConfigurator().reset(initialConfigurationUrl);
        System.out.println("\n" + GSLogbackProperties.toStrings());
    }

    @Test
    public void testConfigureSelectionLoggingWithWarn() {
        Logger logA = LoggerFactory.getLogger("loggerA-" + testName.getMethodName());
        Logger logB = LoggerFactory.getLogger("loggerB-" + testName.getMethodName());
        Logger logC = LoggerFactory.getLogger("loggerC-" + testName.getMethodName());

        File file = GSLogbackProperties.getLogFile();
        String fullOutLogFileName = file.getAbsolutePath();

        GSLogbackConfigurationDocument configuration = new GSLogbackConfigurationDocument("configuration")
                .setFilenameWithExtension(fullOutLogFileName)
                .setRootLevel("INFO")
                .addLoggers("loggerA-" + testName.getMethodName(), "loggerB-" + testName.getMethodName());
        configurator.reset(configuration);

        logA.info("her er A");
        logB.info("her er B");
        logB.warn("her er warn B");
        logC.info("her er C");
        logC.warn("her er warn C");


        System.out.println("\n" + file.getAbsolutePath() + "\n" + "--->\n" + fileContent(file) + "<---");
        Assert.assertEquals(3, countInFile("\n", fullOutLogFileName));
        Assert.assertEquals(2, countInFile(" INFO ", fullOutLogFileName));
        Assert.assertEquals(1, countInFile(" WARN ", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er A", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er B", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er warn B", fullOutLogFileName));
        Assert.assertEquals(0, countInFile("her er C", fullOutLogFileName));
        Assert.assertEquals(0, countInFile("her er warn C", fullOutLogFileName));
    }

    @Test
    public void testConfigureSelectionLoggingWithWarn1() {
        Logger logA = LoggerFactory.getLogger("loggerA-1");
        Logger logB = LoggerFactory.getLogger("loggerB-1");
        Logger logC = LoggerFactory.getLogger("loggerC-1");

        File file = GSLogbackProperties.getLogFile();
        String fullOutLogFileName = file.getAbsolutePath();

        GSLogbackConfigurationDocument configuration = new GSLogbackConfigurationDocument("configuration")
                .setFilenameWithExtension(fullOutLogFileName)
                .setRootLevel("INFO")
                .addLoggers("loggerA-1", "loggerB-1");
        configurator.reset(configuration);

        logA.info("her er A");
        logB.info("her er B");
        logB.warn("her er warn B");
        logC.info("her er C");
        logC.warn("her er warn C");

        System.out.println("\n" + file.getAbsolutePath() + "\n" + "--->\n" + fileContent(file) + "<---");
        Assert.assertEquals(3, countInFile("\n", fullOutLogFileName));
        Assert.assertEquals(2, countInFile(" INFO ", fullOutLogFileName));
        Assert.assertEquals(1, countInFile(" WARN ", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er A", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er B", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er warn B", fullOutLogFileName));
        Assert.assertEquals(0, countInFile("her er C", fullOutLogFileName));
        Assert.assertEquals(0, countInFile("her er warn C", fullOutLogFileName));

        configurator.printRemainingErrorsOrWarnings();  // Logback har givet en warning fordi der var logingr der (med hensigt) blev ignoreret
    }

    @Test
    public void testConfigureSelectionLoggingWithWarn2() {
        Logger logA = LoggerFactory.getLogger("loggerA-1");
        Logger logB = LoggerFactory.getLogger("loggerB-1");
        Logger logC = LoggerFactory.getLogger("loggerC-1");

        File file = GSLogbackProperties.getLogFile();
        String fullOutLogFileName = file.getAbsolutePath();

        GSLogbackConfigurationDocument configuration = new GSLogbackConfigurationDocument("configuration")
                .setFilenameWithExtension(fullOutLogFileName)
                .setRootLevel("INFO")
                .addLoggers("loggerA-1", "loggerC-1");
        configurator.reset(configuration);

        logA.info("her er A");
        logB.info("her er B");
        logB.warn("her er warn B");
        logC.info("her er C");
        logC.warn("her er warn C");

        System.out.println("\n" + file.getAbsolutePath() + "\n" + "--->\n" + fileContent(file) + "<---");
        Assert.assertEquals(3, countInFile("\n", fullOutLogFileName));
        Assert.assertEquals(2, countInFile(" INFO ", fullOutLogFileName));
        Assert.assertEquals(1, countInFile(" WARN ", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er A", fullOutLogFileName));
        Assert.assertEquals(0, countInFile("her er B", fullOutLogFileName));
        Assert.assertEquals(0, countInFile("her er warn B", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er C", fullOutLogFileName));
        Assert.assertEquals(1, countInFile("her er warn C", fullOutLogFileName));

        configurator.printRemainingErrorsOrWarnings();  // Logback har givet en warning fordi der var logingr der (med hensigt) blev ignoreret
    }

    @Test
    public void testPlainLogging() {

        configurator.reset();
        Logger logA = LoggerFactory.getLogger("loggerA-" + testName.getMethodName());
        Logger logB = LoggerFactory.getLogger("loggerB-" + testName.getMethodName());
        Logger logC = LoggerFactory.getLogger("loggerC-" + testName.getMethodName());

        logA.info("her er A");
        logB.info("her er B");
        logB.warn("her er warn B");
        logC.info("her er C");
        logC.warn("her er warn C");

    }

    private int countInFile(String s, File file) {
        String content = fileContent(file);
        if (content == null) {
            return 0;
        }
        return countOccurrences(content, s);

    }

    private String fileContent(File file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            br.close();
            return sb.toString();

        } catch (IOException e) {
            // Return null
        }
        return null;
    }

    private int countInFile(String s, String filename) {
        return countInFile(s, new File(filename));
    }

    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return true;
            }
            for (String aChildren : children) {
                File file = new File(dir, aChildren);
                boolean success = deleteDir(file);
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete(); // The directory is empty now and can be deleted.
    }

    public int countOccurrences(String string, String substring) {
        int count = 0;
        String rest = string;
        while (rest.length() > 0) {
            int pos = rest.indexOf(substring);
            if (pos < 0) {
                break;
            }
            count++;
            rest = rest.substring(pos + substring.length());
        }
        return count;
    }
}
