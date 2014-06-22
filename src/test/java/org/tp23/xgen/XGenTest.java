/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;

/**
 * This test serves as example code..
 * 
 * @author teknopaul
 *
 */
public class XGenTest {

	/**
	 * Create and output an HTML format document, HTML is used in the examples since
	 * it expected that users are familiar with the schema.
	 */
	@Test
	public void testMinialHtmlGeneration() throws XGenExpressionException, XPathExpressionException, TransformerException {

		XGen xGen = XGenFactory.newInstance(FactoryUtils.newDocument());
		xGen.newDocument("/html/head/title");
		xGen.select("//title").setTextContent("Documentation");
		xGen.select("//html").create("body/header").setTextContent("Generation X");
		xGen.select("//body").create("article").setTextContent("Welcome to Generation X, fast programatic XML generation without templates");
		xGen.select("//body").create("navigation/ul/li[5]").setTextContent("Menu Item");
		xGen.select("//body").create("footer").setTextContent("Copyleft teknopaul");
		xGen.serialize(System.out);

		// Assert we have one and only one
		Assert.assertEquals(1, xGen.select("//title").getLength());
		Assert.assertEquals(1, xGen.select("//body").getLength());
		Assert.assertEquals(1, xGen.select("//header").getLength());
		Assert.assertEquals(1, xGen.select("//footer").getLength());
		// and five of these
		Assert.assertEquals(5, xGen.select("//li").getLength());
	}
	
	/**
	 * This test shows creating multiple elements with [] syntax,
	 * and attributes with {name=value} syntax.
	 */
	@Test
	public void testArraySyntax() throws XGenExpressionException, XPathExpressionException, TransformerException {

		XGen xGen = XGenFactory.newInstance();
		xGen.newDocument("/html/head/title");
		
		XGenNodeList headings = xGen.select("//html").create("body/div{id=container}/table{class=table}/thead/tr/th[4]");
		headings.setTextContent(new String[]{
			"id", "date", "foo", "baa"
		});
		
		XGenNodeList table = xGen.select("//table");
		table.create("tbody/tr[2]/td[4]");
		String data[][] = {
				{"one","two", "three", "four"},
				{"a","b", "c", "d"},
		};
		
		table.select("tbody/tr[1]/td").setTextContent(data[0]).setAttribute("class", "odd");
		table.select("tbody/tr[2]/td").setTextContent(data[1]).setAttribute("class", "even");
		
		xGen.serialize(System.out);

		Assert.assertEquals(1, xGen.select("//title").getLength());
		Assert.assertEquals(1, xGen.select("//body").getLength());
		Assert.assertEquals(1, xGen.select("//table").getLength());
		Assert.assertEquals(3, xGen.select("//tr").getLength());
		Assert.assertEquals(4, xGen.select("//th").getLength());
		Assert.assertEquals(8, xGen.select("//td").getLength());

	}

	/**
	 * NodeLists returned by create() and select() can have setTextContent() called and this will add text
	 * to ALL the nodes in the list. 
	 */
	@Test
	public void testSetTextContext() throws XGenExpressionException, XPathExpressionException, TransformerException {

		XGen xGen = XGenFactory.newInstance(FactoryUtils.newDocument());
		xGen.newDocument("/html/head/title");
		xGen.select("//title").setTextContent("Documentation");
		xGen.select("//html").create("body/header").setTextContent("My doc");
		xGen.select("//body").create("article").setTextContent("Welcome to Generation X, write less code, make more XML.");
		xGen.select("//body").create("navigation/ul/li[5]").setTextContent("Menu Item");
		xGen.select("//body").create("footer").setTextContent("Copyleft teknopaul");
		xGen.serialize(System.out);

		Assert.assertEquals(1, xGen.select("//html").getLength());
		Assert.assertEquals(1, xGen.select("//title").getLength());
		Assert.assertEquals(1, xGen.select("//body").getLength());
		Assert.assertEquals(1, xGen.select("//article").getLength());
		Assert.assertEquals(5, xGen.select("//li").getLength());
		Assert.assertEquals(1, xGen.select("//footer").getLength());
		Assert.assertEquals("Copyleft teknopaul", xGen.select("//footer").item(0).getTextContent());

	}
	
	/**
	 * The # symbol can be used as a shorthand for {id=whatever}
	 */
	@Test
	public void testSyntacticSugar() throws XGenExpressionException, XPathExpressionException, TransformerException {
		XGen xGen = XGenFactory.newInstance(FactoryUtils.newDocument());
		xGen.newDocument("/html/head/title");
		xGen.select("//title").setTextContent("Example");
		xGen.select("//html").create("body#home/article");
		xGen.serialize(System.out);

		Assert.assertEquals("home", xGen.select("//body").item(0).getAttributes().getNamedItem("id").getNodeValue());
		Assert.assertNull(xGen.select("//article").item(0).getAttributes().getNamedItem("id"));

	}

	/**
	 * Dots are valid element names so we can't do the obvious # for id . for class trick,
	 * except when output is "html".
	 */
	@Test
	public void testUsingDots() throws XGenExpressionException, XPathExpressionException, TransformerException {
		XGen xGen = XGenFactory.newInstance(FactoryUtils.newDocument());
		// set the output method to HTML
		xGen.setOutputMethod("html");
		xGen.newDocument("/html#main/head/title");
		xGen.select("//title").setTextContent("Example");
		xGen.select("//html").create("body#home/div.container[4]");
		xGen.select("//body").create("footer.small").setTextContent("Copyleft teknopaul");
		// you can set it back to XML just before output if you want classes but not the rest of the validation that
		// comes with html output
		//xGen.setOutputMethod("xml");
		xGen.serialize(System.out);

		Assert.assertEquals("main", xGen.select("//html").item(0).getAttributes().getNamedItem("id").getNodeValue());
		Assert.assertEquals("small", xGen.select("//footer").item(0).getAttributes().getNamedItem("class").getNodeValue());
		// Asser classess dont break anything futher down
		Assert.assertEquals("Copyleft teknopaul", xGen.select("//footer").item(0).getTextContent());
		Assert.assertEquals(4, xGen.select("//div").getLength());

	}
	
	/**
	 * This is a tad hacky but instead of an xGen path you supply valid XML as a string it will get parsed and inserted.
	 * API used is org.w3c.dom.Document.adoptNode() Which has this JavaDoc.
	 * <pre>
	 * Attempts to adopt a node from another document to this document. If supported...
	 * </pre>
	 * YMMV
	 */
	@Test
	public void testInsertingXml() throws XGenExpressionException, XPathExpressionException, TransformerException {
		XGen xGen = XGenFactory.newInstance(FactoryUtils.newDocument());
		xGen.newDocument("/html/head/title");
		xGen.select("//html").create("body#home").create("<div id=\"foo\">me &amp; you</div>");
		xGen.serialize(System.out);

		Assert.assertEquals("me & you", xGen.select("//div").item(0).getTextContent());
	}
}
