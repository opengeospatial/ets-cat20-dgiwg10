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
     *          &lt;ogc:Literal&gt;${identifier}&lt;/ogc:Literal&gt;
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
        String propertyName = findIdentifierPropertyName( outputSchema );
        return createPropertyIsEqualToFilter( propertyName, identifier );
    }

    /**
     * Creates an PropertyIsEqualTo filter for 'Title', an existing title is retrieved from the DataSampler.
     *
     * <pre>
     * &lt;ogc:Filter&gt;
     *      &lt;ogc:PropertyIsEqualTo&gt;
     *          &lt;ogc:PropertyName&gt;Title&lt;/ogc:PropertyName&gt;
     *          &lt;ogc:Literal&gt;${title}&lt;/ogc:Literal&gt;
     *      &lt;/ogc:PropertyIsEqualTo&gt;
     *  &lt;/ogc:Filter&gt;
     * </pre>
     *
     *
     * @param outputSchema
     *            the requested outputSchema, never <code>null</code>
     * @param title
     *            the title to filter for, never <code>null</code>
     * @return the filter element, <code>null</code> if no title could be found
     */
    public Element createTitleFilter( OutputSchema outputSchema, String title ) {
        String propertyName = findTitlePropertyName( outputSchema );
        return createPropertyIsEqualToFilter( propertyName, title );
    }

    /**
     * Creates an PropertyIsLike filter for 'Title', an existing title is retrieved from the DataSampler.
     *
     * <pre>
     * &lt;ogc:Filter&gt;
     *      &lt;ogc:PropertyIsLike capeChar="\" singleChar="?" wildCard="*" &gt;
     *          &lt;ogc:PropertyName&gt;Title&lt;/ogc:PropertyName&gt;
     *          &lt;ogc:Literal&gt;${title}&lt;/ogc:Literal&gt;
     *      &lt;/ogc:PropertyIsLike&gt;
     *  &lt;/ogc:Filter&gt;
     * </pre>
     *
     *
     * @param outputSchema
     *            the requested outputSchema, never <code>null</code>
     * @param propertyValue
     *            the value to filter for, never <code>null</code>
     * @return the filter element, <code>null</code> if no title could be found
     */
    public Element createAnyTextFilter( OutputSchema outputSchema, String propertyValue ) {
        String propertyName = "AnyText";
        return createPropertyIsLikeFilter( propertyName, propertyValue );
    }

    private Element createPropertyIsEqualToFilter( String propertyName, String propertyValue ) {
        Document document = docBuilder.newDocument();
        Element propertyIsEqualTo = document.createElementNS( OGC, "PropertyIsEqualTo" );
        return createAndAppendPropertyNameAndValue( document, propertyIsEqualTo, propertyName, propertyValue );
    }

    private Element createPropertyIsLikeFilter( String propertyName, String propertyValue ) {
        Document document = docBuilder.newDocument();
        Element propertyIsLike = document.createElementNS( OGC, "PropertyIsLike" );
        propertyIsLike.setAttribute( "escapeChar", "\\" );
        propertyIsLike.setAttribute( "singleChar", "?" );
        propertyIsLike.setAttribute( "wildCard", "*" );
        return createAndAppendPropertyNameAndValue( document, propertyIsLike, propertyName, propertyValue );
    }

    private Element createAndAppendPropertyNameAndValue( Document document, Element propertyIsEqualTo,
                                                         String propertyName, String propertyValue ) {
        Element filter = document.createElementNS( OGC, "Filter" );
        Element propertyNameElement = document.createElementNS( OGC, "PropertyName" );
        propertyNameElement.setTextContent( propertyName );
        Element literal = document.createElementNS( OGC, "Literal" );
        literal.setTextContent( propertyValue );
        propertyIsEqualTo.appendChild( propertyNameElement );
        propertyIsEqualTo.appendChild( literal );
        filter.appendChild( propertyIsEqualTo );
        return filter;
    }

    private String findIdentifierPropertyName( OutputSchema outputSchema ) {
        if ( DC.equals( outputSchema ) )
            return "dc:identifier";
        return "Identifier";
    }

    private String findTitlePropertyName( OutputSchema outputSchema ) {
        if ( DC.equals( outputSchema ) )
            return "dc:title";
        return "Title";
    }

}