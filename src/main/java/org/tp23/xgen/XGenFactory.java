/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Factory exists to make the API like the core XML APIs.
 * You can just call new XGen();
 * 
 * @author teknopaul
 *
 */
public class XGenFactory {
	
	public static XGen newInstance() {
		return new XGen(FactoryUtils.newDocument());
	}
	public static XGen newInstance(DocumentBuilder db) {
		return new XGen(db.newDocument());
	}
	public static XGen newInstance(Document document) {
		return new XGen(document);
	}
	public static XGen newInstance(Element elem) {
		return new XGen(elem.getOwnerDocument());
	}
}
