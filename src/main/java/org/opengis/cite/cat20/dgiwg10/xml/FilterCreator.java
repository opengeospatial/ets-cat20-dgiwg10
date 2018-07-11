package org.opengis.cite.cat20.dgiwg10.xml;

import static org.opengis.cite.cat20.dgiwg10.Namespaces.OGC;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;

import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opengis.cite.cat20.dgiwg10.util.OutputSchema;
import org.opengis.cite.cat20.dgiwg10.util.TestSuiteLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FilterCreator {

    private DocumentBuilder docBuilder;

    public FilterCreator() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        try {
            this.docBuilder = factory.newDocumentBuilder();
        } catch ( ParserConfigurationException e ) {
            TestSuiteLogger.log( Level.WARNING, "Failed to create DOM parser", e );
        }
    }

    /**
     * Creates an PropertyIsEqualTo filter for 'Identifier', an existing identifier is retrieved from the DataSampler.
     *
     * <pre>
     * &lt;ogc:Filter&gt;
     *      &lt;ogc:PropertyIsEqualTo&gt;
     *          &lt;ogc:PropertyName&gt;Identifier&lt;/ogc:PropertyName&gt;
     *          &lt;ogc:Literal&gt;${literalValue}&lt;/ogc:Literal&gt;
     *      &lt;/ogc:PropertyIsEqualTo&gt;
     *  &lt;/ogc:Filter&gt;
     * </pre>
     *
     *
     * @param outputSchema
     *            the requested outputSchema, never <code>null</code>
     * @param identifier
     *            the identifier to filter for, never <code>null</code>
     * @return the filter element, <code>null</code> if no identifier could be found
     */
    public Element createIdentifierFilter( OutputSchema outputSchema, String identifier ) {
        Document document = docBuilder.newDocument();
        Element filter = document.createElementNS( OGC, "Filter" );
        Element propertyIsEqualTo = document.createElementNS( OGC, "PropertyIsEqualTo" );
        Element propertyName = document.createElementNS( OGC, "PropertyName" );
        String propertyNameValue = findPropertyNameValue( outputSchema );
        propertyName.setTextContent( propertyNameValue );
        Element literal = document.createElementNS( OGC, "Literal" );
        literal.setTextContent( identifier );
        propertyIsEqualTo.appendChild( propertyName );
        propertyIsEqualTo.appendChild( literal );
        filter.appendChild( propertyIsEqualTo );
        return filter;
    }

    private String findPropertyNameValue( OutputSchema outputSchema ) {
        if ( DC.equals( outputSchema ) )
            return "dc:identifier";
        return "Identifier";
    }

}