package org.opengis.cite.cat20.dgiwg10.xml;

import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;

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
        return request;
    }

}
