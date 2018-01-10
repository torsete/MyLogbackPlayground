package torsete.gslogback;

import org.junit.Ignore;
import torsete.EDITestHelper;
import torsete.util.GSXMLContext;

import java.io.File;

@Ignore
public class GSLogbackTestUtil {
    EDITestHelper ediTestHelper;
    String testFolderName;

    public GSLogbackTestUtil open() {
        ediTestHelper = new EDITestHelper(getClass());
        testFolderName = ediTestHelper.establishEdiTestDirectory();
        return this;
    }

    public void close() {
        ediTestHelper.flushEdiTestDirectory();
    }

    public void configureInitialLogging(String foldername) {
        System.out.println(GSLogbackProperties.toStringsAll());
        String xml =
                "<configuration scan='true' scanPeriod='500'> <!-- in millis -->\n" +
                        "    <property name='gslog.console' scope='system' value='true'/>\n" +
                        "    <property name='gslog.file' scope='system' value='false'/>\n" +
                        "    <include file='" + foldername + "AppConfig\\systemtest\\config\\gslogback/gslog-configuration.xml'/>\n" +
                        "</configuration>";
        GSXMLContext gsxmlContext = new GSXMLContext();
        gsxmlContext.parseString(xml);
        File file = new File(testFolderName + File.separator + GSLogbackTestUtil.class.getSimpleName() + "-configuration-af-test-logging.xml");
        gsxmlContext.serializeAsFile(file);
        GSLogbackProperties.clear();
        GSLogbackProperties.GSLOG_SERVER.setValue(testFolderName);
        new GSLogbackConfigurator()
                .addLogListner(s -> System.out.println(s))
                .reset(file);

    }
}
