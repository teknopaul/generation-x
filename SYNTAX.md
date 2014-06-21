# Syntax

xGenPaths define a list of elements to be created. It is important to note that an xGenPath is not an XPath. They serve very different purposes.

## Simple Example

An xGenPath at its simplest is a forward slash separated list of XML elements to create.

    /html/body/div

creates...

    <html>
      <body>
        <div/>
      </body>
    </html>

Each element in the Path is created, even if an element of the same name already exists.

Use of the following two xGenPaths on the same document would create two <body> elements, which is probably not what is desired.

    body/div
    body/footer

If the path starts with a / it is an absolute path. All XML documents have only one root node, thus a path that starts with a / can only be used for creating a new document.
Any xGenPath used to modify a document by definition must be relative.

## Creating Multiple Elements

Square brackets can be used to indicate creating multiple nodes of the same type

    div/p[4]

creates...

    <div>
      <p/>
      <p/>
      <p/>
      <p/>
    </div>

Multiple paths can be created anywhere in the xGenPath.

    a[2]/b[2]/c[2]

creates...

    <a>
      <b>
        <c/>
        <c/>
      <b/>
      <b>
        <c/>
        <c/>
      <b/>
    </a>
    <a>
      <b>
        <c/>
        <c/>
      <b/>
      <b>
        <c/>
        <c/>
      <b/>
    </a>

## Attributes

Some simple attributes may be added to an xGenPath, N.B. not all possible attributes can be expressed as xGenPaths.

    /a{att=1,foo=asdf}

Creates...

    <a att="1" foo="asdf"/>

There are no quotes, attribute syntax is not JSON, whitespace is not trimmed it is treated as literal so

    a{ att = 1 }

Is an error " att " is not a valid attribute name.

    a{att= 1 }

Creates...

    <a att=" 1 "/>

The characters .,/=}{[]# are NOT escapable.  It is not possible to create an attribute value contain these characters using an xGenPath.
Use you languages APIs for attributes with characters, in the Java reference implementation this is achieved as follows.

    create("a{title=Go to example}").setAttribute("href", "http://example.com");

Lack of escaping is a deliberate design decision. xGenPaths are already embedded as Strings in code so they already have one level of escaping.
Two levels of escaping is a headfuck, Java language APIs has a solution for all possible escaping issues.

## Syntactic sugar

There are two further additions to the format to support id and class attributes which make xGenPaths a bit clearer in certain circumstances.
Use of them is optional.

### Class attributes

An optional feature, that is must be possible to deisable., is for the period character "." to denote aan elements class attribute.
This should be a familiar syntax for those who have worked with HTML selectors.

    div.container

Creates

    <div class="container"/>

It is important that this feature is optional so that under some circumstances

    div.container

Creates...

    <div.container/>

A period is a valid part of an XML element name.

### Hash IDs

The # character can be used after the element or element and class definition as a shorthand for specifying an ID for the element.
This is not optional # is not a valid part of an XML element name.

    div#container

Creates

    <div id="container"/>

The order of the class and hash is not significant, class MUST preced an id

    div.small#container

Creates

    <div class="small" id="container"/>

The following is probably an error

    div#container.small

Creates

    <div id="container.small"/>

Again, there is no escaping, so it is not possible to create <a class="#"/> using xGenPath.

## Parents

.. is reserved for a future version. It is an error to include .. as an element name in the initial version.

Future versions may support .. at the start of an xGenPath and this will denote modifying only the initial context at which element creation begins.

    ../../a


# Summary

I could have just written /html{lang=en}/body/div.small#container[2] and you probably would have got it.

xGenPaths are simpler than XPaths.

# Author

teknopaul

# xGenPath License

Creative Commons Attribution-NoDerivatives 4.0 International

<a rel="license" href="http://creativecommons.org/licenses/by-nd/4.0/"><img alt="Creative Commons License" style="border-width:0" src="http://i.creativecommons.org/l/by-nd/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nd/4.0/">Creative Commons Attribution-NoDerivatives 4.0 International License</a>

i.e. you can copy and use the spec. but you cant play silly buggers with it.  If you come up some funky extensions, get in touch.
