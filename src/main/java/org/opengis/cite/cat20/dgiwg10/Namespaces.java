package org.opengis.cite.cat20.dgiwg10;

import java.net.URI;

/**
 * XML namespace names.
 * 
 * @see <a href="http://www.w3.org/TR/xml-names/">Namespaces in XML 1.0</a>
 *
 */
public class Namespaces {

    private Namespaces() {
    }

    /** W3C XLink */
    public static final String XLINK = "http://www.w3.org/1999/xlink";

    public static final String XLINK_PREFIX = "xlink";

    /** OGC 05-008c1 (OWS 1.0) */
    public static final String OWS = "http://www.opengis.net/ows";

    public static final String OWS_PREFIX = "ows";

    /** W3C XML Schema namespace */
    public static final URI XSD = URI.create( "http://www.w3.org/2001/XMLSchema" );

    /** Schematron (ISO 19757-3) namespace */
    public static final URI SCH = URI.create( "http://purl.oclc.org/dsdl/schematron" );

    /** OGC 07-006r1 (CSW 2.0.2) */
    public static final String CSW = "http://www.opengis.net/cat/csw/2.0.2";

    public static final String CSW_PREFIX = "csw";

    /** OGC 04-095, (FES 1.1) */
    public static final String OGC = "http://www.opengis.net/ogc";

    public static final String OGC_PREFIX = "ogc";

    /** Dublin Core */
    public static final String DC = "http://purl.org/dc/elements/1.1/";

    public static final String DC_PREFIX = "dc";

}
