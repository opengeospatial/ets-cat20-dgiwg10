package org.opengis.cite.cat20.dgiwg10.util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides various utility methods for accessing or manipulating XML representations.
 */
public class XMLUtils {

    private static final XPathFactory XPATH_FACTORY = initXPathFactory();

    private static XPathFactory initXPathFactory() {
        XPathFactory factory = XPathFactory.newInstance();
        return factory;
    }

    /**
     * Writes the content of a DOM Node to a string. The XML declaration is omitted and the character encoding is set to
     * "US-ASCII" (any character outside of this set is serialized as a numeric character reference).
     *
     * @param node
     *            The DOM Node to be serialized.
     * @return A String representing the content of the given node.
     */
    public static String writeNodeToString( Node node ) {
        if ( null == node ) {
            return "";
        }
        Writer writer = null;
        try {
            Transformer idTransformer = TransformerFactory.newInstance().newTransformer();
            Properties outProps = new Properties();
            outProps.setProperty( OutputKeys.ENCODING, "US-ASCII" );
            outProps.setProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            outProps.setProperty( OutputKeys.INDENT, "yes" );
            idTransformer.setOutputProperties( outProps );
            writer = new StringWriter();
            idTransformer.transform( new DOMSource( node ), new StreamResult( writer ) );
        } catch ( TransformerException ex ) {
            TestSuiteLogger.log( Level.WARNING, "Failed to serialize node " + node.getNodeName(), ex );
        }
        return writer.toString();
    }

    /**
     * Evaluates an XPath 1.0 expression using the given context and returns the result as a node set.
     * 
     * @param context
     *            The context node.
     * @param expr
     *            An XPath expression.
     * @param namespaceBindings
     *            A collection of namespace bindings for the XPath expression, where each entry maps a namespace URI
     *            (key) to a prefix (value). Standard bindings do not need to be declared (see
     *            {@link NamespaceBindings#withStandardBindings()}.
     * @return A NodeList containing nodes that satisfy the expression (it may be empty).
     * @throws XPathExpressionException
     *             If the expression cannot be evaluated for any reason.
     */
    public static NodeList evaluateXPath( Node context, String expr, Map<String, String> namespaceBindings )
                            throws XPathExpressionException {
        Object result = evaluateXPath( context, expr, namespaceBindings, XPathConstants.NODESET );
        if ( !NodeList.class.isInstance( result ) ) {
            throw new XPathExpressionException( "Expression does not evaluate to a NodeList: " + expr );
        }
        return (NodeList) result;
    }

    /**
     * Evaluates an XPath expression using the given context and returns the result as the specified type.
     * 
     * <p>
     * <strong>Note:</strong> The Saxon implementation supports XPath 2.0 expressions when using the JAXP XPath APIs
     * (the default implementation will throw an exception).
     * </p>
     * 
     * @param context
     *            The context node.
     * @param expr
     *            An XPath expression.
     * @param namespaceBindings
     *            A collection of namespace bindings for the XPath expression, where each entry maps a namespace URI
     *            (key) to a prefix (value). Standard bindings do not need to be declared (see
     *            {@link NamespaceBindings#withStandardBindings()}.
     * @param returnType
     *            The desired return type (as declared in {@link XPathConstants} ).
     * @return The result converted to the desired returnType.
     * @throws XPathExpressionException
     *             If the expression cannot be evaluated for any reason.
     */
    public static Object evaluateXPath( Node context, String expr, Map<String, String> namespaceBindings,
                                        QName returnType )
                            throws XPathExpressionException {
        NamespaceBindings bindings = NamespaceBindings.withStandardBindings();
        bindings.addAllBindings( namespaceBindings );
        XPathFactory factory = XPATH_FACTORY;
        // WARNING: If context node is Saxon NodeOverNodeInfo, the factory must
        // use the same Configuration object to avoid IllegalArgumentException
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext( bindings );
        Object result = xpath.evaluate( expr, context, returnType );
        return result;
    }

    /**
     * Returns the selected value
     *
     * @param context
     *            The context node.
     * @param xpath
     *            An XPath expression evaluating to an integer value.
     * @return the selected value as int, -1 if the value is not a valid integer
     * @throws XPathExpressionException
     *             If the expression cannot be evaluated for any reason.
     **/
    public static int parseAsInteger( Node context, String xpath )
                            throws XPathExpressionException {
        try {
            String valueAsString = (String) XMLUtils.evaluateXPath( context, xpath, null, XPathConstants.STRING );
            return Integer.parseInt( valueAsString );
        } catch ( NumberFormatException e ) {
            return -1;
        }
    }

}
