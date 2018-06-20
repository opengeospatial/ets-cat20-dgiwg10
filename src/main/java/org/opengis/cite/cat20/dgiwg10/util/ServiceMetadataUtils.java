package org.opengis.cite.cat20.dgiwg10.util;

import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;

import java.net.URI;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat20.dgiwg10.ProtocolBinding;
import org.w3c.dom.Document;

/**
 * Provides various utility methods for accessing service metadata.
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ServiceMetadataUtils {
    /**
     * Extracts a request endpoint from a WFS capabilities document.
     *
     * @param capabilitiesDoc
     *            A service metadata document (csw:Capabilities).
     * @param opName
     *            The operation (request) name.
     * @param binding
     *            The message binding to use (if {@code null} any supported binding will be used).
     * @return A URI referring to a request endpoint; the URI is empty if no matching endpoint is found.
     */
    public static URI getOperationEndpoint( final Document capabilitiesDoc, String opName, ProtocolBinding binding ) {
        if ( null == binding || binding.equals( ProtocolBinding.ANY ) ) {
            binding = getOperationBindings( capabilitiesDoc, opName ).iterator().next();
        }

        NamespaceBindings nsBindings = withStandardBindings();
        String expr = String.format( "//ows:Operation[@name='%s']//ows:%s/@xlink:href", opName,
                                     binding.getElementName() );
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext( nsBindings );
        URI endpoint = null;
        try {
            String href = xpath.evaluate( expr, capabilitiesDoc );
            endpoint = URI.create( href );
        } catch ( XPathExpressionException ex ) {
            // XPath expression is correct
            TestSuiteLogger.log( Level.INFO, ex.getMessage() );
        }
        String queryString = endpoint.getQuery();
        if ( queryString != null ) {
            String uri = endpoint.toString();
            if ( queryString.trim().isEmpty() ) {
                // remove trailing '?'
                endpoint = URI.create( uri.substring( 0, uri.indexOf( '?' ) ) );
            } else if ( !uri.endsWith( "&" ) ) {
                // make sure the query component is ready for appending extra params
                endpoint = URI.create( uri + "&" );
            }
        }
        return endpoint;
    }

    /**
     * Determines which protocol bindings are supported for a given operation.
     *
     * @param capabilitiesDoc
     *            A service metadata document (csw:Capabilities).
     * @param opName
     *            The operation (request) name.
     * @return A Set of protocol bindings supported for the operation.
     */
    public static Set<ProtocolBinding> getOperationBindings( final Document capabilitiesDoc, String opName ) {
        Set<ProtocolBinding> protoBindings = new HashSet<>();
        String expr = "//ows:Operation[@name='%s']//ows:%s";
        for ( ProtocolBinding binding : EnumSet.allOf( ProtocolBinding.class ) ) {
            String elementName = binding.getElementName();
            if ( elementName != null ) {
                String xpath = String.format( expr, opName, elementName );
                try {
                    if ( XMLUtils.evaluateXPath( capabilitiesDoc, xpath, null ).getLength() > 0 ) {
                        protoBindings.add( binding );
                    }
                } catch ( XPathExpressionException xpe ) {
                    throw new RuntimeException( "Error evaluating XPath expression against capabilities doc. ", xpe );
                }
            }
        }
        return protoBindings;
    }
}
