package org.opengis.cite.cat20.dgiwg10.getrecords;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.opengis.cite.cat20.dgiwg10.util.DataSampler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetRecordsIT {

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

        InputStream docAsStream = new URL( "http://gptogc.esri.com/geoportal/csw?request=GetCapabilities&service=CSW&AcceptVersions=2.0.2" ).openStream();
        //InputStream docAsStream = GetRecordsIT.class.getResourceAsStream( "GetCapabilities-response.xml" );
        Document capabilitiesDoc = docBuilder.parse( docAsStream );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( capabilitiesDoc );

        DataSampler dataSampler = new DataSampler( capabilitiesDoc );
        dataSampler.acquireRecords();
        when( suite.getAttribute( SuiteAttribute.DATA_SAMPLER.getName() ) ).thenReturn( dataSampler );
    }

    @Test
    public void testGetRecords() {
        GetRecords getRecords = new GetRecords();
        getRecords.initCommonFixture( testContext );
        getRecords.retrieveDataSampler( testContext );
        getRecords.buildValidators();
        //getRecords.issueGetCapabilities_identifier_DublinCore();
        getRecords.issueGetCapabilities_identifier_Iso();
        // service does not support gmd:MD_Metadata getRecords.issueGetCapabilities_identifier_Iso();
    }

}
