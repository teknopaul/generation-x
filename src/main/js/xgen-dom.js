/*
 * Copyright teknopaul 2014 LGPL
 */

var XGenPath = require('./xgen-path.js').XGenPath;
var XGenNodeList = require('./xgen-nodelist.js').XGenNodeList;
try {
	var DOMImplementation = require('xmldom').DOMImplementation;
} catch(err) {
}
var xpath = require('xpath');

function newDocument() {
	return new DOMImplementation().createDocument();
};

var XGenFactory = {
	newInstance : function(opt) {
		if (opt.getDocumentElement) {
			return new XGen(opt);
		}
		else if (opt.createDocument) {
			return new XGen(opt.createDocument());
		}
		else if (opt.getOwnerDocument) {
			return new XGen(opt.getOwnerDocument());
		}
		else if (typeof opt === 'undefined' && DOMImplementation) {
			return new XGen(newDocument());
		}
		else {
			throw new Error("Unsupported opt " + opt);
		}
	}
};
/**
 * @constructor
 */
var XGen = function(document) {
	this.outputEncoding = "UTF-8";
	this.outputIndent = "yes";
	this.outputMethod = "xml";
	
	this.document = document;
};
/**
 * Create a new document populated with elements form the xGenPath.
 * The path must be absolute, i.e. it must start with a / and it must have a single root
 * element, this is a requirement of XML. The root may have attributes.
 * This method should be called only once per lifetime of the XGen instance.
 * 
 * @param xGenPath 
 * @return org.w3c.dom.Document
 * @throws XGenExpressionException
 */
XGen.prototype.newDocument = function(xGenPath) /*throws XGenExpressionException*/ {
	if (this.getRoot() !== null) {
		throw new Error("Document already exists");
	}
	if ( xGenPath.charAt(0) !== "/") {
		throw new Error("New document needs an absolute path to start");
	}
	var parsedGenPath = new XGenPath(xGenPath.substring(1), this._dotIsClass());
	if ( parsedGenPath.getStep().getArrayLength() != 1 ) {
		throw new Error("Must be only one root element");
	}
	var root = this.document.createElement(parsedGenPath.getStep().getElement());
	var atts = parsedGenPath.getStep().getAttributes();
	if (atts != null) {
		this._setAttributes(root, atts);
	}
	this.document.appendChild(root);
	
	var tailNodes = new XGenNodeList(this);
	var context = XGenNodeList.createSingleNodeList(this, root);
	this._createPath(context, parsedGenPath.next(), tailNodes);
	if (tailNodes.getLength() == 0) {
		tailNodes.add(root);
	}
	return tailNodes;
}

/**
 * @return The root element of the document being created.
 */
XGen.prototype.getRoot = function() {
	return this.document.documentElement;
}

// Core XML creation methods

/**
 * Create XML content, and insert it as a child of the content element.
 * 
 * @param elem where to insert the new elements, may be ommitted
 * @param xGenPath a xGen path string, or a whole parseable XML doc as a string.
 * @return The tail nodes, i.e. a list of all leaf nodes created
 * @throws XGenExpressionException
 */
XGen.prototype.create = function(/*[context], xGenPath*/) /*throws XGenExpressionException*/ {
	var tailNodes = new XGenNodeList(this);
	
	var xGenPath, context;
	// Java style method overloading
	if (arguments.length === 0) {
		throw new Error("Missing xGenPath in create()");
	}
	if (arguments.length === 1) {
		xGenPath = arguments[0];
		context = XGenNodeList.createSingleNodeList(this, this.getRoot());
	}
	else {
		xGenPath = arguments[1];
		context = new XGenNodeList(this);
		context.addElements(arguments[0]);
	}
	
	this._create(context, xGenPath, tailNodes);
	return tailNodes;
};

XGen.prototype.select = function(/* [context], xPath*/) /* throws XPathExpressionException */ {
	var xPath;
	var context;
	// Java style method overloading
	if (arguments.length === 0) {
		throw new Error("Missing xPath in select()");
	}
	if (arguments.length === 1) {
		xPath = arguments[0];
		context = this.getRoot();
	}
	else {
		xPath = arguments[1];
		context = arguments[0];
	}
	var nodeList = xpath.select(xPath, context);
	return new XGenNodeList(this, nodeList);
}
/**
 * This method should be the only method that calls Element generation if
 * parsing XML is also required. 
 */
XGen.prototype._create = function(context, xGenPath, tailNodes) /*throws XGenExpressionException*/ {
	// hackety ho hum
	if (xGenPath.charAt(0) === "<") {
		this._insert(context, xGenPath);
	} else {
		this._createPath(context, new XGenPath(xGenPath, this._dotIsClass()), tailNodes);
	}
}

/**
 * Recursively add Elements to the document.
 * 
 * @param context  The node to which more Elements are being added
 * @param xGenPath  The string expressions
 * @param tailNodes  A list of nodes returned to the client code
 * @throws XGenExpressionException
 */
XGen.prototype._createPath = function(context, xGenPath, tailNodes) /*throws XGenExpressionException*/ {

	var step = xGenPath.getStep();
	if (step == null) return;
	for (var iter = context.iterator(); iter.hasNext() ; ) {
		var node = iter.next();
		// TODO if (nextNode instanceof Element) {
			var generatedElements = this._createPathStep(node, step);
			this._createPath(generatedElements, xGenPath.next(), tailNodes);
			if (xGenPath.isTail()) {
				tailNodes.addElements(generatedElements);
			}
		//}
	}
	
}

/**
 * Recursively add a whole block of XML to the nodes.
 */
XGen.prototype._insert = function(context, xml) /*throws XGenExpressionException*/ {
	try {
		// TODO something like this but with abstract require
		var DOMParser = require('xmldom').DOMParser;
		var doc = new DOMParser().parseFromString(xml);
		for (nextNode in context.iterator()) {
			nextNode.appendChild(doc.documentElement.cloneNode(true));
		}

	}  catch (err) {
		throw err;

	}
}
/**
 * Create one step in an xGen Path e.g. if path is /html/head/body{id=index}/div[3]
 * html, head and body are all steps
 * @param context parent node to the element being created
 * @param step  step syntax is element or element{att:val} or element[n]
 */
XGen.prototype._createPathStep = function(context, step) {
	var generatedNodes = new XGenNodeList(this);
	for (var i = 0; i < step.getArrayLength(); i++) {
		var element = this.document.createElement(step.getElement());
		context.appendChild(element);
		if (step.getAttributes() != null) {
			this._setAttributes(element, step.getAttributes());
		}
		generatedNodes.add(element);
	}
	return generatedNodes;
};

XGen.prototype._setAttributes = function(elem, atts) {
	for (var key in atts) {
		elem.setAttribute(key, atts[key]);
	}
};

XGen.prototype._dotIsClass = function() {
	return "html" === this.outputMethod;
};


if (exports) {
	exports.XGen = XGen;
	exports.XGenFactory = XGenFactory; 
}