package org.opengis.cite.cat20.dgiwg10;

import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.LOCAL_NAME;
import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.NAMESPACE_NAME;
import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.UNEXPECTED_MEDIA_TYPE;
import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.UNEXPECTED_STATUS;
import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.XPATH_RESULT;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.transform.Source;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings;
import org.opengis.cite.validation.ValidationErrorHandler;
import org.testng.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Provides a set of custom assertion methods.
 */
public class ETSAssert {

    private static final Logger LOGR = Logger.getLogger( ETSAssert.class.getPackage().getName() );

    private ETSAssert() {
    }

    /**
     * Asserts that the status code matches the expected status code.
     * 
     * @param statusCode
     *            the status code
     * @param expectedStatusCode
     *            the expected status code
     */
    public static void assertStatusCode( int statusCode, int expectedStatusCode ) {
        if ( statusCode != expectedStatusCode )
            throw new AssertionError( ErrorMessage.format( UNEXPECTED_STATUS ) );
    }

    /**
     * Asserts that the header "Content-Type" contains a value with 'xml'.
     *
     * @param headers
     *            the available headers
     */
    public static void assertXmlContentType( MultivaluedMap<String, String> headers ) {
        List<String> contentType = headers.get( "Content-Type" );
        boolean hasContentTypeXml = hasContentTypeXml( contentType );
        if ( !hasContentTypeXml )
            throw new AssertionError( UNEXPECTED_MEDIA_TYPE );
    }

    /**
     * @param valueToAssert
     *            the boolean to assert to be <code>true</code>
     * @param failureMsg
     *            the message to throw in case of a failure, should not be <code>null</code>
     */
    public static void assertTrue( boolean valueToAssert, String failureMsg ) {
        if ( !valueToAssert )
            throw new AssertionError( failureMsg );
    }

    /**
     * Asserts that the qualified name of a Document root node matches the expected value.
     *
     * @param document
     *            The document to check the root node.
     * @param namespaceUri
     *            the expected namespace uri.
     * @param localName
     *            the expected local name.
     */
    public static void assertQualifiedName( Document document, String namespaceUri, String localName ) {
        assertQualifiedName( document.getDocumentElement(), namespaceUri, localName );
    }

    /**
     * Asserts that the qualified name of a DOM Node matches the expected value.
     *
     * @param node
     *            The Node to check.
     * @param namespaceUri
     *            the expected namespace uri.
     * @param localName
     *            the expected local name.
     */
    public static void assertQualifiedName( Node node, String namespaceUri, String localName ) {
        assertEquals( node.getLocalName(), localName, ErrorMessage.get( LOCAL_NAME ) );
        assertEquals( node.getNamespaceURI(), namespaceUri, ErrorMessage.get( NAMESPACE_NAME ) );
    }

    /**
     * Asserts that an XPath 1.0 expression holds true for the given evaluation context. The standard namespace bindings
     * from NamespaceBindings.withStandardBindings() are used.
     *
     * @param context
     *            The context node.
     * @param expr
     *            A valid XPath 1.0 expression.
     */
    public static void assertXPath( Node context, String expr ) {
        assertXPath( context, expr, null, null );
    }

    /**
     * Asserts that an XPath 1.0 expression holds true for the given evaluation context. The standard namespace bindings
     * from NamespaceBindings.withStandardBindings() must not be declared.
     *
     * @param context
     *            The context node.
     * @param expr
     *            A valid XPath 1.0 expression.
     */
    public static void assertXPath( Node context, String expr, Map<String, String> namespaceBindings ) {
        assertXPath( context, expr, namespaceBindings, null );
    }

    /**
     * Asserts that an XPath 1.0 expression holds true for the given evaluation context. The standard namespace bindings
     * from NamespaceBindings.withStandardBindings() must not be declared.
     *
     * @param context
     *            The context node.
     * @param expr
     *            A valid XPath 1.0 expression.
     * @param namespaceBindings
     *            A collection of namespace bindings for the XPath expression, where each entry maps a namespace URI
     *            (key) to a prefix (value). It may be {@code null}.
     * @param assertionErrorMessage
     */
    public static void assertXPath( Node context, String expr, Map<String, String> namespaceBindings,
                                    String assertionErrorMessage ) {
        if ( null == context ) {
            throw new NullPointerException( "Context node is null." );
        }
        NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
        bindings.addAllBindings( namespaceBindings );
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext( bindings );
        Boolean result;
        try {
            result = (Boolean) xpath.evaluate( expr, context, XPathConstants.BOOLEAN );
        } catch ( XPathExpressionException xpe ) {
            String msg = ErrorMessage.format( ErrorMessageKeys.XPATH_ERROR, expr );
            LOGR.log( Level.WARNING, msg, xpe );
            throw new AssertionError( msg );
        }
        Element elemNode;
        if ( Document.class.isInstance( context ) ) {
            elemNode = Document.class.cast( context ).getDocumentElement();
        } else {
            elemNode = (Element) context;
        }
        String errorMessage = assertionErrorMessage;
        if ( errorMessage == null )
            errorMessage = ErrorMessage.format( XPATH_RESULT, elemNode.getNodeName(), expr );
        assertTrue( result, errorMessage );
    }

    /**
     * Asserts that an XML resource is schema-valid.
     *
     * @param validator
     *            The Validator to use.
     * @param source
     *            The XML Source to be validated.
     */
    public static void assertSchemaValid( Validator validator, Source source ) {
        ValidationErrorHandler errHandler = new ValidationErrorHandler();
        validator.setErrorHandler( errHandler );
        try {
            validator.validate( source );
        } catch ( Exception e ) {
            throw new AssertionError( ErrorMessage.format( ErrorMessageKeys.XML_ERROR, e.getMessage() ) );
        }
        Assert.assertFalse( errHandler.errorsDetected(), ErrorMessage.format( ErrorMessageKeys.NOT_SCHEMA_VALID,
                                                                              errHandler.getErrorCount(),
                                                                              errHandler.toString() ) );
    }

    private static boolean hasContentTypeXml( List<String> contentType ) {
        for ( String ct : contentType )
            if ( ct.contains( "xml" ) )
                return true;
        return false;
    }

}
