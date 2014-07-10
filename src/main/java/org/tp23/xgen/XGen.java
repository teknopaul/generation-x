/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.tp23.xgen.path.XGenPath;
import org.tp23.xgen.path.XGenPathStep;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Contains the core xGen functionality for generating XML.
 * 
 * Also contains convenience methods for evaluating XPaths and Serializing the XML.
 * 
 * This class is not thread safe there should be one instance per Document to be created.
 * 
 * @author teknopaul
 *
 */
public class XGen {

	private String outputEncoding = "UTF-8";
	private String outputIndent = "yes";
	private String outputMethod = "xml";
	
	private final Document document;
	
	// Constructors
	
	/**
	 * Create an XGen instance and create a Document using default JVM XML factories.
	 */
	public XGen() {
		this.document = FactoryUtils.newDocument();
	}
	
	public XGen(DocumentBuilder db) {
		this.document = db.newDocument();
	}
	
	public XGen(Document document) {
		this.document = document;
	}
	
	public XGen(Element elem) {
		this.document = elem.getOwnerDocument();
	}
	
	// New document using xGen path statement

	/**
	 * Create a new document populated with elements form the xGenpath.
	 * The path must be absolute, i.e. it must start with a / and it must have a single root
	 * element, this is a requirement of XML. The root may have attributes.
	 * This method should be called only once per lifetime of the XGen instance.
	 * 
	 * @param xGenPath 
	 * @return org.w3c.dom.Document
	 * @throws XGenExpressionException
	 */
	public XGenNodeList newDocument(String xGenPath, int... arrayLengths) throws XGenExpressionException {
		if (getRoot() != null) {
			throw new IllegalStateException("Document already exists");
		}
		if ( ! xGenPath.startsWith("/")) {
			throw new XGenExpressionException("New document needs an absolute path to start");
		}
		XGenPath parsedGenPath = new XGenPath(xGenPath.substring(1), dotIsClass(), arrayLengths);
		if ( parsedGenPath.getStep().getArrayLength() != 1 ) {
			throw new XGenExpressionException("Must be only one root element");
		}
		Element root = document.createElement(parsedGenPath.getStep().getElement());
		Map<String, String> atts = parsedGenPath.getStep().getAttributes();
		if (atts != null) {
			setAttributes(root, atts);
		}
		document.appendChild(root);
		
		XGenNodeList tailNodes = new XGenNodeList(this);
		XGenNodeList context = XGenNodeList.createSingleNodeList(this, root);
		create(context, parsedGenPath.next(), tailNodes);
		if (tailNodes.getLength() == 0) {
			tailNodes.add(root);
		}
		return tailNodes;
	}
	
	/**
	 * @return The root element of the document being created.
	 */
	public Element getRoot() {
		return this.document.getDocumentElement();
	}
	
	// Core XMLcreation methods
	
	/**
	 * Create XML content, and insert it as a child of the content element.
	 * 
	 * @param elem where to insert the new elements
	 * @param xGenPath a xGen path string, or a whole parseable XML doc as a string.
	 * @return The tail nodes, i.e. a list of all leaf nodes created
	 * @throws XGenExpressionException
	 */
	public XGenNodeList create(Element elem, String xGenPath, int... arrayLengths) throws XGenExpressionException {
		XGenNodeList tailNodes = new XGenNodeList(this);
		XGenNodeList context = XGenNodeList.createSingleNodeList(this, elem);
		create(context, xGenPath, tailNodes, arrayLengths);
		return tailNodes;
	}
	
	/**
	 * Create XML content, and append to all nodes in the list.
	 */
	public XGenNodeList create(XGenNodeList context, String xGenPath, int... arrayLengths) throws XGenExpressionException {
		XGenNodeList tailNodes = new XGenNodeList(this);
		create(context, xGenPath, tailNodes, arrayLengths);
		return tailNodes;
	}
	
	/**
	 * Create XML content, for all nodes in the list.
	 */
	public XGenNodeList create(NodeList context, String xGenPath, int... arrayLengths) throws XGenExpressionException {
		XGenNodeList tailNodes = new XGenNodeList(this);
		XGenNodeList xContext = new XGenNodeList(this);
		xContext.addElements(context);
		create(xContext, xGenPath, tailNodes, arrayLengths);
		return tailNodes;
	}
	
	/**
	 * Create XML content, and insert it as a child of the root element.
	 * 
	 * @param xGenPath a xGen path string, or a whole parseable XML doc as a string.
	 * @return The tail nodes, i.e. a list of all leaf nodes created
	 * @throws XGenExpressionException
	 */
	public XGenNodeList create(String xGenPath, int... arrayLengths) throws XGenExpressionException {
		XGenNodeList tailNodes = new XGenNodeList(this);
		XGenNodeList context = XGenNodeList.createSingleNodeList(this, getRoot());
		create(context, xGenPath, tailNodes, arrayLengths);
		return tailNodes;
	}
	
	// Convenience methods for XPath
	
	/**
	 * Select a single element using an XPath string with the docuument root as the base.
	 * @param xPath
	 * @return org.w3c.Element
	 * @throws XPathExpressionException
	 */
	public Element selectFirst(String xPath) throws XPathExpressionException {
		return (Element)FactoryUtils.newXPath().evaluate(xPath, getRoot(), XPathConstants.NODE);
	}
	
	/**
	 * Select a list of element using an XPath string with the docuument root as the base.
	 * @param xPath
	 * @return XGenNodeList a Mutable NodeList which can be used as a context to create more elements.
	 * @throws XPathExpressionException
	 */
	public XGenNodeList select(String xPath) throws XPathExpressionException {
		NodeList nodeList = (NodeList)FactoryUtils.newXPath().evaluate(xPath, getRoot(), XPathConstants.NODESET);
		return new XGenNodeList(this, nodeList);
	}
	
	/**
	 * Select a single element using an XPath string with the supplied element as the base.
	 * @param context the element from where to start the XPath search
	 * @param xPath
	 * @return org.w3c.Element
	 * @throws XPathExpressionException
	 */
	public Element selectFirst(Element context, String xPath) throws XPathExpressionException {
		return (Element)FactoryUtils.newXPath().evaluate(xPath, context, XPathConstants.NODE);
	}
	
	/**
	 * Select a list of element using an XPath string with the supplied element as the base.
	 * @param context the element from where to start the XPath search
	 * @param xPath
	 * @return XGenNodeList a Mutable NodeList which can be used as a context to create more elements.
	 * @throws XPathExpressionException
	 */
	public XGenNodeList select(Element context, String xPath) throws XPathExpressionException {
		NodeList nodeList = (NodeList)FactoryUtils.newXPath().evaluate(xPath, context, XPathConstants.NODESET);
		return new XGenNodeList(this, nodeList);
	}

	/**
	 * Serializes the while XML document to a string.
	 */
	public String toString() {
		try {
			return serialize();
		}
		catch (TransformerException ex) {
			return "Invalid XML generation";
		} 
	}
	
	
	// Convenience methods for Serialization
	/**
	 * Set encoding used for output, usually UTF-8.
	 * @see javax.xml.transform.OutputKeys#ENCODING
	 */
	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	/**
	 * Add optional whitespace, newlines and indentation to the XML output.
	 * Defaults to "yes"
	 * @see javax.xml.transform.OutputKeys#INDENT
	 */
	public void setOutputIndent(String outputIndent) {
		this.outputIndent = outputIndent;
	}

	/**
	 * Output xml or html., this also affects how period are treated in element names.
	 * When HTML periods are treated as HTML class attributes.
	 * @see javax.xml.transform.OutputKeys#METHOD
	 */
	public void setOutputMethod(String outputMethod) {
		this.outputMethod = outputMethod;
	}
	
	/**
	 * @return An XML string
	 */
	public String serialize() throws TransformerException {
		StringWriter out = new StringWriter();
		serialize(out);
		return out.getBuffer().toString();
	}
	
	public void serialize(OutputStream out) throws TransformerException {
		StreamResult streamResult = new StreamResult(out);
		serialize(streamResult);
	}
	
	public void serialize(Writer out) throws TransformerException {
		StreamResult streamResult = new StreamResult(out);
		serialize(streamResult);
	}
	
	public void serialize(StreamResult streamResult) throws TransformerException {
		if (document == null) {
			throw new IllegalStateException("Document not created yet");
		}
		DOMSource domSource = new DOMSource(document);
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", "2");
		Transformer serializer = factory.newTransformer();
		if (serializer.getClass().getName().contains("org.apache")) {
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		}
		serializer.setOutputProperty(OutputKeys.ENCODING, this.outputEncoding);
		serializer.setOutputProperty(OutputKeys.INDENT, this.outputIndent);
		serializer.setOutputProperty(OutputKeys.METHOD, this.outputMethod);
		serializer.transform(domSource, streamResult);
	}
	
	// Private creation methods
	
	/**
	 * This method should be the only method that calls Element generation if
	 * parsing XML is also required. 
	 */
	private void create(XGenNodeList context, String xGenPath, XGenNodeList tailNodes, int... arrayLengths) throws XGenExpressionException {
		// hackety ho hum
		if (xGenPath.startsWith("<")) {
			insert(context, xGenPath);
		} else {
			create(context, new XGenPath(xGenPath, dotIsClass(), arrayLengths), tailNodes);
		}
	}
	
	/**
	 * Recursively add Elements to the document.
	 * 
	 * @param context  The node to which more Elements are being added
	 * @param xGenPath  The string expressions
	 * @param tailNodes  A list of nodes returned to the client code
	 * @throws XGenExpressionException
	 */
	private void create(XGenNodeList context, XGenPath xGenPath, XGenNodeList tailNodes) throws XGenExpressionException {

		XGenPathStep step = xGenPath.getStep();
		if (step == null) return;
		
		for(Node nextNode : context) {
			if (nextNode instanceof Element) {
				XGenNodeList generatedElements = createPathStep((Element)nextNode, step);
				create(generatedElements, xGenPath.next(), tailNodes);
				if (xGenPath.isTail()) {
					tailNodes.addElements(generatedElements);
				}
			}
		}
		
	}
	
	/**
	 * Recursively add a whole block of XML to the nodes.
	 */
	private void insert(XGenNodeList context, String xml) throws XGenExpressionException {
		try {
			for(Node nextNode : context) {
				Node clone = FactoryUtils.parse(xml).getDocumentElement();
				document.adoptNode(clone);
				nextNode.appendChild(clone);
			}
		} catch (DOMException e) {
			throw new XGenExpressionException("xGen path treated as XML, but was not parseable", e);
		} catch (SAXException e) {
			throw new XGenExpressionException("xGen path treated as XML, but was not parseable", e);
		} catch (IOException e) {
			throw new XGenExpressionException("xGen path treated as XML, but was not parseable", e);

		}
	}
	/**
	 * Create one step in an xGen Path e.g. if path is /html/head/body{id=index}/div[3]
	 * html, head and body are all steps
	 * @param context parent node to the element being created
	 * @param step  step syntax is element or element{att:val} or element[n]
	 */
	private XGenNodeList createPathStep(Element context, XGenPathStep step) {
		XGenNodeList generatedNodes = new XGenNodeList(this);
		for (int i = 0; i < step.getArrayLength(); i++) {
			Element element = document.createElement(step.getElement());
			context.appendChild(element);
			if (step.getAttributes() != null) {
				Iterator<String> iter = step.getAttributes().keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					element.setAttribute(key, step.getAttributes().get(key));
				}
			}
			generatedNodes.add(element);
		}
		return generatedNodes;
	}
	
	private void setAttributes(Element elem, Map<String, String> atts) {
		for (String key : atts.keySet()) {
			elem.setAttribute(key, atts.get(key));
		}
	}
	
	private boolean dotIsClass() {
		return "html".equals(outputMethod);
	}
}
