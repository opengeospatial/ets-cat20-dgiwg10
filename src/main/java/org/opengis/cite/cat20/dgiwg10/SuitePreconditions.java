package org.opengis.cite.cat20.dgiwg10;

import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertQualifiedName;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opengis.cite.cat20.dgiwg10.util.DataSampler;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Checks that various preconditions are satisfied before the test suite is run. If any of these (BeforeSuite) methods
 * fail, all tests will be skipped.
 */
public class SuitePreconditions {

    private static final Logger LOGR = Logger.getLogger( SuitePreconditions.class.getName() );

    /**
     * Verifies that the referenced test subject exists and has the expected type.
     *
     * @param testContext
     *            the test context, never <code>null</code>
     */
    @BeforeSuite
    public void verifyTestSubject( ITestContext testContext ) {
        SuiteAttribute testFileAttr = SuiteAttribute.TEST_SUBJ_FILE;
        Object sutObj = testContext.getSuite().getAttribute( testFileAttr.getName() );
        Class expectedType = testFileAttr.getType();
        if ( sutObj == null || !expectedType.isInstance( sutObj ) ) {
            String msg = String.format( "Value of test suite attribute '%s' is missing or is not an instance of %s",
                                        testFileAttr.getName(), expectedType.getName() );
            LOGR.log( Level.SEVERE, msg );
            throw new AssertionError( msg );
        }
    }

    /**
     * Verifies that the test subject is a CSW 2.0 service. The document element in the supplied metadata resource must
     * be "{http://www.opengis.net/cat/csw/2.0.2}Capabilities".
     *
     * @param testContext
     *            the test run context, never <code>null</code>
     */
    @Test(description = "Test subject is WFS 2.0 service")
    public void verifyCapabilities( ITestContext testContext ) {
        Document capabilitiesDocument = (Document) testContext.getSuite().getAttribute( SuiteAttribute.TEST_SUBJECT.getName() );
        assertQualifiedName( capabilitiesDocument, CSW, "Capabilities" );
    }

    /**
     * Confirms that the service can supply test data.
     *
     * @param testContext
     *            the test context, never <code>null</code>
     */
    @Test(description = "SUT has data", dependsOnMethods = "verifyCapabilities")
    public void dataAreAvailable( ITestContext testContext ) {
        ISuite suite = testContext.getSuite();
        Document capabilitiesDocument = (Document) suite.getAttribute( SuiteAttribute.TEST_SUBJECT.getName() );
        DataSampler sampler = new DataSampler( capabilitiesDocument );
        suite.setAttribute( SuiteAttribute.DATA_SAMPLER.getName(), sampler );
        try {
            sampler.acquireRecords();
        } catch ( RuntimeException rx ) {
            StringBuilder msg = new StringBuilder( "Failed to acquire feature data (" );
            msg.append( rx.getClass().getName() ).append( "): " );
            msg.append( rx.getMessage() );
            throw new AssertionError( msg.toString() );
        }
        Map<String, Node> records = sampler.getRecords();
        boolean sutHasData = !records.isEmpty();
        if ( !sutHasData ) {
            String msg = "No test data available. Service must provide at least one record for testing";
            LOGR.warning( msg );
            throw new AssertionError( msg );
        }
    }

}
