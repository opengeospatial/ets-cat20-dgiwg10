package org.opengis.cite.cat20.dgiwg10.getrecords;

import static net.jadler.Jadler.closeJadler;
import static net.jadler.Jadler.initJadlerListeningOn;
import static net.jadler.Jadler.onRequest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.ISO19193;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.opengis.cite.cat20.dgiwg10.xml.FilterCreator;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetRecordsTest {

    private final FilterCreator filterCreator = new FilterCreator();

    private GetRecords getRecords;

    private static ITestContext testContext;

    private static ISuite suite;

    @BeforeClass
    public static void setUpClass()
                            throws Exception {
        testContext = mock( ITestContext.class );
        suite = mock( ISuite.class );
        when( testContext.getSuite() ).thenReturn( suite );
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();

        InputStream docAsStream = GetRecordsTest.class.getResourceAsStream( "../getcapabilities/GetCapabilities-DGIWG-response.xml" );
        Document capabilitiesDoc = docBuilder.parse( docAsStream );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( capabilitiesDoc );
    }

    @Before
    public void setUp() {
        initJadlerListeningOn( 8080 );
    }

    @After
    public void tearDown() {
        closeJadler();
    }

    @Before
    public void initGetRecords() {
        this.getRecords = new GetRecords();
        this.getRecords.initCommonFixture( testContext );
        this.getRecords.buildValidators();
    }

    @Test
    public void testGetRecords_DublinCore()
                            throws XPathExpressionException {
        prepareJadler( "dublinCore-response.xml" );

        String queryable = "Identifier";
        Element filter = filterCreator.createIdentifierFilter( DC, "{8A1F1B62-5424-44EA-BE46-6BC3B073CDB4}" );
        getRecords.issueGetRecords_DublinCore( queryable, filter );
        getRecords.issueGetRecords_Returnables_DublinCore( queryable );
    }

    @Test
    public void testGetRecords_Iso()
                            throws XPathExpressionException {
        prepareJadler( "iso-response.xml" );

        String queryable = "Identifier";
        Element filter = filterCreator.createIdentifierFilter( ISO19193, "{8A1F1B62-5424-44EA-BE46-6BC3B073CDB4}" );
        getRecords.issueGetRecords_Iso( queryable, filter );
        getRecords.issueGetRecords_Returnables_Iso( queryable );
    }

    private void prepareJadler( String s ) {
        InputStream responseEntity = getClass().getResourceAsStream( s );
        onRequest().respond().withStatus( 200 ).withBody( responseEntity );
    }

}
