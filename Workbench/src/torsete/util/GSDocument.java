package torsete.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Indkapsling af et {@link Document}. Klassen tilbyder f�lgende kategorier af metoder:
 * <ul>
 * <li>Gennemstilling til metoder i et {@link Document}. Fx. {@link #getDocumentElement()}.
 * <li>Bekvemmelighedsmetoder til at opbygge et {@link Document}. Fx. {@link #appendTextElement(Node, String, String, String...)} ()} og {@link #clone()}.
 * <li>Hj�lpemetoder til s�gning og manipulation i et {@link Document}. Fx. {@link #trim()} og {@link #getElementsByTagName(String, boolean)}.
 * </ul>
 * Alle String-data b�r v�re i standard Java encoding (UTF-16).
 * <p>
 * Forhold som tegns�t, encoding, serialisering, parsning, validering, prettyprint og andre "eksterne forhold" h�rer ikke hjemme i klassen her.
 * De skal h�ndteres med {@link GSXMLContext}.
 * <p>
 * Klassen kender ikke Gensafe Pro-begreber.
 */
public class GSDocument implements Cloneable {
    private final static Logger log_ = LoggerFactory.getLogger(GSDocument.class);

    /**
     * Det aktuelle xml-dokument
     */
    protected Document document_;

    /**
     * Instantierer og opretter et nyt {@link Document}
     */
    public GSDocument() {
        setDocument(newDocument());
    }

    /**
     * Instantierer og opretter et nyt {@link Document} med et specifikt Document Element (rod-element)
     *
     * @param pDocumentElementName Navn p� Document Element (rod-element)
     */
    public GSDocument(String pDocumentElementName) {
        this();
        appendElement(document_, pDocumentElementName);
    }

    /**
     * Instantierer med et specifikt {@link Document}.
     *
     * @param pDocument Et well formed {@link Document}. M� ikke v�re null.
     */
    public GSDocument(Document pDocument) {
        setDocument(pDocument);
    }

    /**
     * Instantierer og opretter et nyt {@link Document} med et specifikt Document Element (rod-element)
     *
     * @param pNode Document Element
     */
    public GSDocument(Node pNode) {
        this();
        document_.appendChild(document_.adoptNode(pNode));
    }

    /**
     * S�tter med et specifikt {@link Document}.
     *
     * @param pDocument Et well formed {@link Document}. M� ikke v�re null.
     */
    public void setDocument(Document pDocument) {
        if (pDocument == null) {
            throw new IllegalArgumentException("Document m� ikke v�re null");
        }
        document_ = pDocument;
    }

    /**
     * Afl�ser det aktuelle Document
     *
     * @return Aldrig null
     */
    public Document getDocument() {
        return document_;
    }

    /**
     * Trimmer alle nodes i det aktuelle xml-dokument.
     * S�ledes at text content overalt f�r fjernet foranstillede  blanke og linieskift (" ", "\n" og "\r").
     * Se {@link #trim(Node)}.
     * <p>
     * I praksis kan metoden benyttes til at "oph�ve virkningen af prettyprint" hvis der fx er
     * parset fra en String eller en File med et prettyprintet xml-dokument.
     *
     * @return Den aktuelle instans af {@link GSDocument} selv.
     */
    public GSDocument trim() {
        trim(document_);
        return this;
    }


    /**
     * Fjerner evt. namespace-alias fra alle Node-navne i det aktuelle xml-dokument.
     *
     * @return Den aktuelle instans af {@link GSDocument} selv.
     */
    public GSDocument removeNamespace() {
        removeNamespace(document_.getDocumentElement());
        return this;
    }

    /**
     * Fjerner et evt. namespace-alias fra et Node-navn.
     *
     * @param pTagname Det navn der skal have fjernet et evt. namespace-alias. M� ikke v�re null.
     * @return Aldrig null
     */
    public String removeNamespace(String pTagname) {
        for (int i = 0; i < pTagname.length(); i++) {
            if (pTagname.charAt(i) == ':') {
                return pTagname.substring(i + 1);
            }
        }
        return pTagname.trim();
    }

    /**
     * Fjerner namespace-alias fra alle navne p� elementer og attributter (rekursivt) i det aktuelle xml-dokument.
     *
     * @param pElement Roden hvorfra (og med) der skal �ndres navne.
     * @return Roden selv.
     */
    public Element removeNamespace(Element pElement) {
        document_.renameNode(pElement, null, removeNamespace(pElement.getNodeName()));
        NodeList childNodes = pElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                removeNamespace((Element) childNode);
            }
        }
        NamedNodeMap attributes = pElement.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node item = attributes.item(i);
            pElement.removeAttributeNode((Attr) item);
            pElement.setAttribute(removeNamespace(item.getNodeName()), item.getNodeValue());
        }
        return pElement;
    }

    /**
     * Finder alle elementer ud fra nogle s�gekriterier (rekursivt)
     *
     * @param pElementName S�gekriterium. Angiver navne p� de elementer der skal uds�ges.
     * @param pExactMatch  S�gekriteriem. Hvis true skal der v�re match ogs� p� evt. namespace.
     *                     Hvis false beh�ves der ikke v�re match p� evt. namespace.
     * @return Alle elementer det matcher s�gekriterierne. Aldrig null.
     */
    public List<Element> getElementsByTagName(String pElementName, boolean pExactMatch) {
        return getElementsByTagName(document_.getDocumentElement(), pElementName, pExactMatch);
    }

    /**
     * Finder alle elementer under et givet Element ud fra nogle s�gekriterier (rekursivt)
     *
     * @param pElement     Det element der skal unders�ges.
     * @param pElementName S�gekriterium. Angiver navne p� de elementer der skal uds�ges.
     * @param pExactMatch  S�gekriteriem. Hvis true skal der v�re match ogs� p� evt. namespace.
     *                     Hvis false beh�ves der ikke v�re match p� evt. namespace.
     * @return Alle elementer det matcher s�gekriterierne. Aldrig null.
     */
    public List<Element> getElementsByTagName(Element pElement, String pElementName, boolean pExactMatch) {
        List<Element> elements = new ArrayList<>();
        if (pElementName.indexOf(':') >= 0 || pExactMatch) {
            NodeList nodes = pElement.getElementsByTagName(pElementName);
            for (int i = 0; i < nodes.getLength(); i++) {
                elements.add((Element) nodes.item(i));
            }
        } else {
            NodeList nodes = pElement.getElementsByTagName("*");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                if (element.getNodeName().endsWith(":" + pElementName)) {
                    elements.add(element);
                } else {
                    if (element.getNodeName().equals(pElementName)) {
                        elements.add(element);
                    }
                }
            }
        }
        return elements;
    }


    /**
     * Kloner et det aktuelle xml-dokument. Hvis der optr�der namespaces skal disse v�re erkl�ret.
     *
     * @return Aldrig null
     */
    public GSDocument clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            throw new GSDocumentException(e);
        }
        Document document = (Document) clone(document_.getDocumentElement(), true);
        return new GSDocument(document);
    }

    /**
     * Kloner en Node og dens childs. Hvis der optr�der namespaces skal disse v�re erkl�ret.
     *
     * @param pNode
     * @return En klon (incl. childs). Aldig null
     */
    public Element clone(Node pNode) {
        return (Element) clone(pNode, false);
    }


    /**
     * Gennemstiller til {@link Document#getElementsByTagName}
     *
     * @return
     */
    public NodeList getElementsByTagName(String pTagname) {
        return getDocument().getElementsByTagName(pTagname);
    }

    /**
     * Gennemstiller til {@link Document#getDocumentElement}
     *
     * @return
     */
    public Element getDocumentElement() {
        return getDocument().getDocumentElement();
    }

    /**
     * Gennemstiller til {@link Document}  i form af en facade
     *
     * @param pNode
     * @return Aldrig null
     */
    public List<Node> getChildNodes(Node pNode) {
        List<Node> nodes = new ArrayList<>();
        NodeList childNodes = pNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            nodes.add(childNodes.item(i));
        }
        return nodes;
    }

    /**
     * Gennemstiller til {@link Document}
     *
     * @param pNode
     * @return
     */
    public Node getFirstChild(Node pNode) {
        return pNode.getFirstChild();
    }

    /**
     * Gennemstiller til {@link Document}
     *
     * @param pNode
     * @return
     */
    public Node getLastChild(Node pNode) {
        return pNode.getLastChild();
    }

    /**
     * Gennemstiller til {@link Document}
     *
     * @param pTagName
     * @return
     */
    public Element createElement(String pTagName) {
        return getDocument().createElement(pTagName);
    }

    /**
     * Gennemstiller til {@link Document}
     *
     * @param pName
     * @return
     */
    public Attr createAttribute(String pName) {
        return getDocument().createAttribute(pName);
    }

    /**
     * Danner et nyt element. Det er op til kalderen hvordan det nye Element skal benyttes
     *
     * @param pElementName Navn p� det Element der skal dannes. Kr�vet.
     * @param pAttributes  Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi. Kan undlades.
     * @return Det element der er blevet dannet
     */
    public Element newElement(String pElementName, String... pAttributes) {
        Element element = document_.createElement(pElementName);
        setAttributes(element, pAttributes);
        return element;
    }

    /**
     * Danner et nyt element med et specfifikt tekstindhold. Det er op til kalderen hvordan det nye Element skal benyttes
     *
     * @param pElementName Navn p� det Element der skal dannes. Kr�vet.
     * @param pText        Det tekstindhold der skal v�re under elementet. Kr�vet.
     * @param pAttributes  Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi. Kan undlades.
     * @return Det element der er blevet dannet
     */
    public Element newTextElement(String pElementName, String pText, String... pAttributes) {
        Element element = newElement(pElementName, pAttributes);
        element.setTextContent(pText);
        return element;
    }

    /**
     * Tilf�jer en tekst under en Node
     *
     * @param pParentNode Den Node hvorunder teksten skal inds�ttes
     * @param pValue      Tekst
     * @return Den Node hvorunder teksten er blevet indsat
     */
    public Text appendText(Node pParentNode, String pValue) {
        Text text = document_.createTextNode(pValue);
        pParentNode.appendChild(text);
        return text;
    }

    /**
     * S�tter et antal attributter p� et Element
     *
     * @param pElement    Det element der skal have p�sat attributter
     * @param pAttributes Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi.
     *                    Kan i princippet undlades - i givet fald vil metoden s� blot ikke have nogen virkning
     * @return Det element der har f�rt sat attributter
     */
    public Element setAttributes(Element pElement, String... pAttributes) {
        if (pAttributes.length % 2 != 0) {
            throw new IllegalArgumentException("Metode er kaldt med et ugyldigt antal argumenter");
        }
        int i = 0;
        while (i < pAttributes.length) {
            String attributeName = pAttributes[i++];
            String attributeValue = pAttributes[i++];
            pElement.setAttribute(attributeName, attributeValue);
        }
        return pElement;
    }

    /**
     * Tilf�jer en kommentar under en Node
     *
     * @param pParentNode Den Node hvorunder kommentaren skal inds�ttes
     * @param pValue      Kommentar
     * @return Den Node hvorunder kommentaren er blevet indsat
     */
    public Comment appendComment(Node pParentNode, String pValue) {
        Comment comment = document_.createComment(pValue);
        pParentNode.appendChild(comment);
        return comment;
    }

    /**
     * Flytter en Node fra et andet Document.
     * <p>.
     * Hvis noden i det afgivende dokument skal bevares, s� uds�t den for {@link org.w3c.dom.Document#cloneNode(boolean)} inden metoden kaldes
     *
     * @param pParentNode Den Node hvorunder en Node fra et andet Document skal inds�ttes
     * @param pNode       Node fra et andet Document. Efter kaldet vil noden v�re tom
     * @return Den Node der er blevet dannet
     */
    public Node appendAdoptedChild(Node pParentNode, Node pNode) {
        Node childNode = document_.adoptNode(pNode);
        pParentNode.appendChild(childNode);
        return childNode;
    }

    /**
     * Importerer en Node fra et andet Document.
     *
     * @param pParentNode Den Node hvorunder en Node fra et andet Document skal inds�ttes
     * @param pNode       Node fra et andet Document. Efter kaldet vil noden v�r u�ndret
     * @return Den Node der er blevet dannet
     */
    public Node importNode(Node pParentNode, Node pNode) {
        Node childNode = document_.adoptNode(pNode.cloneNode(true));
        pParentNode.appendChild(childNode);
        return childNode;
    }

    /**
     * Tilf�jer et nyt element
     *
     * @param pParentNode  Den Node hvorunder elementet skal tilf�jes. Kr�vet.
     * @param pElementName Navn p� det Element der skal inds�ttes. Kr�vet.
     * @param pAttributes  Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi. Kan undlades.
     * @return Det element der er blevet tilf�jet
     */
    public Element appendElement(Node pParentNode, String pElementName, String... pAttributes) {
        Element element = newElement(pElementName, pAttributes);
        return (Element) pParentNode.appendChild(element);
    }

    public Element appendElement(Node pParentNode, Comment comment, String pElementName, String... pAttributes) {
        Element element = newElement(pElementName, pAttributes);
        element.appendChild(document_.importNode(comment, false));
        return (Element) pParentNode.appendChild(element);
    }

    /**
     * Tilf�jer rod-elementet
     *
     * @param pGsDocument  M� ikke have er rod-element. Kr�vet.
     * @param pElementName Navn p� det Element der skal inds�ttes. Kr�vet.
     * @param pAttributes  Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi. Kan undlades.
     * @return Det element der er blevet tilf�jet
     */
    public Element appendElement(GSDocument pGsDocument, String pElementName, String... pAttributes) {
        Element element = newElement(pElementName, pAttributes);
        return (Element) pGsDocument.getDocument().appendChild(element);
    }

    /**
     * Gennemstiller til {@link Node}
     *
     * @param pParentNode
     * @param pChildNode
     * @return
     */
    public Node appendChild(Node pParentNode, Node pChildNode) {
        pParentNode.appendChild(pChildNode);
        return pChildNode;
    }

    /**
     * Gennemstiller til {@link Node}
     *
     * @param pGsDocument
     * @param pChildNode
     * @return
     */
    public Node appendChild(GSDocument pGsDocument, Node pChildNode) {
        pGsDocument.getDocument().appendChild(pChildNode);
        return pChildNode;
    }

    /**
     * Tilf�jer et nyt element med et specfifikt tekstindhold
     *
     * @param pParentNode  Den Node hvorunder elementet skal tilf�jes. Kr�vet.
     * @param pElementName Navn p� det Element der skal inds�ttes. Kr�vet.
     * @param pText        Det tekstindhold der skal v�re under elementet. Kr�vet.
     * @param pAttributes  Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi. Kan undlades.
     * @return Det element der er blevet tilf�jet
     */
    public Element appendTextElement(Node pParentNode, String pElementName, String pText, String... pAttributes) {
        Element element = newTextElement(pElementName, pText, pAttributes);
        return (Element) pParentNode.appendChild(element);
    }

    public Element appendTextElement(Node pParentNode, Comment comment, String pElementName, String pText, String... pAttributes) {
        Element element = newTextElement(pElementName, pText, pAttributes);
        element.appendChild(document_.importNode(comment, false));
        return (Element) pParentNode.appendChild(element);
    }

    /**
     * Tilf�jer et nyt element med et specfifikt tekstindhold. Elementet tilf�jes til �verste Element i dokumentet (rodelementet)
     * <p>
     * Se {@link #appendTextElement(Node, String, String, String...)}
     *
     * @param pElementName
     * @param pText
     * @param pAttributes
     * @return
     */
    public Element appendTextElement(String pElementName, String pText, String... pAttributes) {
        return appendTextElement(document_.getDocumentElement(), pElementName, pText, pAttributes);
    }

    /**
     * Tilf�jer et nyt element med et specfifikt tekstindhold som "det f�rste" element
     *
     * @param pParentNode  Den Node hvorunder elementet skal tilf�jes som den f�rste. Kr�vet.
     * @param pElementName Navn p� det Element der skal inds�ttes. Kr�vet.
     * @param pText        Det tekstindhold der skal v�re under elementet. Kr�vet.
     * @param pAttributes  Eventuelle atributter i form af et antal (2,4,6,...) sammenh�rende v�rdier af navn og v�rdi. Kan undlades.
     * @return Det element der er blevet tilf�jet
     */
    public Element insertTextElement(Node pParentNode, String pElementName, String pText, String... pAttributes) {
        Element element = newTextElement(pElementName, pText, pAttributes);
        return (Element) pParentNode.insertBefore(element, pParentNode.getFirstChild());
    }

    /**
     * Danner et nyt {@link Document}
     * <p>
     * Det er en hj�lpemetode for {@link GSDocument} selv.
     * Den b�r overskrives hvis det er n�dvendigt med en anden funktionalitet til at danne et nyt {@link Document}.
     *
     * @return
     */
    protected Document newDocument() {
        DocumentBuilder documentBuilder = getDocumentBuilder();
        Document document = documentBuilder.newDocument();
        return document;
    }

    /**
     * Tilf�jer et antal attributter til et element.
     * Hvis en attribut findes i forvejen vil den blive overskrevet
     *
     * @param pElement
     * @param pAttributes M� ikke v�re null
     */
    public void addAttributes(Element pElement, NamedNodeMap pAttributes) {
        for (int i = 0; i < pAttributes.getLength(); i++) {
            Node attributeNode = pAttributes.item(i);
            pElement.setAttribute(attributeNode.getNodeName(), attributeNode.getNodeValue());
        }
    }

    /**
     * Kopierer alle attributter fra et element til et andet Element.
     * Hvis en attribut findes i forvejen vil den blive overskrevet
     *
     * @param pTargetElement
     * @param pSourceElement M� ikke v�re null
     */
    public void copyAttributes(Element pTargetElement, Element pSourceElement) {
        addAttributes(pTargetElement, pSourceElement.getAttributes());
    }

    /**
     * Kloner en Node og dens childs
     * <p>
     * Det er en hj�lpemetode for {@link GSDocument} selv.
     * Den b�r overskrives hvis det er n�dvendigt med en anden funktionalitet til at klone.
     *
     * @param pNode
     * @param pAsDocument True hvis resuktat skal leveres i et Document, false hvis resultatet skal leveres i et Element
     * @return En klon (incl. childs)
     */
    public Node clone(Node pNode, boolean pAsDocument) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(pNode);
            DOMResult result = new DOMResult();
            transformer.transform(source, result);
            Document document = (Document) result.getNode();
            if (pAsDocument) {
                return document;
            }
            return document.getDocumentElement();
        } catch (TransformerException e) {
            throw new GSDocumentException(e);
        }
    }

    /**
     * Trimmer alle child nodes til en given node (rekursivt)
     * <p>
     * I praksis kan metoden benyttes til at "oph�ve virkningen af prettyprint" hvis den angivne Node er
     * parset fra en String eller en File med et prettyprintet xml-dokument.
     * <p>
     * Metoden virker s�dan her:
     * <p>
     * Hvis en node b�de indeholder child Element nodes og child Text nodes
     * vil tekstindholdet i alle child Text nodes blive �ndret s�ledes: Foranstillede " ", "\n" og "\r"
     * bliver fjernet.
     * <p>
     * Alts�:
     * 1) Kun Text nodes kan blive �ndret,
     * 2) Kun blanktegn og linieskift kan blive fjernet, og
     * 3) Kun hvis de er foranstillede
     * <p>
     * Det er en hj�lpemetode for {@link GSDocument} selv.
     * Den b�r overskrives hvis det er n�dvendigt med en anden funktionalitet til at trimme et {@link Document}.
     *
     * @param pNode Den node der skal trimmes. Det kan typisk v�re et Document
     */
    protected Node trim(Node pNode) {
        NodeList childNodes = pNode.getChildNodes();
        boolean nodeContainsElement = false;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode instanceof Element) {
                nodeContainsElement = true;
                break;
            }
        }
        int i = 0;
        while (i < childNodes.getLength()) {
            Node childNode = childNodes.item(i);
            if (nodeContainsElement) {
                if (childNode instanceof Text) {
                    String nodeValue = trim(childNode.getNodeValue());
                    childNode.setTextContent(nodeValue);
                    if (childNode.getTextContent().length() == 0) {
                        pNode.removeChild(childNode);
                        i--;
                    }
                }
            }
            trim(childNode);
            i++;
        }

        return pNode;
    }

    /**
     * Returnerer den {@link javax.xml.parsers.DocumentBuilder} der som default skal benyttes n�r der parses.
     * <p>
     * Det er en hj�lpemetode for {@link GSDocument} selv.
     * Den b�r overskrives hvis det er n�dvendigt med en anden anden {@link javax.xml.parsers.DocumentBuilder}.
     *
     * @return Aldrig null
     */
    protected DocumentBuilder getDocumentBuilder() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw new GSDocumentException(e);
        }
    }

    /**
     * Fjerner foranstillede " ", "\n" og "\r".
     * <p>
     * Det er en hj�lpemetode for {@link GSDocument} selv.
     * Den b�r overskrives hvis det er n�dvendigt med en anden anden virkem�de for at trimme en String.
     *
     * @param pString M� ikke v�re null.
     * @return Aldrig null
     */
    protected String trim(String pString) {
        StringBuilder sb = new StringBuilder();
        boolean beforeText = true;
        for (int i = 0; i < pString.length(); i++) {
            char c = pString.charAt(i);
            if (beforeText) {
                switch (c) {
                    case ' ':
                    case '\n':
                    case '\r':
                        break;
                    default:
                        beforeText = false;
                        sb.append(c);
                        break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Finder og returnerer f�rste og bedste childelement med navn = pChildelementname til elementet pElement.<br>
     * For returv�rdier se {@link XPathConstants}.<br>
     * Se i �vrigt {@link XPathFactory#newXPath()} og {@link XPath#evaluate(String, Object, QName)}
     *
     * @param pXPath
     * @param pNode       Udgangspunktet for fremfindingen. Husk brug af "." i xpath-udtryk
     * @param pReturntype
     * @return
     */
    private Object evaluateXPath(String pXPath, Node pNode, QName pReturntype) {
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            return xPath.evaluate(pXPath, pNode, pReturntype);
        } catch (XPathExpressionException e) {
            throw new GSDocumentException(e);
        }
    }

    /**
     * Evaluerer et XPath-udtryk.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pXPath
     * @return Elements v�rdi - {@link String} (aldrig null)
     */
    public String evaluateXPathToString(String pXPath) {
        return evaluateXPathToString(pXPath, document_);
    }

    /**
     * Evaluerer et XPath-udtryk.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pXPath
     * @param pNode  Udgangspunkt. Husk brug af "." i xpath-udtryk
     * @return Elements v�rdi - {@link String} (aldrig null)
     */
    public String evaluateXPathToString(String pXPath, Node pNode) {
        return (String) evaluateXPath(pXPath, pNode, XPathConstants.STRING);
    }

    /**
     * Evaluerer et XPath-udtryk.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pXPath
     * @return NodeList (aldrig null)
     */
    public NodeList evaluateXPathToNodeList(String pXPath) {
        return (NodeList) evaluateXPath(pXPath, document_, XPathConstants.NODESET);
    }

    /**
     * Evaluerer et XPath-udtryk og leverer resultatet i et XML-dokument.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pDocumentElementName
     * @param pXPath
     * @return Et XML-dokument med resultatet af evalueringen. Aldrig null
     */
    public GSDocument evaluateXPathToDocument(String pDocumentElementName, String pXPath) {
        GSDocument xpathDocument = new GSDocument(pDocumentElementName);
        NodeList nodeList = evaluateXPathToNodeList(pXPath);
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            xpathDocument.importNode(xpathDocument.getDocumentElement(), node);
        }
        return xpathDocument;
    }

    /**
     * Evaluerer et XPath-udtryk.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pXPath
     * @return Liste af elementer (aldrig null)
     */
    public List<Element> evaluateXPathToList(String pXPath) {
        List<Element> elements = new ArrayList<>();
        NodeList nodeList = (NodeList) evaluateXPath(pXPath, document_, XPathConstants.NODESET);
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                elements.add((Element) nodeList.item(i));
            }
        }
        return elements;
    }

    /**
     * Evaluerer et XPath-udtryk.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pXPath
     * @return Element (aldrig null)
     */
    public Element evaluateXPathToElement(String pXPath) {
        return evaluateXPathToElement(pXPath, document_);
    }

    /**
     * Evaluerer et XPath-udtryk.
     * Se {@link #evaluateXPath(String, Node, QName)}
     *
     * @param pXPath
     * @param pNode  Udgangspunkt. Husk brug af "." i xpath-udtryk
     * @return Element (aldrig null)
     */
    public Element evaluateXPathToElement(String pXPath, Node pNode) {
        Node node = (Node) evaluateXPath(pXPath, pNode, XPathConstants.NODE);
        return (Element) node;
    }

    /**
     * Omd�ber en "node".<br>Se {@link Document#renameNode(Node, String, String)}
     */
    public Node renameNode(Node pNode, String pNamespaceURI, String pQualifiedName) {
        return document_.renameNode(pNode, pNamespaceURI, pQualifiedName);
    }

    /**
     * Omd�ber en "node" til v�rdien af pQualifiedName, med dokumentets namespace.<br>Se {@link GSDocument#renameNode(Node, String, String)}
     */
    public Node renameNode(Node pNode, String pQualifiedName) {
        return renameNode(pNode, document_.getNamespaceURI(), pQualifiedName);
    }

    /**
     * Omd�ber "root node" til v�rdien af pQualifiedName.<br>Se {@link GSDocument#renameNode(Node, String, String)}
     */
    public Node renameRootNode(String pNamespaceURI, String pQualifiedName) {
        return renameNode(document_.getDocumentElement(), pNamespaceURI, pQualifiedName);
    }

    /**
     * Omd�ber "root node" til v�rdien af pQualifiedName, med dokumentets namespace.<br>Se {@link GSDocument#renameNode(Node, String, String)}
     */
    public Node renameRootNode(String pQualifiedName) {
        return renameNode(document_.getDocumentElement(), document_.getNamespaceURI(), pQualifiedName);
    }

    public List<Node> getNodeList(Supplier<NodeList> nodelistSupplier, Class... classes) {
        List<Node> nodes = new ArrayList<>();
        NodeList nodeList = nodelistSupplier.get();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            boolean isClass = classes.length == 0 ? true : false;
            for (int j = 0; j < classes.length; j++) {
                Class cls = classes[j];
                if (node.getClass().getName().equals(cls.getName())) {
                    isClass = true;
                }
            }
            if (isClass) {
                nodes.add(node);

            }
        }
        return nodes;
    }
}

