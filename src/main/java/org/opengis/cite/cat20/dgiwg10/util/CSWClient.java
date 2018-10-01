package org.opengis.cite.cat20.dgiwg10.util;

import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETCAPABILITIES;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.REQUEST_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_TYPE;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.GET;

import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class CSWClient {

    private static final Logger LOG = Logger.getLogger( CSWClient.class.getName() );

    private Client client;

    /** A Document that describes the service under test. */
    private Document capabilitiesDocument;

    /**
     * Default client constructor. The client is configured to consume SOAP message entities. The request and response
     * may be logged to a default JDK logger (in the namespace "com.sun.jersey.api.client").
     */
    public CSWClient() {
        client = createClient();
    }

    private Client createClient() {
        ClientConfig config = new DefaultClientConfig();
        Client client = Client.create( config );
        client.addFilter( new LoggingFilter() );
        return client;
    }

    private Client createClient(String username, String pw) {

        Client client = createClient();
        client.addFilter(new HTTPBasicAuthFilter(username, pw));
        return client;
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
     * Submits an HTTP request message. For POST requests the XML request entity is added as request entity.
     *
     * @param endpoint
     *            The service endpoint.
     * @param request
     *            the request to send, never <code>null</code>
     * @return A ClientResponse object representing the response message.
     */
    public ClientResponse submitPostRequest( URI endpoint, Document request ) {
        return submitPostRequest(this.client, endpoint, request);
    }

    public ClientResponse submitPostRequest( URI endpoint, Document request, String user, String pw ) {
        Client client = (user != null && pw != null) ? createClient(user, pw) : this.client;
        return submitPostRequest(client, endpoint, request);
    }

    protected ClientResponse submitPostRequest(Client client, URI endpoint, Document request ) {
        WebResource resource = client.resource( endpoint );
        Source requestBody = new DOMSource( request );
        return resource.entity( requestBody ).post( ClientResponse.class );
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

}