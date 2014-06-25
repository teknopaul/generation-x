
// To make xGenNodeList[i] work, use this syntax
// Array.prototype.push.call(this, item);

var XGenNodeList = function(xGen, nodeList){
	this.xGen = xGen;
	this.list = [];
	if (nodeList) this.addElements(nodeList);
};

// @static
XGenNodeList.createSingleNodeList = function(xGen, node) {
	var xGenNodeList = new XGenNodeList(xGen);
	xGenNodeList.add(node);
	return xGenNodeList;
};

XGenNodeList.prototype.item = function(index) {
	return this.list[index];
};

Object.defineProperty(XGenNodeList.prototype, "length", {
	get: function() {
		return this.list.length;
	},
	set: function(len) {
		// noop
	}
});

XGenNodeList.prototype.getLength = function() {
	return this.list.length;
};

XGenNodeList.prototype.add = function(node) {
	this.list.push(node);
};

XGenNodeList.prototype.addElements = function(nodeList) {
	// Dom nodeList or XGenNodeList
	if (nodeList.item) {
		for (var i = 0 ; i < nodeList.length ; i++) {
			this.list.push(nodeList.item(i));
		}
	}
	// xPath array (TODO fix this so XPath returns proper NodeLists)
	else if (nodeList.length) {
		for (var i = 0 ; i < nodeList.length ; i++) {
			this.list.push(nodeList[i]);
		}
	}
};

XGenNodeList.prototype.setTextContent = function(string) {
	for (var iter = this.iterator(); iter.hasNext() ; ) {
		var node = iter.next();
		var text = this.xGen.document.createTextNode(string);
		node.appendChild(text);
	}
	return this;
};
XGenNodeList.prototype.setAttribute = function(name, value) {
	for (var iter = this.iterator(); iter.hasNext() ; ) {
		var node = iter.next();
		//TODO if (node instanceof Element) {
			node.setAttribute(name, value);
		//}
	}
	return this;
}
XGenNodeList.prototype.appendChild = function(newChild) {
	var tailNodes = new XGenNodeList(this.xGen);
	for (var iter = this.iterator(); iter.hasNext() ; ) {
		var node = iter.next();
		var clone = newChild.cloneNode(true);
		node.appendChild(clone);
		tailNodes.add(clone);
	}
	return tailNodes;
};

XGenNodeList.prototype.create = function(xGenPath) /*throws XGenExpressionException*/ {
	return this.xGen.create(this, xGenPath);
};
XGenNodeList.prototype.select(xPath) throws XPathExpressionException {
	var xGenNodeList = new XGenNodeList(xGen);
	for (var iter = this.iterator(); iter.hasNext() ; ) {
		var node = iter.next();
		xGenNodeList.addElements(xGen.select(node, xPath));
	}
	return xGenNodeList;
};

// TODO this should be better in JS 1.7, does not seem to work in node, investigate
XGenNodeList.prototype.iterator = function() {
	var iter = {
		idx : 0,
		list : this.list,
		hasNext : function() {
			return this.idx < this.list.length;
		},
		next : function() {
			if (this.idx >= this.list.length) {
				throw new StopIteration();
			}
			return this.list[this.idx++];
		}
	}
	return iter;
};

if (exports) {
	exports.XGenNodeList = XGenNodeList;
}