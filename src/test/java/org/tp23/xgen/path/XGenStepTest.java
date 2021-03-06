/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen.path;

import org.junit.Assert;

import org.junit.Test;

public class XGenStepTest {

	@Test
	public void test() throws Exception {
		XGenPathStep step = new XGenPathStep("elem", false);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(1, step.getArrayLength());
		Assert.assertNull(step.getAttributes());
		
		step = new XGenPathStep("elem{id=123}", false);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(1, step.getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("123", step.getAttributes().get("id"));

		step = new XGenPathStep("elem{id=123}[3]", false);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(3, step.getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("123", step.getAttributes().get("id"));

		step = new XGenPathStep("elem#123[3]", false);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(3, step.getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("123", step.getAttributes().get("id"));

		step = new XGenPathStep("elem.foo[3]", true);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(3, step.getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("foo", step.getAttributes().get("class"));

		step = new XGenPathStep("elem.foo#baa", true);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(1, step.getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("foo", step.getAttributes().get("class"));
		Assert.assertEquals("baa", step.getAttributes().get("id"));

		step = new XGenPathStep("elem#baa.foo", true);
		Assert.assertEquals("elem", step.getElement());
		Assert.assertEquals(1, step.getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("baa.foo", step.getAttributes().get("id"));
		Assert.assertEquals(null, step.getAttributes().get("class"));

		XGenPath path = new XGenPath("elem.foo[?]/baa[?]", true, 3, 4);
		Assert.assertEquals("elem", path.getStep().getElement());
		Assert.assertEquals(3, path.getStep().getArrayLength());
		Assert.assertNotNull(step.getAttributes());
		Assert.assertEquals("foo", path.getStep().getAttributes().get("class"));
		path = path.next();
		Assert.assertEquals("baa", path.getStep().getElement());
		Assert.assertEquals(4, path.getStep().getArrayLength());
		Assert.assertNull(path.getStep().getAttributes());

	}

}
