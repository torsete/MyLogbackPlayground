package torsete.util;

import java.nio.charset.Charset;

/**
 * Encoding i {@link GSXMLContext}
 */
public enum GSXMLEncoding {
    UTF8("UTF-8"),
    USASCII("US-ASCII"),
    ISO88591("ISO-8859-1");

    /**
     * Identifikation af en encoding som den ser ud i XML.
     */
    private String xmlEncodingName_;

    GSXMLEncoding(String pXmlName) {
        xmlEncodingName_ = pXmlName;
    }

    public Charset getCharset() {
        Charset charset = Charset.forName(xmlEncodingName_);
        return charset;
    }

    public String getXmlEncodingName() {
        return xmlEncodingName_;
    }

    /**
     * Finder den encoding der svarer til en given værdi af koden for encoding i et XML-dokument (i en String eller en File)
     *
     * @param pXmlEncodingName Fx. "UTF-8". Må ikke være null
     * @return Null hvis der ikke findes nogen tilsvarende encoding
     */
    public static GSXMLEncoding getEncoding(String pXmlEncodingName) {
        for (GSXMLEncoding encoding : values()) {
            if (pXmlEncodingName.equalsIgnoreCase(encoding.getXmlEncodingName())) {
                return encoding;
            }
        }
        return null;
    }
}
