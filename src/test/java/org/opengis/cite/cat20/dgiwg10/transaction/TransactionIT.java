package org.opengis.cite.cat20.dgiwg10.transaction;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
@Ignore
public class TransactionIT {

    private static final String SERVICE_UNDER_TEST = "http://dgiwg.geo-solutions.it/geonetwork/srv/eng/csw?REQUEST=GetCapabilities&SERVICE=CSW";

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

        InputStream docAsStream = new URL( SERVICE_UNDER_TEST ).openStream();
        Document capabilitiesDoc = docBuilder.parse( docAsStream );
        when( suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() ) ).thenReturn( capabilitiesDoc );
    }

    @Test
    public void testTransaction_noError()
                            throws Exception {
        Transaction transaction = new Transaction();
        transaction.initCommonFixture( testContext );

        transaction.isTransactionalCsw();
        transaction.hasTransactionPostUrl();
        transaction.verifyAbstract();
        transaction.issueInsertOperation();
    }

}