package org.tp23.xgen;

import org.junit.Test;

import javax.xml.transform.TransformerException;

/**
 * Created by teknopaul on 6/21/14.
 */
public class SyntaxTest {

	@Test
	public void testMultipleArrays() throws XGenExpressionException, TransformerException {
		XGen xGen = XGenFactory.newInstance();
		XGenNodeList root = xGen.newDocument("/xml");
		root.create("a[2]/b[2]/c[2]");
		xGen.serialize(System.out);

	}


	@Test
	public void testDotsInIDs() throws XGenExpressionException, TransformerException {
		XGen xGen = XGenFactory.newInstance();
		XGenNodeList root = xGen.newDocument("/xml");
		root.create("a#foo.baa");
		xGen.serialize(System.out);

	}
}
