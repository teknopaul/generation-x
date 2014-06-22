package org.tp23.xgen;

import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReadmeTest {

	private static final String TITLE = "Generation X";

	@Test
	public void test() throws Exception {
		
		XGen xGen = XGenFactory.newInstance();
		xGen.setOutputMethod("html");
		
		xGen.newDocument("/html{lang=en}/head/title").setTextContent(TITLE);
		
		xGen.select("//head").create("link{rel=stylesheet}")
			.setAttribute("type", "text/css")
			.setAttribute("href", "http://maxcdn.bootstrapcdn.com/bootswatch/3.1.1/slate/bootstrap.min.css");
		
		// Container
		XGenNodeList container = xGen.select("//html").create("body/div.container");

		// Header
		container.create("div.row/div.col-md-12/h1.well").setTextContent(TITLE);
		
		// Menu
		container.create("div.row/div.col-md-2/ul.well/li[4]").setTextContent("Menu");

		// jumbotron
		xGen.select("//div[@class='row'][2]").create("div.col-md-10 jumbotron/h1").setTextContent("Programatic XML Generation");
		
		// small footer
		container.create("div.row/div.text-center/footer/p.small").setTextContent("Copyleft teknopaul");
		
		// simplified XML serialization
		xGen.serialize(new FileOutputStream("./src/test/eg/index.html"));
	}
	
	@Test
	public void testMixup() throws Exception {

		// Create doc with w3c APIs
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element root = doc.createElement("wrapper");
		doc.appendChild(root);
		
		// Add a few elements with xGen
		NodeList nl = XGenFactory.newInstance(doc).create(root, "data/fs/files/file[5]");
		
		// Carry on with w3c
		for( int i = 0 ; i < nl.getLength(); i++) {
			nl.item(i).setTextContent(getFile(i));
		}
		
		// Print with xGen
		XGenFactory.newInstance(doc).serialize(System.out);
		
		// Or print with verbose Java APIs if you prefer
		DOMSource domSource = new DOMSource(doc);
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute("indent-number", "2");
		Transformer serializer = factory.newTransformer();
		if (serializer.getClass().getName().contains("org.apache")) {
			serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		}
		serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		serializer.setOutputProperty(OutputKeys.INDENT, "yes");
		serializer.transform(domSource, new StreamResult(System.out));
	}
	
	/**
	 * This is pretty sexy code, it will get better if I add support for ../
	 * and lamdas
	 * @throws Exception
	 */
	@Test
	public void testChain() throws Exception {
		
		XGen xGen = XGenFactory.newInstance();
		xGen.newDocument("/xml")
			.create("div/ul/li[3]")
			.setTextContent("foo")
			.setAttribute("abc", "123")
			.create("a/span")
			.setAttribute("class", "grey");
		xGen.serialize(System.out);
		
	}

	@Test
	public void testNodeMutator() throws Exception {
		final int[]i = new int[1];
		XGen xGen = XGenFactory.newInstance();
		xGen.newDocument("/xml")
			.create("div/ul/li[3]").setTextContent("a", "b", "c")
			.each(new NodeMutator() {
				public Node each(Node node) {
					((Element)node).setAttribute("id", "123-" + i[0]++);
					return node;
				}
			});
		xGen.serialize(System.out);
		
	}
	
	@Test
	public void testWalking() throws Exception {
		final int[]i = new int[1];
		XGen xGen = XGenFactory.newInstance();
		xGen.newDocument("/walking/down")
			.create("the")
			.create("right/path")
			.create("keep/walking[5]")
			.each(new NodeMutator() {
				@Override
				public Node each(Node node) {
					node.setTextContent("" + i[0]++);
					return node;
				}
			});
		xGen.serialize(System.out);
		
	}
	
	private String getFile(int i) {
		return "file" + i;
	}
}
