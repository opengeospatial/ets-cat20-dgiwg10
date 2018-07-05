package org.opengis.cite.cat20.dgiwg10.util;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public enum OutputSchema {

    /**
     * DublinCore
     */
    DC( "csw:Record", "http://www.opengis.net/cat/csw/2.0.2" ),

    /**
     * ISO 19139
     */
    ISO19193( "gmd:MD_Metadata", "http://www.isotc211.org/2005/gmd" );

    private final String typeName;

    private final String outputSchema;

    OutputSchema( String typeName, String outputSchema ) {
        this.typeName = typeName;
        this.outputSchema = outputSchema;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getOutputSchema() {
        return outputSchema;
    }

}