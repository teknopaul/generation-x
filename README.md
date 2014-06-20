# Generation-X

Programatic XML generation without templates.

Using the Java org.w3c.dom APIs to create XML docs involves a lot of boiler plate code.

This project implements an XPath like syntax for generating new XML elements.

The following _xGenPath_ will create the expected XML output.

    /html/body/div#container/table.table/tbody/tr[5]
    
This is useful for generating XML when a templating system is not a good fit.

# Install

mvn install

# Example

This example code creates a bootstrap HTML page. Generation-X is perhaps better suited to genreating
XML but the HTML schema should be familiar so it should be easy to see from this exmaple what
the intention of the code is.

    XGen xGen = XGenFactory.newInstance();
    xGen.setOutputMethod("html");
		
    xGen.newDocument("/html{lang=en}/head/title").setTextContent(TITLE);
		
    xGen.select("//head").create("link{rel=stylesheet}")
        .setAttribute("type", "text/css")
        .setAttribute("href", "http://maxcdn.bootstrapcdn.com/bootswatch/3.1.1/slate/bootstrap.min.css");
		
    // Container
    XGenNodeList container = xGen.select("//html").create("body/div.container");

    // Header
    container.create("div.row/div.col-md-12/h1.well").setTextContent(TITLE);
		
    // Menu
    container.create("div.row/div.col-md-2/ul.well/li[4]").setTextContent("Menu");

    // jumbotron
    xGen.select("//div[@class='row'][2]").create("div.col-md-10 jumbotron/h1").setTextContent("Programatic XML Generation");
		
    // small footer
    container.create("div.row/div.text-center/footer/p.small").setTextContent("Copyleft teknopaul");
		
    // simplified XML serialization
    xGen.serialize(new FileOutputStream("./src/test/eg/index.html"));

The above exmale uses only Generation-X code but behind the scenes the stnadard Java w3c Dom is used and you can 
mix and match between the APIs.

    // Create doc with w3c APIs
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = doc.createElement("wrapper");
    doc.appendChild(root);

    // Add a few elements with xGen
    NodeList nl = XGenFactory.newInstance(doc).create(root, "data/fs/files/file[5]");

    // Carry on with w3c
    for( int i = 0 ; i < nl.getLength(); i++) {
        nl.item(i).setTextContent(getFile(i));
    }

    // Print with xGen
    XGenFactory.newInstance(doc).serialize(System.out);

    // Or print with verbose Java APIs if you prefer
    DOMSource domSource = new DOMSource(doc);
    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setAttribute("indent-number", "2");
    Transformer serializer = factory.newTransformer();
    if (serializer.getClass().getName().contains("org.apache")) {
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    }
    serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    serializer.transform(domSource, new StreamResult(System.out));

# XGenNodeList

The XGen select() and create() methods return a w3c compatibly nodeList but the object returned has some handy fetures.

## XGenNodeList mutators

XGenNodeList has mutator methods that affect all the elements in the list.

    xGenNodeList.setTextContent("foo")

will add "foo" to _all_ the elements in the list.  this is similar to jQuery code where all selectors are potentially arrays.

setTextContent() takes a var-args so if you know you have three elements you can write

   xGen.create("/div/ul/li[3]").setTextContent("one", "two", "three");

setAttribute(name, value) also works on all the elements in the list.

as does appendChild()  and also select() and create().

XGenNodeList methods generally return _this_ so you can chain calls

    XGen xGen = XGenFactory.newInstance();
    xGen.newDocument("/xml")
        .create("div/ul/li[3]")
        .setTextContent("foo")
        .setAttribute("abc", "123")
        .create("a/span")
        .setAttribute("class", "grey");
    xGen.serialize(System.out);

For JDK 7 we dont have lambdas yet but there is an each method to whet your appetite.

    xGen.newDocument("/xml")
        .create("div/ul/li[3]")
        .each(new NodeMutator() {
            public void each(Node node) {
                ((Element)node).setAttribute("abc", "123");
                return node;
           }
        }); // you can chain more calls here

# Dependencies

None at runtime, JUnit during the build.
Jar is only a few K at this stage

# Licence

LGPL

# Coming Soon...

Ideas for future development

* Lamda support - need specific Java 8 builds.
* xGenPath for jQuery - would be cool should be easy to port from Java.
* xGenPath for nodejs - XML so favoured in nodejs world but might help someone.
* xGenPath for JSON - Should be as usefull for generating big JSON blobs as it is for XML.
* support for .. parent paths - This would save some xpath lookups, but might be hard to implement.


