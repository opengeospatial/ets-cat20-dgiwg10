package org.opengis.cite.cat20.dgiwg10.util;

import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETCAPABILITIES;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETRECORDS;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.REQUEST_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_TYPE;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.GET;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.POST;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CSWClient {

    private static final Logger LOG = Logger.getLogger( CSWClient.class.getName() );

    private Client client;

    private DocumentBuilder docBuilder;

    /** A Document that describes the service under test. */
    private Document capabilitiesDocument;

    /**
     * Default client constructor. The client is configured to consume SOAP message entities. The request and response
     * may be logged to a default JDK logger (in the namespace "com.sun.jersey.api.client").
     */
    public CSWClient() {
        ClientConfig config = new DefaultClientConfig();
        this.client = Client.create( config );
        this.client.addFilter( new LoggingFilter() );
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        try {
            this.docBuilder = factory.newDocumentBuilder();
        } catch ( ParserConfigurationException e ) {
            TestSuiteLogger.log( Level.WARNING, "Failed to create DOM parser", e );
        }
    }

    /**
     * Constructs a client that is aware of the capabilities of a WFS implementation.
     *
     * @param capabilitiesDocument
     *            A service description (e.g. WFS capabilities document).
     */
    public CSWClient( Document capabilitiesDocument ) {
        this();
        String docElemName = capabilitiesDocument.getDocumentElement().getLocalName();
        if ( !"Capabilities".equals( docElemName ) ) {
            throw new IllegalArgumentException( "Not a capabilities document: " + docElemName );
        }
        this.capabilitiesDocument = capabilitiesDocument;
    }

    /**
     * Submits an HTTP request message. For GET requests the XML request entity is serialized to its corresponding KVP
     * string format and added to the query component of the Request-URI. For SOAP requests that adhere to the
     * "Request-Response" message exchange pattern, the outbound message entity is a SOAP envelope containing the
     * standard XML request in the body.
     *
     * @param endpoint
     *            The service endpoint.
     * @param queryParams
     *            a list of query parameters, may be <code>null</code>
     * @return A ClientResponse object representing the response message.
     */
    public ClientResponse submitGetRequest( URI endpoint, Map<String, String> queryParams ) {
        LOG.log( Level.FINE, String.format( "Submitting GET request to URI %s", endpoint ) );
        UriBuilder uriBuilder = UriBuilder.fromUri( endpoint );
        if ( queryParams != null ) {
            for ( Map.Entry<String, String> parameter : queryParams.entrySet() ) {
                uriBuilder.queryParam( parameter.getKey(), parameter.getValue() );
            }
        }
        URI requestURI = uriBuilder.build();
        LOG.log( Level.FINE, String.format( "Request URI: %s", requestURI ) );
        WebResource resource = client.resource( requestURI );
        return resource.get( ClientResponse.class );
    }

    /**
     * Retrieves a complete representation of the capabilities document from the CSW 2.0.2 implementation described by
     * the service metadata. The <code>acceptVersions</code> parameter is omitted, so the response shall reflect the
     * latest version supported by the SUT.
     *
     * @return A Document containing the response to a GetCapabilities request, or {@code null} if one could not be
     *         obtained.
     */
    public Document getCapabilities() {
        if ( this.capabilitiesDocument == null ) {
            throw new IllegalStateException( "Service description is unavailable." );
        }
        URI endpoint = ServiceMetadataUtils.getOperationEndpoint( this.capabilitiesDocument, GETCAPABILITIES, GET );
        WebResource resource = client.resource( endpoint );
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add( REQUEST_PARAM, GETCAPABILITIES );
        queryParams.add( SERVICE_PARAM, SERVICE_TYPE );
        return resource.queryParams( queryParams ).get( Document.class );
    }

    /**
     * Submits a GetRecords Request with POST encoding
     * 
     * @param outputSchema
     *            the outputSchema to use in the request, never <code>null</code>
     * @param elementSetName
     *            the elementSetName to use in the request, never <code>null</code>
     * @return the response, never <code>null</code>
     * @throws IllegalArgumentException
     *             if no POST endpoint for operation GetRecords could be found in the capabilities
     */
    public ClientResponse getRecords( OutputSchema outputSchema, ElementSetName elementSetName ) {
        URI endpoint = getOperationEndpoint( this.capabilitiesDocument, GETRECORDS, POST );
        if ( endpoint == null )
            throw new IllegalArgumentException( "No POST binding available for GetRecords request." );
        WebResource resource = client.resource( endpoint );
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
        Source requestBody = new DOMSource( request );
        return resource.entity( requestBody ).post( ClientResponse.class );
    }

}