package torsete.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Omgivelser for et {@link GSDocument}.
 * <p>
 * Håndterer følgende for et{@link GSDocument}:
 * <ul>
 * <li>Parsning
 * <li>Serialisering
 * <li>Prettyprint
 * <li>Validering
 * <li>"Eksterne forhold" generelt. Dvs. filer og bytes, tegnsæt og encoding, etc.
 * </ul>
 * Klassen har alene at gøre med "omgivelserne" for et {@link GSDocument}.
 * Den kender hverken Gensafe Pro-begreber eller {@link Document}. Håndtering et {@link Document} skal ske med  {@link GSDocument}
 */
public class GSXMLContext {
    private final static Logger log_ = LoggerFactory.getLogger(GSXMLContext.class);

    public static final boolean DEFAULT_SERIALIZE_WITH_XML_HEADER = true;
    public static final boolean DEFAULT_PARSE_COALESCING_ENABLED = true;
    public static final boolean DEFAULT_PARSE_EXTERNALENTITYINJECTION_ENABLED = false;
    public static final String DEFAULT_VALIDATE_SCHEMALANGUAGE = XMLConstants.W3C_XML_SCHEMA_NS_URI;

    /**
     * Det GSXMLDocument der er aktuelt i sammenhængen
     */
    protected GSDocument gsDocument_;

    /**
     * Har betydning ifm. serialisering.
     * Den encoding der vil blive benyttet i en serialisering
     */
    protected GSXMLEncoding serializeEncoding_;

    /**
     * Har betydning ifm. serialisering.
     * Skal være true hvis xml-header skal med, false ellers.
     */
    protected boolean isSerializeWithXmlHeader_;

    /**
     * Bestemmer hvordan der serialiseres.
     * <p>
     * XML er "ægte" xml.
     * <p>
     * HTML er et tilpasset xml-format der er er egnet som - ja HTML. Fx er der ingen "self closing tags".
     * Hvor XML ville serialisere til (eksempelvis) "<textarea/>" vil HTML serialisere til "<textarea></textarea>".
     * <p>
     * PRETTY_HTML er som HTML, blot "prettyprintet", dvs. med linieskift og indrykning på passsende steder
     * <p>
     * TEXT vil kun serialisere tekstindhold. Dvs. "flad tekst" uden elementnavne.
     */
    public enum GSXMLSerializeMethod {
        XML("xml"),
        HTML("html"),
        PRETTY_HTML("html"),
        TEXT("text");

        private String transformerOutputKeyValue_;

        GSXMLSerializeMethod(String pTransformerOutputKeyValue) {
            transformerOutputKeyValue_ = pTransformerOutputKeyValue;
        }

        public String getKeyValue() {
            return transformerOutputKeyValue_;
        }
    }

    /**
     * Bestemer hvordan der serialiseres. Se Se {@link GSXMLSerializeMethod}
     */
    protected GSXMLSerializeMethod serializeMethod_;

    /**
     * Har betydning ifm. parse.
     * Skal være true for at evt. "CDATA nodes" vil blive konverteret til "Text nodes"
     */
    protected boolean isParseCoalescing_;

    /**
     * Har betydning ifm. parse.
     * True hvis "XML External Entity Injection" skal være mulig (frarådes).
     * False hvis "XML External Entity Injection" ikke skal være mulig (tilrådes)
     */
    protected boolean isParseExternalEntityInjectionEnabled_;

    /**
     * Har betydning ifm. validate.
     * Den aktuelle schema language. Eksempelvis XMLConstants.W3C_XML_SCHEMA_NS_URI
     */
    protected String validateSchemaLanguage_;

    /**
     * Instantierer med et specifikt xml-dokument.
     *
     * @param pGSDocument Se {@link #gsDocument_}
     */
    public GSXMLContext(GSDocument pGSDocument) {
        enableSerializeWithXmlHeader(DEFAULT_SERIALIZE_WITH_XML_HEADER);
        setSerializeMethod(GSXMLSerializeMethod.XML);
        setGSDocument(pGSDocument);
        enableParseCoalescing(DEFAULT_PARSE_COALESCING_ENABLED);
        enableParseExternalEntityInjection(DEFAULT_PARSE_EXTERNALENTITYINJECTION_ENABLED);
        setValidateSchemaLanguage(DEFAULT_VALIDATE_SCHEMALANGUAGE);
    }

    /**
     * Instantierer med et specifikt {@link org.w3c.dom.Document}.
     *
     * @param pDocument
     */
    public GSXMLContext(Document pDocument) {
        this(new GSDocument(pDocument));
    }

    /**
     * Instantierer med et tomt xml-dokument. Encoding af serialisering bliver UTF-8.
     */
    public GSXMLContext() {
        this(new GSDocument());
    }

    /**
     * Se {@link #gsDocument_}
     *
     * @param pGSDocument
     * @return den aktuelle instans af {@link GSXMLContext} selv.
     */
    public GSXMLContext setGSDocument(GSDocument pGSDocument) {
        gsDocument_ = pGSDocument;
        return this;
    }

    /**
     * Se {@link #gsDocument_}
     */
    public GSDocument getGSDocument() {
        return gsDocument_;
    }

    /**
     * Returnerer den encoding som blev parset.
     *
     * @return Null hvis der ikke er blevet parset, eller hvis der ikke blev registreret en encoding ifm. parse
     */
    public GSXMLEncoding getParseEncoding() {
        if (gsDocument_ != null) {
            String inputEncoding = gsDocument_.getDocument().getInputEncoding();
            if (inputEncoding != null) {
                return GSXMLEncoding.getEncoding(inputEncoding);
            }
        }
        return null;
    }

    /**
     * Sætter den encoding der skal serialiseres med. Se {@link #serializeEncoding_}
     *
     * @param pSerializeEncoding Kan være null
     * @return den aktuelle instans af {@link GSXMLContext} selv.
     */
    public GSXMLContext setSerializeEncoding(GSXMLEncoding pSerializeEncoding) {
        serializeEncoding_ = pSerializeEncoding;
        return this;
    }

    /**
     * Bestemer hvordan der serialiseres. Se Se {@link GSXMLSerializeMethod}
     *
     * @param pSerializeMethod
     */
    public GSXMLContext setSerializeMethod(GSXMLSerializeMethod pSerializeMethod) {
        serializeMethod_ = pSerializeMethod;
        return this;
    }

    /**
     * Se {@link #isParseCoalescing_}
     *
     * @param pEnabled
     * @return Den aktuelle instans af {@link GSXMLContext} selv.
     */
    public GSXMLContext enableParseCoalescing(boolean pEnabled) {
        isParseCoalescing_ = pEnabled;
        return this;
    }

    /**
     * Se {@link #isSerializeWithXmlHeader_}
     *
     * @param pEnabled
     * @return Den aktuelle instans af {@link GSXMLContext} selv.
     */
    public GSXMLContext enableSerializeWithXmlHeader(boolean pEnabled) {
        isSerializeWithXmlHeader_ = pEnabled;
        return this;
    }

    /**
     * Se {@link #isParseExternalEntityInjectionEnabled_}
     *
     * @param pEnabled
     * @return Den aktuelle instans af {@link GSXMLContext} selv.
     */
    public GSXMLContext enableParseExternalEntityInjection(boolean pEnabled) {
        isParseExternalEntityInjectionEnabled_ = pEnabled;
        return this;
    }

    /**
     * Se {@link #validateSchemaLanguage_}
     *
     * @param pSchemaLanguage
     * @return Den aktuelle instans af {@link GSXMLContext} selv.
     */
    public GSXMLContext setValidateSchemaLanguage(String pSchemaLanguage) {
        validateSchemaLanguage_ = pSchemaLanguage;
        return this;
    }

    /**
     * Serialiserer ud på en String
     * <p>
     * Encoding af den dannede String bliver et subset af UTF-16 svarende til den aktuelle encoding. XML-headeren vil indeholde den aktuelle encoding.
     *
     * @return En serialiseret udgave af det aktuelle xml-dokument
     */
    public String serializeAsString() {
        DOMSource domSource = getSerializeDomSource(gsDocument_.getDocument());
        Writer writer = new StringWriter();
        StreamResult streamResult = new StreamResult(writer);
        try {
            Transformer transformer = getSerializeTransformer();
            transformer.transform(domSource, streamResult);
            return writer.toString();
        } catch (TransformerException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Serialiserer ud på et array af bytes med den aktuelle encoding
     *
     * @return En serialiseret udgave af det aktuelle xml-dokument
     */
//	@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
//			value = {"DM_DEFAULT_ENCODING"},
//			justification = "Keep Teamcity happy")
    public byte[] serializeAsBytes() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        serializeAsStream(byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return bytes;
    }

    /**
     * Serialiserer ud på en fil. Filens indhold får den aktuelle encoding
     *
     * @param pFile Den fil der skal serialiseres til
     * @return Filen med den serialiserede udgave af det aktuelle xml-dokument
     */
    public File serializeAsFile(File pFile) {
        DOMSource domSource = getSerializeDomSource(gsDocument_.getDocument());
        StreamResult streamResult = new StreamResult(pFile);
        try {
            Transformer transformer = getSerializeTransformer();
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            throw new GSXMLContextException(e);
        }
        return pFile;
    }

    /**
     * Serialiserer ud på en output stream. Indhold får den aktuelle encoding
     *
     * @param pOutputStream Den output stream der skal serialisers til
     * @return Output stream'en med den serialiserede udgave af det aktuelle xml-dokument
     */
    public OutputStream serializeAsStream(OutputStream pOutputStream) {
        DOMSource domSource = getSerializeDomSource(gsDocument_.getDocument());
        StreamResult streamResult = new StreamResult(pOutputStream);
        try {
            Transformer transformer = getSerializeTransformer();
            transformer.transform(domSource, streamResult);
        } catch (TransformerConfigurationException e) {
            throw new GSXMLContextException(e);
        } catch (TransformerException e) {
            throw new GSXMLContextException(e);
        }
        return pOutputStream;
    }

    public InputStream getInputStream() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializeAsStream(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return inputStream;
    }


    /**
     * Prettyprinter det aktuelle xml-dokument.
     *
     * @return Prettyprint i et læseligt format. Dvs. med "passende" indrykninger og linieskift, og med en "læselig" encoding (UTF-16)
     */
    public String prettyprint() {
        return prettyprint(gsDocument_.getDocument(), 2);
    }

    /**
     * Danner et sæt af (key,value)-par, hvor key er Element-navne og value er attributindhold og tekstindhold
     * <p>
     * Formålet er at kunne vise en "lidt mere flad tekst" end fx {@link #prettyprint()} kan.
     * Det kan fx være nyttigt i toString()-metoder.
     *
     * @return Aldrig null
     */
    public String printTextContent() {
        return printTextContent(gsDocument_.getDocumentElement());
    }

    /**
     * Danner et sæt af (key,value)-par, hvor key er Element-navne og value er attributindhold og tekstindhold
     * <p>
     * Formålet er at kunne vise en "lidt mere flad tekst" end fx {@link #prettyprint()} kan.
     * Det kan fx være nyttigt i toString()-metoder.
     *
     * @param pNode
     * @param pDelimiters Valgfri. 1. element er "lighedstegn". 2. og 3. element er start og slut på attributvisning. Default er "=", "(" og ")".
     * @return Aldrig null
     */
    public String printTextContent(Node pNode, String... pDelimiters) {
        StringBuilder sb = new StringBuilder();
        printNodeTextContent(sb, 0, pNode, pDelimiters);
        return sb.length() == 0 ? "" : sb.substring(1);
    }

    /**
     * Danner et (key,value)-par, hvor key er Element-navne og value er attributindhold og tekstindhold
     *
     * @param pSb          Buffer der opsamler resultatet
     * @param pIndentIndex Nesting level
     * @param pNode        Det element der der printes
     * @param pDelimiters  Valgfri. 1. element er "lighedstegn". 2. og 3. element er start og slut på attributvisning. Default er "=", "(" og ")".
     */
    protected void printNodeTextContent(StringBuilder pSb, int pIndentIndex, Node pNode, String... pDelimiters) {

        /**
         * Forbered en ny linie til at vise elementet
         */
        pSb.append("\n");
        for (int i = 0; i < pIndentIndex; i++) {
            pSb.append("  ");
        }

        /**
         * Dan visning af elementets tekstindhold og attributter:
         */
        String elementString = printNodeContentString(pNode, pDelimiters);
        String attributesString = printAttributeContentString(pNode, pDelimiters);

        /**
         * Placerer resultatet i linien:
         */
        pSb.append(pNode.getNodeName() + (attributesString.length() == 0 ? "" : " ") + attributesString + elementString);

        /**
         * Og behandl afsluttende alle Element child nodes:
         */
        NodeList childNodes = pNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node instanceof Element) {
                printNodeTextContent(pSb, pIndentIndex + 1, node, pDelimiters);
            }
        }

    }

    /**
     * Danner en String der viser "tagname=textcontent"
     *
     * @param pNode
     * @param pDelimiters Valgfri. 1. element er "lighedstegn". Default er "=".
     * @return Fx. "regnr=XX12345". Aldrig null.
     */
    public String printNodeContentString(Node pNode, String... pDelimiters) {
        String equalSymbol = pDelimiters.length > 0 ? pDelimiters[0] : null;
        if (equalSymbol == null) {
            equalSymbol = "=";
        }
        StringBuilder textSb = new StringBuilder();
        NodeList elementChildNodes = pNode.getChildNodes();
        for (int i = 0; i < elementChildNodes.getLength(); i++) {
            Node elementChildNode = elementChildNodes.item(i);
            if (elementChildNode instanceof Text) {
                textSb.append(equalSymbol);
                textSb.append(elementChildNode.getNodeValue());
                equalSymbol = "";
            }
        }
        return textSb.toString();
    }

    /**
     * Danner en string der viser alle attributter og deres indhold
     *
     * @param pNode
     * @param pDelimiters Valgfri. 1. element er "lighedstegn". 2. og 3. element er start og slut på attributvisning. Default er "=", "(" og ")".
     * @return Fx. "(att1="val",att2="val2")". Aldrig null.
     */
    public String printAttributeContentString(Node pNode, String... pDelimiters) {
        String equalSymbol = pDelimiters.length > 0 ? pDelimiters[0] : null;
        String beginSymbol = pDelimiters.length > 1 ? pDelimiters[1] : null;
        String endSymbol = pDelimiters.length > 2 ? pDelimiters[2] : null;
        if (equalSymbol == null) {
            equalSymbol = "=";
        }
        if (beginSymbol == null) {
            beginSymbol = "(";
        }
        if (endSymbol == null) {
            endSymbol = ")";
        }
        StringBuilder attributesSb = new StringBuilder();

        NamedNodeMap attributeNodeMap = pNode.getAttributes();
        String dlm = beginSymbol;
        for (int i = 0; i < attributeNodeMap.getLength(); i++) {
            String name = attributeNodeMap.item(i).getNodeName();
            String value = attributeNodeMap.item(i).getNodeValue();
            attributesSb.append(dlm);
            attributesSb.append(name);
            attributesSb.append(equalSymbol);
            attributesSb.append("\"");
            attributesSb.append(value.replace('"', '\''));
            attributesSb.append("\"");
            dlm = ",";
        }
        if (attributesSb.length() > 0) {
            attributesSb.append(endSymbol);
        }

        return attributesSb.toString();
    }

    /**
     * Prettyprinter en XML-node
     * <p>
     * Det er en hjælpemetode for {@link GSXMLContext} selv.
     * Den bør overskrives hvis det er nødvendigt med en anden funktionalitet til prettyprint.
     *
     * @param pNode   XML-node
     * @param pIndent Indrykning (antal positioner pr. nyt niveau af Element)
     * @return Prettyprint i et læseligt format. Dvs. med "passende" indrykninger og linieskift, og med en "læselig" encoding (UTF-16)
     */
    protected String prettyprint(Node pNode, int pIndent) {
        DOMSource domSource = getSerializeDomSource(pNode);
        Writer writer = new StringWriter();
        StreamResult streamResult = new StreamResult(writer);
        try {
            Transformer transformer = getSerializeTransformer();
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + pIndent);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(domSource, streamResult);
            return writer.toString();
        } catch (Exception e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Prettyprinter en XML-node
     * <p>
     * Metoden kan overskrives hvis det er nødvendigt med en anden funktionalitet til prettyprint.
     *
     * @param pNode XML-node
     * @return Prettyprint i et læseligt format. Dvs. med "passende" indrykninger og linieskift, og med en "læselig" encoding (UTF-16)
     */
    public String prettyprint(Node pNode) {
        return prettyprint(pNode, 2);
    }

    /**
     * Parser en String og gør indholdet til det aktuelle xml-dokument
     * <p>
     * Indhold i input-String skal være encoded med UTF-16 (standard Java encoding). Men xml-headeren må godt angive en anden encoding
     *
     * @param pString
     * @return Det aktuelle nye xml-dokument
     * @throws GSXMLContextException (med getCause()==ParserConfigurationException)
     *                               Hvis der potentielt er mulighed for "XML External Entity Injection"
     *                               (kræver dog at {@link #isParseExternalEntityInjectionEnabled_} er sat til true
     *                               (fx med {@link #enableParseExternalEntityInjection(boolean)}))
     */
    public GSDocument parseString(String pString) {
        try {
            DocumentBuilder builder = getParseDocumentBuilder();
            StringReader stringReader = new StringReader(pString);
            InputSource inputSource = new InputSource(stringReader);
            Document document = builder.parse(inputSource);
            gsDocument_ = new GSDocument(document);
            return gsDocument_;
        } catch (SAXException | IOException e) {
            throw new GSXMLContextException(e);
        }
    }


    /**
     * Parser et array af bytes og gør indholdet til det aktuelle xml-dokument
     * <p>
     * Indhold skal være encoded med UTF-8, US-ASCII eller ISO8955-1 (jf. {@link GSXMLEncoding}
     *
     * @param pBytes
     * @return Det aktuelle nye xml-dokument
     * @throws GSXMLContextException (med getCause()==ParserConfigurationException)
     *                               Hvis der potentielt er mulighed for "XML External Entity Injection"
     *                               (kræver dog at {@link #isParseExternalEntityInjectionEnabled_} er sat til true
     *                               (fx med {@link #enableParseExternalEntityInjection(boolean)}))
     */
    public GSDocument parseBytes(byte[] pBytes) {
        InputStream inputStream = new ByteArrayInputStream(pBytes);
        return parseStream(inputStream);
    }

    /**
     * Parser en fil og gør indholdet til det aktuelle xml-dokument
     * <p>
     * Indhold skal være encoded med UTF-8, US-ASCII eller ISO8955-1 (jf. {@link GSXMLEncoding}
     *
     * @param pFile
     * @return Det aktuelle nye xml-dokument
     * @throws GSXMLContextException (med getCause()==ParserConfigurationException)
     *                               Hvis der potentielt er mulighed for "XML External Entity Injection"
     *                               (kræver dog at {@link #isParseExternalEntityInjectionEnabled_} er sat til true
     *                               (fx med {@link #enableParseExternalEntityInjection(boolean)}))
     */
    public GSDocument parseFile(File pFile) {
        try {
            InputStream inputStream = new FileInputStream(pFile);
            return parseStream(inputStream);
        } catch (FileNotFoundException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Parser en input stream og gør indholdet til det aktuelle xml-dokument
     * <p>
     * Indhold skal være encoded med UTF-8, US-ASCII eller ISO8955-1 (jf. {@link GSXMLEncoding}
     *
     * @param pInputStream
     * @return Det aktuelle nye xml-dokument
     * @throws GSXMLContextException (med getCause()==ParserConfigurationException)
     *                               Hvis der potentielt er mulighed for "XML External Entity Injection"
     *                               (kræver dog at {@link #isParseExternalEntityInjectionEnabled_} er sat til true
     *                               (fx med {@link #enableParseExternalEntityInjection(boolean)}))
     */
    public GSDocument parseStream(InputStream pInputStream) {
        try {
            DocumentBuilder builder = getParseDocumentBuilder();
            Document document = builder.parse(pInputStream);
            gsDocument_ = new GSDocument(document);
            return gsDocument_;
        } catch (SAXException | IOException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Parser en Map-struktur til et nyt xml-dokument
     *
     * @param documentElementName Navn på rod-element i det resulterende xml-dokument
     * @param object              Map-struktur i form af en objektgraf bestående af MAP-, List og String-objekter
     * @return Det aktuelle nye xml-dokument
     */
    public GSDocument parseMapStructure(String documentElementName, Object object) {
        gsDocument_ = new GSDocument(documentElementName);
        parseMapStructure(gsDocument_.getDocumentElement(), object);
        return gsDocument_;
    }

    /**
     * Parser en Map-struktur til en eksisterende Node
     *
     * @param parentNode
     * @param object     Map-struktur i form af en objektgraf bestående af MAP-, List og String-objekter
     * @return Den eksisternde Node, nu med tilføjede childs. Aldrig null
     */
    public Node parseMapStructure(Node parentNode, Object object) {
        if (object instanceof String) {
            String string = (String) object;
            gsDocument_.appendText(parentNode, string);
        } else if (object instanceof List) {
            List list = (List) object;
            Element listElement = gsDocument_.createElement(object.getClass().getSimpleName());
            parentNode.appendChild(listElement);
            list.forEach(child -> {
                parseMapStructure(listElement, child);
            });
        } else if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            Element mapElement = gsDocument_.createElement(object.getClass().getSimpleName());
            parentNode.appendChild(mapElement);
            map.entrySet().stream()
                    .forEach(child -> {
                                Element childElement = gsDocument_.createElement(child.getKey());
                                mapElement.appendChild(childElement);
                                parseMapStructure(childElement, child.getValue());
                            }
                    );
        } else if (object instanceof Consumer) {
            Consumer consumer = (Consumer) object;
            consumer.accept(parentNode);
        } else {
            throw new IllegalArgumentException("Uventet klasse " + object.getClass().getName());
        }

        return parentNode;
    }

    /**
     * Afgører om et tegn er et gyldigt xml-tegn der både kan parses og serialiseres (i xml-version 1.0)
     *
     * @param pChar Et hvilkensomhelst tegn
     * @return True hvis gyldigt xml-tegn, false ellers.
     */
    public boolean isValidXmlCharacter(char pChar) {
        char c = pChar;
        if (0 <= c && c <= 8 ||
                11 <= c && c <= 12 ||
                14 <= c && c <= 31 ||
                56320 <= c && c <= 57343 ||
                65534 <= c && c <= 65535) {
            /**
             * Document:ok Serialisering:ok Parse:exception (1.055 tegn)
             */
            return false;
        }
        if (55296 <= c && c <= 56319) {
            /**
             * Document:ok Serialisering:exception (1.024 tegn)
             */
            return false;
        }
        /**
         * Document:ok Serialisering:ok Parse:ok
         */
        return true;
    }

    /**
     * Danner et schema fra en xsd-fil
     *
     * @param pFile
     * @return Aldrig null
     * @throws SAXException Hvis et schema ikke kunne dannes
     */
    public Schema getSchema(File pFile) throws SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(validateSchemaLanguage_);
        Schema schema = factory.newSchema(pFile);
        return schema;
    }

    /**
     * Validerer det aktuelle xml-dokument i forhold til et xml-skema
     *
     * @param pSchema Det schema der valideres op imod
     * @throws SAXException Hvis det aktuelle xml-dokuemnt er invalid
     */
    public void validate(Schema pSchema) throws SAXException {
        try {
            Validator validator = pSchema.newValidator();
            InputStream inputStream = new ByteArrayInputStream(serializeAsBytes());
            Source source = new StreamSource(inputStream);
            validator.validate(source);
        } catch (IOException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Validerer det aktuelle xml-dokument i forhold til et xml-skema
     *
     * @param pSchemaFile Den fil der indeholder det schema der valideres op imod
     * @throws SAXException Hvis det aktuelle xml-dokuemnt er invalid
     */
    public void validate(File pSchemaFile) throws SAXException {
        Schema schema = getSchema(pSchemaFile);
        validate(schema);
    }


    /**
     * Returnerer den {@link javax.xml.parsers.DocumentBuilder} der som default skal benyttes når der parses.
     * <p>
     * Det er en hjælpemetode for {@link GSXMLContext} selv.
     * Den bør overskrives hvis det er nødvendigt med en anden anden {@link javax.xml.parsers.DocumentBuilder}.
     *
     * @throws GSXMLContextException (med getCause()==ParserConfigurationException)
     *                               Hvis der potentielt er mulighed for "XML External Entity Injection"
     *                               (kræver dog at {@link #isParseExternalEntityInjectionEnabled_} er sat til true
     *                               (fx med {@link #enableParseExternalEntityInjection(boolean)}))
     */
    protected DocumentBuilder getParseDocumentBuilder() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            if (!isParseExternalEntityInjectionEnabled_) {
                disableExternalEntityInjection(documentBuilderFactory);
            }
            documentBuilderFactory.setCoalescing(isParseCoalescing_);
            return documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * En hjælpemetode for {@link GSDocument} selv.
     * Den kan overskrives hvis det er nødvendigt med en anden funktionalitet,
     * men bør aldrig benyttes på anden måde.
     * <p>
     * Danner en {@link DOMSource} ud fra et {@link GSDocument}.
     * Hvis {@link Document} i {@link GSDocument} har en input encoding der er forskellig fra den angivne encoding
     * vil en serialisering af {@link Document} ikke får den angivne encoding.
     * Det betyder at det bliver {@link GSXMLContext} der kan bestemme encoding
     * i en serialisering, og ikke {@link Document}} (hvis {@link Document}} har en "inputEncoding" != null vil denne
     * encoding altid overrule den encoding der måtte blive sat i en serialisering ved hjælp af {@link Transformer}).
     *
     * @return Aldrig null
     */
    protected DOMSource getSerializeDomSource(Node pNode) {
        String inputEncoding = gsDocument_.getDocument().getInputEncoding();
        if (inputEncoding != null && serializeEncoding_ != null && !serializeEncoding_.getXmlEncodingName().equals(inputEncoding)) {
            /**
             * Sikrer at det bliver {@link #serializeEncoding_} der serialiseres med
             */
            return new DOMSource(pNode == gsDocument_.getDocument() ? gsDocument_.clone().getDocument() : gsDocument_.clone(pNode));
        }
        return new DOMSource(pNode);
    }


    protected Transformer getSerializeTransformer() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            if (serializeEncoding_ != null) {
                transformer.setOutputProperty(OutputKeys.ENCODING, serializeEncoding_.getXmlEncodingName());
            }
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, isSerializeWithXmlHeader_ ? "no" : "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, serializeMethod_.getKeyValue());
            if (serializeMethod_ == GSXMLSerializeMethod.PRETTY_HTML) {
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "" + 2);
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            }
            return transformer;
        } catch (TransformerConfigurationException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Instantierer en Writer som vil skrive på en fil med en specifik encoding.
     * <p>
     * Metoden kan benyttes når der er tale om XML der ikke ligger i et {@link org.w3c.dom.Document}.
     * Fx ifm. SAX og {@link javax.xml.stream.XMLOutputFactory}
     *
     * @param pFilename Navn på fil hvorpå den instantierede writer vil skrive
     * @return Aldrig null
     */
    public Writer createWriter(String pFilename, GSXMLEncoding pEncoding) {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pFilename), pEncoding.getXmlEncodingName()));
        } catch (UnsupportedEncodingException e) {
            throw new GSXMLContextException(e);
        } catch (FileNotFoundException e) {
            throw new GSXMLContextException(e);
        }
    }

    /**
     * Fjerner muligheden for "XML External Entity Injection" som kan give adgang til lokale filer mv. via xml-parseren
     * <p>
     * Det er en hjælpemetode for {@link GSXMLContext} selv.
     * Den bør overskrives hvis det er nødvendigt med en anden funktionalitet til at tjekke for "XML External Entity Injection".
     *
     * @param pDocumentBuilderFactory
     * @throws ParserConfigurationException Hvis der potentielt er mulighed for "XML External Entity Injection"
     */
    protected void disableExternalEntityInjection(DocumentBuilderFactory pDocumentBuilderFactory) throws ParserConfigurationException {
        pDocumentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        pDocumentBuilderFactory.setXIncludeAware(false);
        pDocumentBuilderFactory.setExpandEntityReferences(false);
        pDocumentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        pDocumentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
    }
}
