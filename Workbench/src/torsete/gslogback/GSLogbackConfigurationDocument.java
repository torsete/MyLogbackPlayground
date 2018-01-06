package torsete.gslogback;

import org.w3c.dom.Element;
import torsete.util.GSDocument;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Indeholder metoder der sigter mod konfiguration af Logback til at
 */
public class GSLogbackConfigurationDocument extends GSDocument {

    private HashMap<String, Element> appenderElements; // filname,appenderelement Intent: Avoid that two or more appenders uses the same file name

    protected GSLogbackConfigurationDocument(String rootTagName) {
        this(rootTagName, new HashMap<>());
    }

    protected GSLogbackConfigurationDocument(String rootTagName, HashMap<String, Element> appenderElements) {
        super(rootTagName);
        this.appenderElements = appenderElements;
    }

    public Element addFileAppender(String appenderName, String fileName) {
        Element appenderElement = appenderElements.get(fileName);
        if (appenderElement != null) {
            return appenderElement;
        }

        appenderElement = appendElement(getDocumentElement(), "appender",
                "name", appenderName,
                "class", "ch.qos.logback.core.FileAppender");
        appendTextElement(appenderElement, "file", fileName);
        Element encoderElement = appendElement(appenderElement, "encoder");
        appendTextElement(encoderElement, "pattern", GSLogbackProperties.GSLOG_PATTERN.getValue());
        appenderElements.put(fileName, appenderElement);
        return appenderElement;
    }

    public Element setConsoleAppender(String appenderName) {
        Element appenderElement = appenderElements.get("");
        if (appenderElement != null) {
            return appenderElement;
        }
        appenderElement = appendElement(getDocumentElement(), "appender", "name", appenderName, "class", "ch.qos.logback.core.ConsoleAppender");
        Element encoderElement = appendElement(appenderElement, "encoder");
        appendTextElement(encoderElement, "pattern", GSLogbackProperties.GSLOG_PATTERN.getValue());
        appenderElements.put("", appenderElement);
        return appenderElement;
    }

    public Element addFilter(Element parentElement, String level, String onMatch, String onMismatch) {
        Element filterElement = appendElement(parentElement, "filter", "class", "ch.qos.logback.classic.filter.LevelFilter");
        appendTextElement(filterElement, "level", level);
        appendTextElement(filterElement, "onMatch", onMatch);
        appendTextElement(filterElement, "onMismatch", onMismatch);
        return filterElement;
    }


    public Element addLogger(String appenderName, String level, String loggerName, boolean additivity) {
        Element loggerElement = appendElement(getDocumentElement(), "logger",
                "name", loggerName,
                "level", level,
                "additivity", additivity + "");
        appendElement(loggerElement, "appender-ref", "ref", appenderName);
        return loggerElement;
    }

    public Element addRootLevel(String level, String... appenderNames) {
        Element rootElement = appendElement(getDocumentElement(), "root", "level", level);
        Arrays.stream(appenderNames).forEach(appenderName -> appendElement(rootElement, "appender-ref", "ref", appenderName));
        return rootElement;
    }

    public Element addInclude(String fileName) {
        Element includeElement = appendElement(getDocumentElement(), "include", "file", fileName);
        return includeElement;
    }

    /**
     * Tilføjer loggers med de aktuelle settings
     *
     * @param loggerNames
     */
    public GSLogbackConfigurationDocument addLoggers(String... loggerNames) {
        addLoggers(false, loggerNames);
        return this;
    }

    private String rootLevel;
    private String filenameWithExtension;

    public GSLogbackConfigurationDocument setRootLevel(String rootLevel) {
        this.rootLevel = rootLevel;
        return this;
    }

    public GSLogbackConfigurationDocument setFilenameWithExtension(String filenameWithExtension) {
        this.filenameWithExtension = filenameWithExtension;
        return this;    }

    /**
     * Tilføjer root logger med de aktuelle settings
     */
    public GSLogbackConfigurationDocument addRootLogger() {
        if (rootLevel != null) {
            Element appenderElement = addAppender();
            String appenderName = appenderElement.getAttribute("name");
            addRootLevel(rootLevel, appenderName);
        } else {
            addRootLevel("OFF");
        }
        return this;
    }

    public GSLogbackConfigurationDocument include(String fileName) {
        addInclude(fileName);
        return this;
    }

    Element addConsoleAppender() {
        return setConsoleAppender("gslog.consoleAppender");
    }

    Element addFileAppender(String filename) {
        return addFileAppender("gslog.fileAppender" + new Date().getTime(), filename);
    }

    Element addLoggers(boolean additivity, String... loggerNames) {
        Element appenderElement = addAppender();
        String appenderName = appenderElement.getAttribute("name");
        Arrays.stream(loggerNames).forEach(loggerName -> addLogger(appenderName, rootLevel, loggerName, additivity));
        return appenderElement;
    }

    Element addAppender() {
        return filenameWithExtension == null ? addConsoleAppender() : addFileAppender(filenameWithExtension);
    }

}
