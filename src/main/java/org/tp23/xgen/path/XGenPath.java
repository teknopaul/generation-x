/*
 * Copyright teknopaul 2014 LGPL
 */
package org.tp23.xgen.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.tp23.xgen.XGenExpressionException;

/**
 * Parses and stores the steps of an xGenPath.
 * 
 * Paths are forward slashed and absolute or relative.
 * 
 * <code>/html/head/title</code>
 * 
 * or
 * 
 * <code>body/div</code>
 * 
 * This code only accepts relative paths, since the abosolute path is only permitted when creating a
 * new document, XML does not support two root elements.
 * 
 * This class is immutable, thus thread safe.
 * 
 * @author teknopaul
 *
 */
public class XGenPath {

	/**
	 * Position prevents having to recreate the steps each time we navigate the path.
	 * 
	 */
	private final int position;
	private List<XGenPathStep> steps;
	
	/**
	 * @param xGenPath a Non absolute xGenPath, i.e. it should not start with /
	 * @param dotIsClass if true steps with dots itn eh name are treated as name.class, similar to jQuery syntax.
	 * @throws XGenExpressionException
	 */
	public XGenPath(String xGenPath, boolean dotIsClass) throws XGenExpressionException {
		this.steps = new ArrayList<XGenPathStep>();
		String [] steps = xGenPath.split("/");
		for (String step : steps) {
			this.steps.add(new XGenPathStep(step, dotIsClass));
		}
		this.steps = Collections.unmodifiableList(this.steps);
		this.position = 0;
	}

	private XGenPath(int position, List<XGenPathStep> steps) {
		this.position = position;
		this.steps = steps;
	}

	/**
	 * Returns the current step.
	 * @return
	 */
	public XGenPathStep getStep() {
		if (position >= steps.size()) {
			return null;
		}
		return steps.get(position);
	}
	
	/**
	 * @return returns a new Path that is one step shorter.
	 */
	public XGenPath next() {
		return new XGenPath(position + 1, steps);
	}
	
	/**
	 * @return return true if this is the last segment of a path
	 */
	public boolean isTail() {
		return position == steps.size() - 1;
	}

	/**
	 * Returns a string representation of the path, regenerated from parsed input.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (XGenPathStep step : this.steps) {
			if (i++ != 0) {
				sb.append('/');
			}
			sb.append(step.toString());
		}
		return sb.toString();
	}
}
