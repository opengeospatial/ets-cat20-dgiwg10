package org.opengis.cite.cat20.dgiwg10.getrecordbyid;

import static javax.xml.xpath.XPathConstants.NODE;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETRECORDS;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertSchemaValid;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXPath;
import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.UNEXPECTED_STATUS;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.XSD;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.POST;
import static org.opengis.cite.cat20.dgiwg10.returnables.Returnables.assertReturnablesDublinCore;
import static org.opengis.cite.cat20.dgiwg10.returnables.Returnables.assertReturnablesIso;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.ISO19193;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;
import static org.opengis.cite.cat20.dgiwg10.util.ValidationUtils.createSchemaResolver;
import static org.opengis.cite.cat20.dgiwg10.util.XMLUtils.evaluateXPath;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.cat20.dgiwg10.CommonFixture;
import org.opengis.cite.cat20.dgiwg10.ErrorMessage;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.opengis.cite.cat20.dgiwg10.util.DataSampler;
import org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings;
import org.opengis.cite.cat20.dgiwg10.util.TestSuiteLogger;
import org.opengis.cite.cat20.dgiwg10.util.ValidationUtils;
import org.opengis.cite.cat20.dgiwg10.xml.RequestCreator;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetRecordById extends CommonFixture {

    private final RequestCreator requestCreator = new RequestCreator();

    private DataSampler dataSampler;

    private Validator cswValidator;

    private Validator isoValidator;

    private Document dublinCoreResponse;

    private Document isoResponse;

    /**
     * @param testContext
     *            the test context
     */
    @BeforeClass
    public void retrieveDataSampler( ITestContext testContext ) {
        this.dataSampler = (DataSampler) testContext.getSuite().getAttribute( SuiteAttribute.DATA_SAMPLER.getName() );
    }

    @BeforeClass
    public void buildValidators() {
        URL cswSchemaUrl = getClass().getResource( "/org/opengis/cite/cat20/dgiwg10/xsd/csw/2.0.2/csw.xsd" );
        try {
            Schema cswSchema = ValidationUtils.createSchema( cswSchemaUrl.toURI() );
            this.cswValidator = cswSchema.newValidator();
            this.cswValidator.setResourceResolver( createSchemaResolver( XSD ) );
        } catch ( URISyntaxException e ) {
            // very unlikely to occur with no schema to process
            TestSuiteLogger.log( Level.WARNING, "Failed to build XML Schema Validator for csw.xsd.", e );
        }

        try {
            URL metadatEntitySchemaUrl = getClass().getResource( "/org/opengis/cite/cat20/dgiwg10/xsd/iso/19139/20070417/gmd/metadataEntity.xsd" );
            URL srvSchemaUrl = getClass().getResource( "/org/opengis/cite/cat20/dgiwg10/xsd/iso/19139/20070417/srv/1.0/serviceMetadata.xsd" );
            Schema schema = ValidationUtils.createSchema( metadatEntitySchemaUrl.toURI(), srvSchemaUrl.toURI(),
                                                          cswSchemaUrl.toURI() );
            this.isoValidator = schema.newValidator();
            this.isoValidator.setResourceResolver( createSchemaResolver( XSD ) );
        } catch ( URISyntaxException e ) {
            // very unlikely to occur with no schema to process
            TestSuiteLogger.log( Level.WARNING, "Failed to build XML Schema Validator for csw.xsd.", e );
        }
    }

    /**
     * Issue an HTTP GetRecordById request with csw:Record.
     */
    @Test(description = "Implements A.1.3 GetRecordById for DGIWG Basic CSW - 'csw:Record' (Requirement 10)")
    public void issueGetRecordById_DublinCore() {
        URI endpoint = getOperationEndpoint( this.capabilitiesDoc, GETRECORDS, POST );
        if ( endpoint == null )
            throw new SkipException( "No POST binding available for GetRecords request." );

        String identifier = dataSampler.findSampleIdentifier();
        if ( identifier == null )
            throw new SkipException( "No identifier available." );

        this.requestDocument = requestCreator.createGetRecordById( ISO19193, FULL, identifier );
        this.response = this.cswClient.submitPostRequest( endpoint, this.requestDocument );
        assertEquals( this.response.getStatus(), 200, ErrorMessage.format( UNEXPECTED_STATUS ) );

        this.responseDocument = this.response.getEntity( Document.class );
        assertXPath( "//csw:GetRecordByIdResponse", this.responseDocument,
                     NamespaceBindings.withStandardBindings().getAllBindings(),
                     "Response is not a GetRecordByIdResponse" );
        assertSchemaValid( this.cswValidator, new DOMSource( this.responseDocument ) );

        this.dublinCoreResponse = this.responseDocument;
    }

    /**
     * Issue an HTTP GetRecordById request with gmd:MD_Metadata.
     */
    @Test(description = "Implements A.1.3 GetRecordById for DGIWG Basic CSW - 'gmd:MD_Metadata' (Requirement 10)")
    public void issueGetRecordById_Iso() {
        URI endpoint = getOperationEndpoint( this.capabilitiesDoc, GETRECORDS, POST );
        if ( endpoint == null )
            throw new SkipException( "No POST binding available for GetRecords request." );

        String identifier = dataSampler.findSampleIdentifier();
        if ( identifier == null )
            throw new SkipException( "No identifier available." );

        this.requestDocument = requestCreator.createGetRecordById( ISO19193, FULL, identifier );
        this.response = this.cswClient.submitPostRequest( endpoint, this.requestDocument );
        assertEquals( this.response.getStatus(), 200, ErrorMessage.format( UNEXPECTED_STATUS ) );

        this.responseDocument = this.response.getEntity( Document.class );
        assertXPath( "//csw:GetRecordByIdResponse", this.responseDocument,
                     NamespaceBindings.withStandardBindings().getAllBindings(),
                     "Response is not a GetRecordByIdResponse" );
        assertSchemaValid( this.isoValidator, new DOMSource( this.responseDocument ) );

        this.isoResponse = this.responseDocument;
    }

    /**
     * Verify that all metadata returnables are present in the result (csw:Record).
     */
    @Test(description = "Implements A.1.3 GetRecordById for DGIWG Basic CSW - 'csw:Record', returnables (Requirement 13)", dependsOnMethods = "issueGetRecordById_DublinCore")
    public void issueGetRecordById_Returnables_DublinCore()
                            throws XPathExpressionException {
        Node record = (Node) evaluateXPath( this.dublinCoreResponse, "//csw:Record[1]", null, NODE );
        if ( record == null )
            throw new AssertionError( "No csw:Record record available" );
        assertReturnablesDublinCore( record );
    }

    /**
     * Verify that all metadata returnables are present in the result (gmd:MD_Metadata).
     */
    @Test(description = "Implements A.1.3 GetRecordById for DGIWG Basic CSW - 'gmd:MD_Metadata', returnables (Requirement 13)", dependsOnMethods = "issueGetRecordById_Iso")
    public void issueGetRecordById_Returnables_Iso()
                            throws XPathExpressionException {
        Node record = (Node) evaluateXPath( this.isoResponse, "//gmd:MD_Metadata[1]", null, NODE );
        if ( record == null )
            throw new AssertionError( "No gmd:MD_Metadata record available" );
        assertReturnablesIso( record );
    }

}
