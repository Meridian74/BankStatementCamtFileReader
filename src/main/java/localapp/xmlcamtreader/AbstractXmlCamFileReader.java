package localapp.xmlcamtreader;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public abstract class AbstractXmlCamFileReader {
	
	private Document document = null;
	private XPath xpath = null;
	private NodeList statements = null;
	
	
	protected AbstractXmlCamFileReader(File filename) {
		init(filename);
	}
	
	
	public void init(File filename) {
		// CAMT:XML fájl beolvasása - 'autoclose'-t használva
		try (FileInputStream inputStream = new FileInputStream(filename)) {
			// Document-object létrehozása a memóriába az XML file olvasásához
			this.document = createDocument(inputStream);
			// XPath-object létrehozása, amely felismeri camt:xml formátumot, ezzel tudunk navigálni az elemek közt
			this.xpath = createXPathObject();
			// Kigyűjtjük a Statement ('camt:Stmt') node elemeket a '*.CAM' file-ból
			String pathFromDocumentRoot = "//camt:Document/camt:BkToCstmrStmt/camt:Stmt";
			this.statements = (NodeList) (
					(xpath.compile(pathFromDocumentRoot)).evaluate(document, XPathConstants.NODESET)
			);
			
		} catch (IOException | ParserConfigurationException | SAXException | XPathExpressionException exp) {
			throw new RuntimeException("Hiba történt a .CAM file '" + filename.getName() + " megnyitása közben: " + exp.getMessage());
		}
	}
	
	private static Document createDocument(FileInputStream inputStream) throws
			ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		// Ezek a tiltások biztonsági okokból (IDE, SonarLint security warningolt a DocumentBuilderFactory.newInstance() miatt)
		factory.setExpandEntityReferences(false);
		// Tiltsa le az inline DTD-k betöltését
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		// Tiltsa le a külső entitások betöltését
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		
		// Névtér érzékeny beolvasást állítja be
		factory.setNamespaceAware(true);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		// Tiltsa le a DTD betöltését
		builder.setEntityResolver(new EntityResolver() {
			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				return new InputSource(); // Ne töltsön be semmit
			}
		});
		
		return builder.parse(inputStream);
	}
	
	private static XPath createXPathObject() {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		
		// Névtér beállítása az XPath keresésekhez
		xpath.setNamespaceContext(new NamespaceContext() {
			@Override
			public String getNamespaceURI(String prefix) {
				if ("camt".equals(prefix)) {
					return "urn:iso:std:iso:20022:tech:xsd:camt.053.001.02";
				} else if ("xsi".equals(prefix)) {
					return XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
				}
				return null;
			}
			
			@Override
			public String getPrefix(String uri) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Iterator<String> getPrefixes(String uri) {
				throw new UnsupportedOperationException();
			}
		});
		
		return xpath;
	}
	
	public Document getDocument() {
		return document;
	}
	
	public XPath getXpath() {
		return xpath;
	}
	
	public NodeList getStatements() {
		return statements;
	}
	
}