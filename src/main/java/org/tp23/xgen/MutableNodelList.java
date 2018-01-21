package org.tp23.xgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * org.w3c.dom.NodeList implementation that accepts changes and implements Iterable.
 * 
 * I wonder why all NodeList don't implement Iterable.
 * 
 * @author teknopaul
 *
 */
public class MutableNodelList implements NodeList, Iterable<Node>  {

	private List<Node> list = new ArrayList<Node>();
	
	public MutableNodelList() {
	}
	
	public MutableNodelList(NodeList nodeList) {
		addElements(nodeList);
	}
	
	@Override
	public Node item(int index) {
		return list.get(index);
	}

	@Override
	public int getLength() {
		return list.size();
	}
	
	public void add(Node node) {
		list.add(node);
	}
	
	public void addElements(NodeList nodeList) {
		for (int i = 0 ; i < nodeList.getLength() ; i++) {
			list.add(nodeList.item(i));
		}
	}
	
	@Override
	public Iterator<Node> iterator() {
		return new Iterator<Node>() {
			int pos = 0;
			@Override
			public boolean hasNext() {
				return getLength() > pos;
			}

			@Override
			public Node next() {
				return item(pos++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
	}
}
