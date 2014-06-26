// Generated by browserify.awk, do not edit
/*
 * Copyright teknopaul 2014 LGPL
 */

/**
 * Parser and container for parsed xGenPaths
 * that define XML elements to be created.
 * 
 * @constructor
 */
var XGenPath = function(xGenPath, dotIsClass) {
	this._position = 0;
	this._steps = [];
	this._xGenPath = xGenPath;
	this._dotIsClass = typeof dotIsClass == 'undefined' ? true : dotIsClass;
	// xGenPath is null when created by next()
	if (xGenPath) {
		var steps = xGenPath.split("/");
		for (var step in steps) {
			this._steps.push(new XGenPathStep(steps[step], this._dotIsClass));
		}
	}
};

XGenPath.prototype.getStep = function() {
	if (this._position >= this._steps.length) {
		return null;
	}
	return this._steps[this._position];
}

/**
 * @return returns a new Path that is one step shorter.
 */
XGenPath.prototype.next = function() {
	var newPath = new XGenPath(null, this.dotIsClass);
	newPath._position = this._position + 1;
	newPath._steps = this._steps;
	return newPath;
}

/**
 * @return return true if this is the last segment of a path
 */
XGenPath.prototype.isTail = function() {
	return this._position == this._steps.length - 1;
};



// PATH STEPS
/**
 * @constructor
 */
var XGenPathStep = function(step, dotIsClass) {
	/**
	 * Name of the element to be created
	 */
	this.element;
	/**
	 * Attributes to be created, or null
	 */
	this.attributes = null;
	/**
	 * Number of elements to create
	 */
	this.arrayLength = 1;
	
	this.parse(step, dotIsClass);
	
	if (this.element === null || this.element === "") {
		throw new Error("Element name missing in step");
	}
	
};

XGenPathStep.prototype.parse = function(step, dotIsClass) {
	// parse backwards
	var stepPart = step;
	
	// chop off array suffix
	if (step.lastIndexOf("]") === step.length - 1) {
		// TODO if [ missing throw XGenSyntaxException
		var arraySize = stepPart.substring(stepPart.lastIndexOf('[') + 1, stepPart.lastIndexOf(']'));
		try {
			this.arrayLength = parseInt(arraySize);
		} catch (err) {
			throw new Error("Array index invalid parsing step " + step);
		}
		stepPart = stepPart.substring(0, step.lastIndexOf('['));
	}
	
	// chop off attributes
	if (stepPart.lastIndexOf("}") === step.length - 1) {
		// TODO if { missing throw XGenSyntaxException
		this.attributes = {}; 
		var attsString = stepPart.substring(stepPart.lastIndexOf('{') + 1, stepPart.lastIndexOf('}'));
		stepPart = stepPart.substring(0, step.lastIndexOf('{'));
		// for now no escapeing
		var atts = attsString.split(",");
		for (var a = 0 ; a < atts.length ; a++) {
			var nameVal = atts[a].split("=");
			try {
				this.attributes[nameVal[0]] = nameVal[1];
			} catch (err) {
				throw new Error("Attribute syntax invalid parsing step " + step);
			}
		}
	}
	
	// Syntactic Sugar,  body.container#index  converts to <body id="index" class="container">
	
	if (stepPart.indexOf('#') > -1) {
		if (this.attributes === null) {
			this.attributes = {};
		}
		this.attributes.id = stepPart.substring(stepPart.indexOf('#') + 1);
		stepPart = stepPart.substring(0, stepPart.indexOf('#'));
	}
	
	if (dotIsClass && stepPart.indexOf('.') > -1) {
		if (this.attributes === null) {
			this.attributes = {};
		}
		this.attributes.class = stepPart.substring(stepPart.indexOf('.') + 1);
		stepPart = stepPart.substring(0, stepPart.indexOf('.'));
	}

	this.element = stepPart;

};
XGenPathStep.prototype.getAttributes = function() {
	return this.attributes;
};
XGenPathStep.prototype.getElement = function() {
	return this.element;
};
XGenPathStep.prototype.getArrayLength = function() {
	return this.arrayLength;
};

/*
 * Copyright teknopaul 2014 LGPL
 */

/**
 * NodeList is an Array with a length and an item(idx) method.
 *  
 * @constructor
 */
var XGenNodeList = function(xGen, nodeList){
	this.xGen = xGen;
	this.length = 0;
	if (nodeList) this.addElements(nodeList);
};

/**
 * Factory method for node list containing only one node.
 * @static
 */
XGenNodeList.createSingleNodeList = function(xGen, node) {
	var xGenNodeList = new XGenNodeList(xGen);
	xGenNodeList.add(node);
	return xGenNodeList;
};

/**
 * Make this compatible with DOM level 2 NodeList.
 */
XGenNodeList.prototype.item = function(index) {
	return this[index];
};

// start - MutableNodeList, N.B. these do NOT change the DOM //

XGenNodeList.prototype.add = function(node) {
	if (typeof node === 'undefined') return;
	// magic to make this[0] work
	Array.prototype.push.call(this, node);
};

XGenNodeList.prototype.addElements = function(nodeList) {
	// DOM nodeList
	if (nodeList.item) {
		for (var i = 0 ; i < nodeList.length ; i++) {
			Array.prototype.push.call(this, nodeList.item(i));
		}
	}
	// xPath array (TODO fix this so XPath returns proper NodeLists)
	else if (nodeList.length) {
		for (var i = 0 ; i < nodeList.length ; i++) {
			Array.prototype.push.call(this, nodeList[i]);
		}
	}
};

// end - MutableNodeList //

// start DOM Mutations //

/**
 * Accepts a single string argument, in which case the same string is used for each node.
 * or multiple strings in which case the number of arguments should match
 * the number of nodes in the list.
 */
XGenNodeList.prototype.setTextContent = function() {
	for (var i = 0 ; i < this.length ; i++) {
		var node = this[i];
		var string = "";
		if (arguments.length === 1) {
			string = arguments[0];
		}
		else {
			string = arguments[i];
		}
		var textNode = this.xGen.document.createTextNode(string);
		node.appendChild(textNode);
	} 
	return this;
};

/**
 * Adds an attribute to each node in the list.
 */
XGenNodeList.prototype.setAttribute = function(name, value) {
	for (var i = 0 ; i < this.length ; i++) {
		var node = this[i];
		node.setAttribute(name, value);
	}
	return this;
};

/**
 * Adds a different attribute to each node in the list.
 * 
 * Accepts an array of objects to be set as attributes
 * create("div/ul/li[4]").setAttributes([
 *   { class : "odd selected", id : "main-menu" },
 *   { class : "even" },
 *   { class : "odd"  },
 *   { class : "even" },
 * ]);
 * 
 * or an array of length one in which case the same attributes
 * are added to each node in the list.
 */
XGenNodeList.prototype.setAttributes = function(arr) {
	for (var i = 0 ; i < this.length ; i++) {
		var node = this[i];
		var idx = arr.length > 1 ? i : 0;
		var atts = arr[idx];
		for (var att in atts) {
			node.setAttribute(att, atts[att]);
		}
	}
	return this;
};

/**
 * Appends a clone of the supplied node to each node in the list.
 */
XGenNodeList.prototype.appendChild = function(newChild) {
	var tailNodes = new XGenNodeList(this.xGen);
	for (var i = 0 ; i < this.length ; i++) {
		var node = this[i];
		var clone = newChild.cloneNode(true);
		node.appendChild(clone);
		tailNodes.add(clone);
	}
	return tailNodes;
};

/**
 * Calls a function for each node in the list.
 * 
 * The function should typically return the node it is given 
 * to allow continuation, it may also return new nodes created.
 * It may return nothing to filter the list.
 * 
 * @param function which is supplied the node as an argument.
 * @return tailnodes if any nodes are created.
 */
XGenNodeList.prototype.each = function(lambda) {
	var tailNodes = new XGenNodeList(this.xGen);
	for (var i = 0 ; i < this.length ; i++) {
		tailNodes.add(lambda(this[i], this.xGen));
	}
	return tailNodes;
};

/**
 * Create elements based on an xGenPAth for each node in the list.
 */
XGenNodeList.prototype.create = function(xGenPath) /*throws XGenExpressionException*/ {
	return this.xGen.create(this, xGenPath);
};

/**
 * Select nodes starting from each node in the list.
 * select() uses XPaths of CSS Selectors depending on the runtime context, web or nodejs.
 */
XGenNodeList.prototype.select = function(xPath) {
	var xGenNodeList = new XGenNodeList(this.xGen);
	for (var i = 0 ; i < this.length ; i++) {
		var node = this[i];
		xGenNodeList.addElements(xGen.select(node, xPath));
	}
	return xGenNodeList;
};

/**
 * jQuerify this nodelist
 * e.g. 
 * ...
 * .create("p/ul/li[5]").$().hide();
 */
XGenNodeList.prototype.$ = function() {
	return $(this);
};

/*
 * Copyright teknopaul 2014 LGPL
 */

/**
 * XGenPath implementation for creating DOM elements.
 * 
 * NodeJS version uses..
 * 'xmldom' (https://github.com/jindw/xmldom) which is a DOM Level 2 compliant API
 * and 
 * 'xpath' (https://github.com/goto100/xpath)
 * 
 * Other DOM Level to docs are supported by creating them and suppling the created doc 
 * as an argument to the XGenFactory.newInstance() method.
 * If you do that creating XML with full XML strings is not supported 
 * i.e.  create("<xml/>")  will throw an Error.
 * 
 * This implementation should also work in a browser.
 * 
 * Not yet tested.
 * 
 * <script src="xgen-path.js"/>
 * <script src="xgen-nodelist.js"/>
 * <script src="xgen-dom.js"/>
 */


var XGenFactory = {
	newInstance : function(opt) {
		
		if (typeof opt === 'undefined' && DOMImplementation) {
			return new XGen(new DOMImplementation().createDocument());
		}
		else if (opt.getDocumentElement) {
			return new XGen(opt);
		}
		else if (opt.createDocument) {
			return new XGen(opt.createDocument());
		}
		else if (opt.getOwnerDocument) {
			return new XGen(opt.getOwnerDocument());
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
	if ( ! document ) throw new Error("Document object is required"); 
	
	this.outputEncoding = "UTF-8"; // TODO
	this.outputIndent = "yes";     // TODO
	this.outputMethod = "xml";
	this.selectMode = XGen.QRY_MODE_XPATH;
	
	this.document = document;
	
};

/** 
 * Select with XPaths, works if require('xpath') does 
 */
XGen.QRY_MODE_XPATH = 1;
/** 
 * Select with CSS Selectors, jQuery style, works in a web page 
 */
XGen.QRY_MODE_SELECTOR = 2;

XGen.prototype.configure = function(options) {
	if ( ! options) {
		if (typeof window !== 'undefined' && typeof window.document !== 'undefined') {
			this.selectMode = XGen.QRY_MODE_SELECTOR;
			this.outputMethod = "html";
			return;
		}
		options = {};
	} 
	this.outputEncoding = options.outputEncoding || "UTF-8";
	this.outputIndent = options.outputIndent || "yes";
	this.outputMethod = options.outputMethod || "xml";
	this.selectMode = options.selectMode || XGen.QRY_MODE_XPATH;
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
	if (tailNodes.length == 0) {
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
	var path;
	var context;
	// Java style method overloading
	if (arguments.length === 0) {
		throw new Error("Missing path in select()");
	}
	if (arguments.length === 1) {
		path = arguments[0];
		context = this.getRoot();
	}
	else {
		path = arguments[1];
		context = arguments[0];
	}
	
	if (this.selectMode === XGen.QRY_MODE_SELECTOR) {
		return this._selectQuerySelector(context, path);
	}
	else if (this.selectMode === XGen.QRY_MODE_XPATH) {
		return this._selectXPath(context, path);
	}
	else {
		return this._selectXPath(context, path);
	}
}
 
XGen.prototype._selectXPath = function(context, xPath) /* throws XPathExpressionException */ {
	var nodeList = xpath.select(xPath, context);
	return new XGenNodeList(this, nodeList);
};
XGen.prototype._selectQuerySelector = function(context, querySelector) /* throws XPathExpressionException */ {
	var nodeList = context.querySelectorAll(querySelector);
	return new XGenNodeList(this, nodeList);
};

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
	for (var i = 0 ; i < context.length ; i++) {
		var node = context.item(i);
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
XGen.prototype._insert = function(context, xml) {
	var doc = new DOMParser().parseFromString(xml);
	for (var i = 0 ; i < context.length ; i++) {
		var nextNode = context.item(i);
		nextNode.appendChild(doc.documentElement.cloneNode(true));
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

