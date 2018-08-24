package org.opengis.cite.cat20.dgiwg10.getcapabilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetCapabilitiesTest {

    private static ITestContext testContext;

    private static ISuite suite;

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUpClass()
                            throws Exception {
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void testVerifyMetadataLink()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-DGIWG-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifyMetadataLink();
    }

    @Test(expected = AssertionError.class)
    public void testVerifyMetadataLink_invalid()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifyMetadataLink();
    }

    @Test
    public void testVerifySupportGetRecordsReturnType()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-DGIWG-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifySupportGetRecordsReturnType();
    }

    @Test(expected = AssertionError.class)
    public void testVerifySupportGetRecordsReturnType_invalid()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifySupportGetRecordsReturnType();
    }

    @Test
    public void testVerifyGetRecordsQueryables()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-DGIWG-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifyGetRecordsQueryables();
    }

    @Test(expected = AssertionError.class)
    public void testVerifyGetRecordsQueryables_invalid()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifyGetRecordsQueryables();
    }

    @Test
    public void testVerifySupportGetRecordByIdReturnType()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-DGIWG-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifySupportGetRecordByIdReturnType();
    }

    @Test(expected = AssertionError.class)
    public void testVerifySupportGetRecordByIdReturnType_invalid()
                            throws Exception {
        Document capabilitiesDocument = parseCapabilitiesDocument( "GetCapabilities-response.xml" );

        GetCapabilities getCapabilities = new GetCapabilities();
        getCapabilities.setResponseDocument( capabilitiesDocument );
        getCapabilities.verifySupportGetRecordByIdReturnType();
    }

    private Document parseCapabilitiesDocument( String resource )
                            throws SAXException, IOException {
        InputStream is = getClass().getResourceAsStream( resource );
        return docBuilder.parse( is );
    }

}