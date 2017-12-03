package torsete.logback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Torsten on 28.11.2017.
 */
public class LogConfigurationDemo {
    private final static Logger log = LoggerFactory.getLogger(LogConfigurationDemo.class);

    public static void main(String... args) {
        new LogConfigurationDemo().run();
    }

    private void run() {
//        runPlain();
//        runLongPlain();
//        runConfigurationWithUserName();
        runWithConfiguration();
    }

    private void runPlain() {
        doSomeWork(2);
    }

    private void runLongPlain() {
        doSomeWork(20000);
    }

    private void runConfigurationWithUserName() {
        log.info("Brugernavn skal lige bestemmes... ");

        new GSLogConfigurator()
                .acceptSettings(s -> s.setUser("USRxxx"))
                .configure();

        doSomeWork(2);
    }

    private void runWithConfiguration() {
        new GSLogConfigurator()
                .acceptSettings(s -> s
                        .setHome("myhome")
                        .setVersion("myVersion")
                        .setEnvironment("myenvironment")
                        .setDatabase("mydatabase")
                        .setSystem("mysystem")
                        .setApplication(getClass().getSimpleName())
                )
                .configure();

        log.info("Brugernavn skal lige bestemmes... ");

        new GSLogConfigurator()
                .acceptSettings(s -> s.setUser("USRxxx"))
                .configure();

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
