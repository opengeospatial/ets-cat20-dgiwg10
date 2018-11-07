package org.opengis.cite.cat20.dgiwg10.util;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;

import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Provides various utility methods for creating and configuring HTTP client components.
 */
public class ClientUtils {

    /**
     * Builds an HTTP request message that uses the GET method.
     *
     * @param endpoint
     *            A URI indicating the target resource.
     * @param qryParams
     *            A Map containing query parameters (may be null);
     * @param mediaTypes
     *            A list of acceptable media types; if not specified, generic XML ("application/xml") is preferred.
     *
     * @return A ClientRequest object.
     */
    public static ClientRequest buildGetRequest( URI endpoint, Map<String, String> qryParams, MediaType... mediaTypes ) {
        UriBuilder uriBuilder = UriBuilder.fromUri( endpoint );
        if ( null != qryParams ) {
            for ( Map.Entry<String, String> param : qryParams.entrySet() ) {
                uriBuilder.queryParam( param.getKey(), param.getValue() );
            }
        }
        URI uri = uriBuilder.build();
        ClientRequest.Builder reqBuilder = ClientRequest.create();
        if ( null == mediaTypes || mediaTypes.length == 0 ) {
            reqBuilder = reqBuilder.accept( MediaType.APPLICATION_XML_TYPE );
        } else {
            reqBuilder = reqBuilder.accept( mediaTypes );
        }
        ClientRequest req = reqBuilder.build( uri, HttpMethod.GET );
        return req;
    }

    /**
     * Obtains the (XML) response entity as a JAXP Source object and resets the entity input stream for subsequent
     * reads.
     *
     * @param response
     *            A representation of an HTTP response message.
     * @param targetURI
     *            The target URI from which the entity was retrieved (may be null).
     * @return A Source to read the entity from; its system identifier is set using the given targetURI value (this may
     *         be used to resolve any relative URIs found in the source).
     */
    public static Source getResponseEntityAsSource( ClientResponse response, String targetURI ) {
        Source source = response.getEntity( DOMSource.class );
        if ( null != targetURI && !targetURI.isEmpty() ) {
            source.setSystemId( targetURI );
        }
        if ( response.getEntityInputStream().markSupported() ) {
            try {
                // NOTE: entity was buffered by client filter
                response.getEntityInputStream().reset();
            } catch ( IOException ex ) {
                Logger.getLogger( ClientUtils.class.getName() ).log( Level.WARNING, "Failed to reset response entity.",
                                                                     ex );
            }
        }
        return source;
    }

    /**
     * Obtains the (XML) response entity as a DOM Document and resets the entity input stream for subsequent reads.
     *
     * @param response
     *            A representation of an HTTP response message.
     * @param targetURI
     *            The target URI from which the entity was retrieved (may be null).
     * @return A Document representing the entity; its base URI is set using the given targetURI value (this may be used
     *         to resolve any relative URIs found in the document).
     */
    public static Document getResponseEntityAsDocument( ClientResponse response, String targetURI ) {
        DOMSource domSource = (DOMSource) getResponseEntityAsSource( response, targetURI );
        Document entityDoc = (Document) domSource.getNode();
        entityDoc.setDocumentURI( domSource.getSystemId() );
        return entityDoc;
    }

}
