/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This NodeList implementation is a more convenient than the org.w3c one. It support mutating the elements in the list
 * and it  can be iterated with foreach loops.
 * 
 * This can result in jQuery style coding where lists of results are treated the same as single results. 
 * 
 * @author teknopaul
 *
 */
public class XGenNodeList extends MutableNodelList {
	
	private XGen xGen;
	
	public XGenNodeList(XGen xGen) {
		this.xGen = xGen;
	}

	public XGenNodeList(XGen xGen, NodeList nodeList) {
		super(nodeList);
		this.xGen = xGen;
	}
	
	public static XGenNodeList createSingleNodeList(XGen xGen, Node node) {
		XGenNodeList xGenNodeList = new XGenNodeList(xGen);
		xGenNodeList.add(node);
		return xGenNodeList;
	}
	
	public XGenNodeList setTextContent(String string) {
		for (Node node : this) {
			node.setTextContent(string);
		}
		return this;
	}
	public XGenNodeList setTextContent(String...strings) {
		for (int i = 0 ; i < getLength() && i < strings.length; i++) {
			item(i).setTextContent(strings[i]);
		}
		return this;
	}
	
	public XGenNodeList setAttribute(String name, String value) {
		for (Node node : this) {
			if (node instanceof Element) {
				((Element)node).setAttribute(name, value);
			}
		}
		return this;
	}
	public XGenNodeList setAttribute(Attr... newAttr) {
		for (int i = 0 ; i < getLength() && i < newAttr.length; i++) {
			Node node = item(i);
			if (node instanceof Element) {
				((Element)node).setAttributeNode(newAttr[i]);
			}
		}
		return this;
	}
	
	public XGenNodeList appendChild(Node newChild) {
		final XGenNodeList tailNodes = new XGenNodeList(xGen);
		for (Node node : this) {
			Node clone = newChild.cloneNode(true);
			node.appendChild(clone);
			tailNodes.add(clone);
		}
		return tailNodes;
	}
	public XGenNodeList appendChild(Node... newChild) {
		final XGenNodeList tailNodes = new XGenNodeList(xGen);
		for (int i = 0 ; i < getLength() && i < newChild.length; i++) {
			item(i).appendChild(newChild[i]);
			tailNodes.add(newChild[i]);
		}
		return tailNodes;
	}
	
	public XGenNodeList each(NodeMutator mutator) {
		final XGenNodeList tailNodes = new XGenNodeList(xGen);
		for (Node node : this) {
			tailNodes.add(mutator.each(node));
		}
		return tailNodes;
	}
	
	public XGenNodeList create(String xGenPath) throws XGenExpressionException {
		return xGen.create(this, xGenPath);
	}
	
	public XGenNodeList select(String xPath) throws XPathExpressionException {
		XGenNodeList xGenNodeList = new XGenNodeList(xGen);
		for (Node node : this) {
			xGenNodeList.addElements(xGen.select((Element)node, xPath));
		}
		return xGenNodeList;
	}
}
