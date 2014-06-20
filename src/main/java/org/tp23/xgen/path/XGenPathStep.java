/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen.path;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.tp23.xgen.XGenExpressionException;

/**
 * Parses a single step in the xGen path.
 * 
 * Syntax is as follows...
 * 
 * <pre>
 * name.class#id{att1=val,att2=val}[9]
 * </pre>
 * 
 * <li>Order of class and id is important.
 * <li>Whitespace is not permitted except in attribute values.
 * <li>There is no escaping facility, for example to use "=" is an attribute value you must
 * use the standard org.w3c.dom APIs and not xGenPath.
 * 
 * This object is immutable thus thread safe.
 * 
 * @author teknopaul
 *
 */
public class XGenPathStep {

	/**
	 * Name of the element to be created
	 */
	private String element;
	/**
	 * Attributes to be created, or null
	 */
	private Map<String, String> attributes = null;
	/**
	 * Number of elements to create
	 */
	private int arrayLength = 1;
	
	/**
	 * Parse the step and store the components 
	 * @param step
	 * @param dotIsClass supporting period "." as classname is optional because a period is a valid part of an XML name.
	 * @throws XGenExpressionException
	 */
	public XGenPathStep(String step, boolean dotIsClass) throws XGenExpressionException {
		parse(step, dotIsClass);
		if (this.element == null || this.element.equals("")) {
			throw new XGenExpressionException("Element name missing in step");
		}
	}
	
	public Map<String, String> getAttributes() {
		return attributes;
	}

	public String getElement() {
		return element;
	}

	public int getArrayLength() {
		return arrayLength;
	}

	/**
	 * The string is parsed backwards.
	 * 
	 * <li>array identifyer
	 * <li>attributes
	 * <li>id
	 * <li>class
	 * <li>name
	 */
	private void parse(String step, boolean dotIsClass) throws XGenExpressionException {
		// parse backwards
		String stepPart = step;
		
		// chop off array suffix
		if (step.endsWith("]")) {
			// TODO if [ missing throw XGenSyntaxException
			String arraySize = stepPart.substring(stepPart.lastIndexOf('[') + 1, stepPart.lastIndexOf(']'));
			try {
				this.arrayLength = Integer.parseInt(arraySize);
			} catch (NumberFormatException e) {
				throw new XGenExpressionException("Array index invalid parsing step " + step, e);
			}
			stepPart = stepPart.substring(0, step.lastIndexOf('['));
		}
		
		// chop off attributes
		if (stepPart.endsWith("}")) {
			// TODO if { missing throw XGenSyntaxException
			this.attributes = new HashMap<String, String>(); 
			String attsString = stepPart.substring(stepPart.lastIndexOf('{') + 1, stepPart.lastIndexOf('}'));
			stepPart = stepPart.substring(0, step.lastIndexOf('{'));
			// for now no escapeing
			String[] atts = attsString.split(",");
			for (String att : atts) {
				String[] nameVal = att.split("=");
				try {
					this.attributes.put(nameVal[0], nameVal[1]);
				} catch (IndexOutOfBoundsException e) {
					throw new XGenExpressionException("Attribute syntax invalid parsing step " + step, e);
				}
			}
		}
		
		// Syntactic Sugar,  body#index.container  converts to <body id="index" class="container">
		if (dotIsClass && stepPart.indexOf('.') > -1) {
			if (this.attributes == null) {
				this.attributes = new HashMap<String, String>();
			}
			this.attributes.put("class", stepPart.substring(stepPart.indexOf('.') + 1));
			stepPart = stepPart.substring(0, stepPart.indexOf('.'));
		}
		
		if (stepPart.indexOf('#') > -1) {
			if (this.attributes == null) {
				this.attributes = new HashMap<String, String>();
			}
			this.attributes.put("id", stepPart.substring(stepPart.indexOf('#') + 1));
			stepPart = stepPart.substring(0, stepPart.indexOf('#'));
		}

		this.element = stepPart;
		
		if (this.attributes != null) {
			this.attributes = Collections.unmodifiableMap(this.attributes);
		}

	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.element);
		if (this.attributes != null) {
			sb.append("{");
			int i = 0;
			for (String key : this.attributes.keySet()) {
				if (i++ != 0) {
					sb.append(',');
				}
				sb.append(key).append('=').append(this.attributes.get(key));
			}
			sb.append("}");
		}
		if (this.arrayLength > 1) {
			sb.append("[");
			sb.append(this.arrayLength);
			sb.append("]");
		}
		return sb.toString();
	}
}
