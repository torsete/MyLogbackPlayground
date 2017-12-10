package torsete.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import torsete.gslogback.GSLogbackConfigurator;
import torsete.gslogback.GSLogbackSettings;

/**
 * Created by Torsten on 28.11.2017.
 */
public class LogbackConfigurationDemo {
    private final static Logger log = LoggerFactory.getLogger(LogbackConfigurationDemo.class);

    public static void main(String... args) {
        log.info("HostName=" + new GSLogbackSettings().getHostName());
        log.info("ContextName=" + new GSLogbackSettings().getContextName());

        new LogbackConfigurationDemo().run();
    }

    private void run() {
//        runPlain();
        runLongPlain();
//        runLongPlainWithScan();
//        runConfigurationWithUserName();
//        runWithConfiguration();
    }

    private void runPlain() {
        doSomeWork(2);
    }

    private void runLongPlain() {
        doSomeWork(20000);
    }

    private void runLongPlainWithScan() {
//        new GSLogConfigurator().configure(true);
        doSomeWork(20000);
    }

    private void runConfigurationWithUserName() {
        log.info("Brugernavn skal lige bestemmes... ");

        new GSLogbackConfigurator()
                .acceptSettings(s -> s.setUser("USRxxx"))
                .configure(true);

        doSomeWork(2);
    }

    private void runWithConfiguration() {
        new GSLogbackConfigurator()
                .acceptSettings(s -> s
                        .setHome("myhome")
                        .setVersion("myVersion")
                        .setEnvironment("myenvironment")
                        .setDatabase("mydatabase")
                        .setSystem("mysystem")
                        .setApplication(getClass().getSimpleName())
                )
                .configure(true);

        log.info("Brugernavn skal lige bestemmes... ");

        new GSLogbackConfigurator()
                .acceptSettings(s -> s.setUser("USRxxx"))
                .configure(true);

        doSomeWork(2);
    }


    private void doSomeWork(int repeat) {
        for (int i = 0; i < repeat; i++) {
            log.debug("Some debugging... " + i);
            log.info("Some info... " + i);

            if (i % 10 == 0) {
                log.error("Some errors... " + i);
            }
            pause(500);
        }
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
