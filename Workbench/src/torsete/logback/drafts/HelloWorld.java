package torsete.logback.drafts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Torsten on 28.11.2017.
 */
public class HelloWorld {
    private final static Logger log = LoggerFactory.getLogger(HelloWorld.class);

    public HelloWorld() {
        log.debug("Hello debug");
        log.info("Hello info");
    }

    public static void main(String... args) {
        new HelloWorld();
    }
}
