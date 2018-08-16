package org.opengis.cite.cat20.dgiwg10.getrecordbyid;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.opengis.cite.cat20.dgiwg10.getrecords.GetRecordsTest;
import org.opengis.cite.cat20.dgiwg10.util.DataSampler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetRecordByIdTest {

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

        InputStream docAsStream = GetRecordsTest.class.getResourceAsStream( "../getcapabilities/GetCapabilities-DGIWG-response.xml" );
        Document capabilitiesDoc = docBuilder.parse( docAsStream );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( capabilitiesDoc );

        DataSampler dataSampler = mock( DataSampler.class );
        when( dataSampler.findSampleIdentifier() ).thenReturn( "ok" );
        when( suite.getAttribute( SuiteAttribute.DATA_SAMPLER.getName() ) ).thenReturn( dataSampler );
    }

    @Before
    public void setUp() {
        initJadlerListeningOn( 8090 );
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Test
    public void testGetRecordById_Dc()
                            throws XPathExpressionException {
        prepareJadler( "dublinCore-response.xml" );

        GetRecordById getRecordById = new GetRecordById();
        getRecordById.initCommonFixture( testContext );
        getRecordById.retrieveDataSampler( testContext );
        getRecordById.buildValidators();

        getRecordById.issueGetRecordById_DublinCore();
        getRecordById.issueGetRecordById_Returnables_DublinCore();
    }

    @Test
    public void testGetRecordById_Iso()
                            throws XPathExpressionException {
        prepareJadler( "iso-response.xml" );

        GetRecordById getRecordById = new GetRecordById();
        getRecordById.initCommonFixture( testContext );
        getRecordById.retrieveDataSampler( testContext );
        getRecordById.buildValidators();

        getRecordById.issueGetRecordById_Iso();
        getRecordById.issueGetRecordById_Returnables_Iso();
    }

    private void prepareJadler( String resource ) {
        InputStream responseEntity = getClass().getResourceAsStream( resource );
        onRequest().respond().withBody( responseEntity );
    }

}
