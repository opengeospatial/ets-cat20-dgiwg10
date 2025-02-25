package org.opengis.cite.cat20.dgiwg10.util;

import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETCAPABILITIES;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.REQUEST_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_TYPE;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.GET;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.w3c.dom.Document;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

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

    private Client createClient() {;
        return ClientUtils.buildClient();
    }

    private Client createClient(String username, String pw) {
        Client client = createClient();
        client.register(HttpAuthenticationFeature.basic(username, pw));
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
    public Response submitGetRequest( URI endpoint, Map<String, String> queryParams ) {
        LOG.log( Level.FINE, String.format( "Submitting GET request to URI %s", endpoint ) );
        UriBuilder uriBuilder = UriBuilder.fromUri( endpoint );
        if ( queryParams != null ) {
            for ( Map.Entry<String, String> parameter : queryParams.entrySet() ) {
                uriBuilder.queryParam( parameter.getKey(), parameter.getValue() );
            }
        }
        URI requestURI = uriBuilder.build();
        LOG.log( Level.FINE, String.format( "Request URI: %s", requestURI ) );
        WebTarget target = this.client.target(requestURI);
        Builder reqBuilder = target.request();
        return reqBuilder.buildGet().invoke();
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
    public Response submitPostRequest( URI endpoint, Document request ) {
        return submitPostRequest(this.client, endpoint, request);
    }

    public Response submitPostRequest( URI endpoint, Document request, String user, String pw ) {
        Client client = (user != null && pw != null) ? createClient(user, pw) : this.client;
        return submitPostRequest(client, endpoint, request);
    }

    protected Response submitPostRequest(Client client, URI endpoint, Document request ) {
        Source requestBody = new DOMSource( request );
        WebTarget target = this.client.target(endpoint);
        return target.request().buildPost(Entity.entity(requestBody, MediaType.APPLICATION_XML)).invoke();
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
        WebTarget target = this.client.target(endpoint);
        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.add( REQUEST_PARAM, GETCAPABILITIES );
        queryParams.add( SERVICE_PARAM, SERVICE_TYPE );
        UriBuilder uriBuilder = UriBuilder.fromUri(endpoint);
        if (null != queryParams) {
                for (Entry<String, List<String>> param : queryParams.entrySet()) {
                        uriBuilder.queryParam(param.getKey(), param.getValue().get(0));
                }
        }
        return target.request().buildGet().invoke().readEntity(Document.class);
    }

}