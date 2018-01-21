/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Some utility methods for creating default XML documents from the JVM.
 */
public class FactoryUtils {

	private static DocumentBuilderFactory docBuilderFactory;
	private static DocumentBuilder dBuilder;
	private static XPathFactory xPathFactory;
	
	static {
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		xPathFactory = XPathFactory.newInstance();
		try {
			dBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Return a new  empty XML document, using the default JVM XML API
	 * usually this is Xerces.
	 */
	public static Document newDocument() {
		return dBuilder.newDocument();
	}

	/**
	 * Create an XPath instance using JVM defaults.
	 */
	public static XPath newXPath() {
		return xPathFactory.newXPath();
	}

	// XML Parser methods

	public static Document parse(File f) throws SAXException, IOException {
		return dBuilder.parse(f);
	}
	
	public static Document parse(InputSource is) throws SAXException, IOException {
		return dBuilder.parse(is);
	}
	
	public static Document parse(InputStream is) throws SAXException, IOException {
		return dBuilder.parse(is);
	}
	
	/**
	 * Parse an XML string, NOT that parse(uri) method from the Java APIs.
	 * @param xml A XML String
	 */
	public static Document parse(String xml) throws SAXException, IOException {
		return dBuilder.parse(new InputSource(new StringReader(xml)));
	}

	/**
	 * Parses XML from over the network.
	 */
	public static Document parse(URL uri) throws SAXException, IOException {
		return dBuilder.parse(uri.toExternalForm());
	}
	
	public static Document parse(InputStream is, String systemId) throws SAXException, IOException {
		return dBuilder.parse(is, systemId);
	}

}
