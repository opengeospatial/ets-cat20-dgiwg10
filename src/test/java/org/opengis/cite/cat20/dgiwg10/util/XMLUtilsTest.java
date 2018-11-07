package org.opengis.cite.cat20.dgiwg10.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the XMLUtils class.
 */
public class XMLUtilsTest {

    private static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    private static final String EX_NS = "http://example.org/ns1";

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUpClass()
                            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void writeDocToString()
                            throws SAXException, IOException {
        Document doc = docBuilder.parse( this.getClass().getResourceAsStream( "../getcapabilities/GetCapabilities-response.xml" ) );
        String content = XMLUtils.writeNodeToString( doc );
        assertTrue( "String should start with '<!-- Example from'", content.startsWith( "<!-- Example from" ) );
    }

    @Test
    public void evaluateXPathExpression_match()
                            throws XPathExpressionException, SAXException, IOException {
        Document doc = docBuilder.parse( this.getClass().getResourceAsStream( "../getcapabilities/GetCapabilities-response.xml" ) );
        String expr = "/csw:Capabilities/ows:ServiceIdentification[ows:Title]";
        NodeList results = XMLUtils.evaluateXPath( doc, expr, NamespaceBindings.withStandardBindings().getAllBindings() );
        assertTrue( "Expected 1 node in results.", results.getLength() == 1 );
        assertEquals( "ServiceIdentification", results.item( 0 ).getLocalName() );
    }

    @Test
    public void evaluateXPathExpression_noMatch()
                            throws XPathExpressionException, SAXException, IOException {
        Document doc = docBuilder.parse( this.getClass().getResourceAsStream( "../getcapabilities/GetCapabilities-response.xml" ) );
        String expr = "/csw:Capabilities/ows:ServiceIdentification[not(ows:Title)]";
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put( ATOM_NS, "tns" );
        nsBindings.put( EX_NS, "ns1" );
        NodeList results = XMLUtils.evaluateXPath( doc, expr, nsBindings );
        assertTrue( "Expected empty results.", results.getLength() == 0 );
    }

    @Test(expected = XPathExpressionException.class)
    public void evaluateXPathExpression_booleanResult()
                            throws XPathExpressionException, SAXException, IOException {
        Document doc = docBuilder.parse( this.getClass().getResourceAsStream( "../getcapabilities/GetCapabilities-response.xml" ) );
        String expr = "count(//tns:entry) > 0";
        Map<String, String> nsBindings = new HashMap<>();
        nsBindings.put( ATOM_NS, "tns" );
        NodeList results = XMLUtils.evaluateXPath( doc, expr, nsBindings );
        assertNull( results );
    }

    @Test
    public void testParseAsNumber()
                            throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream( "insertResponse.xml" );
        Document doc = docBuilder.parse( resourceAsStream );
        String xpath = "//csw:TransactionResponse/csw:TransactionSummary/csw:totalInserted";
        int totalInserted = XMLUtils.parseAsInteger( doc, xpath );
        assertThat( totalInserted, is( 1 ) );
    }

}
