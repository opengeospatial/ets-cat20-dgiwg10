package org.opengis.cite.cat20.dgiwg10.xml;

import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.OGC;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.opengis.cite.cat20.dgiwg10.util.ElementSetName;
import org.opengis.cite.cat20.dgiwg10.util.OutputSchema;
import org.opengis.cite.cat20.dgiwg10.util.TestSuiteLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestCreator {

    private static final Logger LOG = Logger.getLogger( RequestCreator.class.getName() );

    private DocumentBuilder docBuilder;

    public RequestCreator() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        try {
            this.docBuilder = factory.newDocumentBuilder();
        } catch ( ParserConfigurationException e ) {
            TestSuiteLogger.log( Level.WARNING, "Failed to create DOM parser", e );
        }
    }

    /**
     *
     * @param outputSchema
     *            the outputSchema to use in the request, never <code>null</code>
     * @param elementSetName
     *            the elementSetName to use in the request, never <code>null</code>
     * @return the request, never <code>null</code>
     * @throws IllegalArgumentException
     *             if no POST endpoint for operation GetRecords could be found in the capabilities
     */
    public Document createGetRecordsRequest( OutputSchema outputSchema, ElementSetName elementSetName ) {
        return createGetRecordsRequest( outputSchema, elementSetName, null );
    }

    /**
     * @param outputSchema
     *            the outputSchema to use in the request, never <code>null</code>
     * @param elementSetName
     *            the elementSetName to use in the request, never <code>null</code>
     * @param filter
     *            the filter to append (local name must be 'Filter'), may be <code>null</code> if no filter should be
     *            added
     * @return the request, never <code>null</code>
     * @throws IllegalArgumentException
     *             if no POST endpoint for operation GetRecords could be found in the capabilities or the passed filter
     *             does not have a local name 'Filter'
     */
    public Document createGetRecordsRequest( OutputSchema outputSchema, ElementSetName elementSetName, Node filter ) {
        Document request;
        try {
            InputStream requestAsStream = getClass().getResourceAsStream( "/org/opengis/cite/cat20/dgiwg10/getrecords/GetRecords-request.xml" );
            request = docBuilder.parse( requestAsStream );
        } catch ( IOException | SAXException e ) {
            LOG.log( Level.SEVERE, "GetRecords request could not be created", e );
            throw new IllegalArgumentException( "GetRecords request could not be created" );
        }
        Element getRecords = request.getDocumentElement();
        getRecords.setAttribute( "outputSchema", outputSchema.getOutputSchema() );
        Element query = (Element) getRecords.getElementsByTagNameNS( CSW, "Query" ).item( 0 );
        query.setAttribute( "typeNames", outputSchema.getTypeName() );
        Element elementSetNameElement = (Element) query.getElementsByTagNameNS( CSW, "ElementSetName" ).item( 0 );
        elementSetNameElement.setTextContent( elementSetName.name().toLowerCase() );
        appendFilter( request, query, filter );
        return request;
    }

    /**
     * @param outputSchema
     *            the outputSchema to use in the request, never <code>null</code>
     * @param elementSetName
     *            the elementSetName to use in the request, never <code>null</code>
     * @param identifier
     *            the id to append, never <code>null</code>
     * @return the request, never <code>null</code>
     * @throws IllegalArgumentException
     *             if no POST endpoint for operation GetRecordById could be found in the capabilities
     */
    public Document createGetRecordById( OutputSchema outputSchema, ElementSetName elementSetName, String identifier ) {
        Document request;
        try {
            InputStream requestAsStream = getClass().getResourceAsStream( "/org/opengis/cite/cat20/dgiwg10/getrecordbyid/GetRecordById-request.xml" );
            request = docBuilder.parse( requestAsStream );
        } catch ( IOException | SAXException e ) {
            LOG.log( Level.SEVERE, "GetRecords request could not be created", e );
            throw new IllegalArgumentException( "GetRecords request could not be created" );
        }
        Element getRecordById = request.getDocumentElement();
        getRecordById.setAttribute( "outputSchema", outputSchema.getOutputSchema() );
        Element id = (Element) getRecordById.getElementsByTagNameNS( CSW, "Id" ).item( 0 );
        id.setTextContent( identifier );
        Element elementSetNameElement = (Element) getRecordById.getElementsByTagNameNS( CSW, "ElementSetName" ).item( 0 );
        elementSetNameElement.setTextContent( elementSetName.name().toLowerCase() );
        return request;
    }

    /**
     * @return an CSW insert request, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the request could not be created
     */
    public Document createInsertRequest() {
        try {
            InputStream requestAsStream = getClass().getResourceAsStream( "/org/opengis/cite/cat20/dgiwg10/transaction/insert-DMFMetadata-request.xml" );
            return docBuilder.parse( requestAsStream );
        } catch ( IOException | SAXException e ) {
            LOG.log( Level.SEVERE, "Insert request could not be created", e );
            throw new IllegalArgumentException( "Insert request could not be created" );
        }
    }

    /**
     * @return an CSW update request, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the request could not be created
     */
    public Document createUpdateRequest() {
        try {
            InputStream requestAsStream = getClass().getResourceAsStream( "/org/opengis/cite/cat20/dgiwg10/transaction/update-DMFMetadata-request.xml" );
            return docBuilder.parse( requestAsStream );
        } catch ( IOException | SAXException e ) {
            LOG.log( Level.SEVERE, "Update request could not be created", e );
            throw new IllegalArgumentException( "Update request could not be created" );
        }
    }

    /**
     * @param identifier
     *            the identifier of the record to delete, never <code>null</code>
     * @return an CSW delete request, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the request could not be created
     */
    public Document createDeleteRequest( String identifier ) {
        Document document;
        try {
            InputStream requestAsStream = getClass().getResourceAsStream( "/org/opengis/cite/cat20/dgiwg10/transaction/delete-request.xml" );
            document = docBuilder.parse( requestAsStream );
        } catch ( IOException | SAXException e ) {
            LOG.log( Level.SEVERE, "Delete request could not be created", e );
            throw new IllegalArgumentException( "Delete request could not be created" );
        }
        Element transaction = document.getDocumentElement();
        Element delete = (Element) transaction.getElementsByTagNameNS( CSW, "Delete" ).item( 0 );
        Element constraint = (Element) delete.getElementsByTagNameNS( CSW, "Constraint" ).item( 0 );
        Element filter = (Element) constraint.getElementsByTagNameNS( OGC, "Filter" ).item( 0 );
        Element propertyIsEqualTo = (Element) filter.getElementsByTagNameNS( OGC, "PropertyIsEqualTo" ).item( 0 );
        Element literal = (Element) propertyIsEqualTo.getElementsByTagNameNS( OGC, "Literal" ).item( 0 );
        literal.setTextContent( identifier );
        return document;
    }

    private void appendFilter( Document request, Element query, Node filter ) {
        if ( filter == null )
            return;
        if ( !"Filter".equals( filter.getLocalName() ) )
            throw new IllegalArgumentException( "filter has no local name 'Filter'" );

        Element constraint = request.createElementNS( CSW, "Constraint" );
        constraint.setAttribute( "version", "1.1.0" );
        constraint.appendChild( request.importNode( filter, true ) );
        query.appendChild( constraint );
    }

}
