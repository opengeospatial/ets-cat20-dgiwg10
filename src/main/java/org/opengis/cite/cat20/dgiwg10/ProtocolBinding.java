package org.opengis.cite.cat20.dgiwg10;

/**
 * An enumerated type that indicates how a request message is bound to an application protocol. In effect, a binding
 * prescribes how the message content is mapped into a concrete exchange format.
 * 
 * <ul>
 * <li>HTTP GET</li>
 * <li>HTTP POST</li>
 * </ul>
 */
public enum ProtocolBinding {

    /** HTTP GET method */
    GET( "Get" ),
    /** HTTP POST method */
    POST( "Post" ),
    /** Any supported binding */
    ANY( null );

    private final String elementName;

    ProtocolBinding( String elementName ) {
        this.elementName = elementName;
    }

    public String getElementName() {
        return elementName;
    }

}
