package org.opengis.cite.cat20.dgiwg10.getrecords;

import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETRECORDS;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertSchemaValid;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXPath;
import static org.opengis.cite.cat20.dgiwg10.ErrorMessageKeys.UNEXPECTED_STATUS;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.XSD;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.POST;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.ISO19193;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;
import static org.opengis.cite.cat20.dgiwg10.util.ValidationUtils.createSchemaResolver;
import static org.testng.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.opengis.cite.cat20.dgiwg10.CommonFixture;
import org.opengis.cite.cat20.dgiwg10.ErrorMessage;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.opengis.cite.cat20.dgiwg10.util.DataSampler;
import org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings;
import org.opengis.cite.cat20.dgiwg10.util.TestSuiteLogger;
import org.opengis.cite.cat20.dgiwg10.util.ValidationUtils;
import org.opengis.cite.cat20.dgiwg10.xml.FilterCreator;
import org.opengis.cite.cat20.dgiwg10.xml.RequestCreator;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * A.1.2 GetRecord for DGIWG Basic CSW
 *
 * a) Test Purpose: Verify that the server implements DGIWG the following DGIWG requirements (Requirement 6, Requirement
 * 7, Requirement 8, Requirement 9)
 *
 * b) Test Method:
 *
 * - Ensure that the CSW is loaded with metadata which supports all of the queriables and returnables described in table
 * 5, 6, 8, 9 and 10.
 *
 * - Issue a number of HTTP POST GetRecords requests with return types of both csw:Record and gmd:MD_Metadata using all
 * of the queriables in Tables 5 and 6. Verify that a valid result is obtained (Requirements 6, 7).
 *
 * - Verify that all metadata returnables are present in the result (Requirement 8)
 *
 * - Verify that the gmd:MD_Metadata record returned is compliant with the DMF specification (Requirement 11) and with
 * ISO19139 (Requirement 12). Returnables shall be mapped to the DMF using tables 5,6,8,9 and 10 (Requirement 8) and
 * additional items in table 9 (Requirement 14).
 *
 * c) References: Sections 7.3, 7.4, 7.5, 7.6.2, 7.7.1, 7.7.3,
 *
 * d) Test Type: Capability
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetRecords extends CommonFixture {

    private final RequestCreator requestCreator = new RequestCreator();

    private final FilterCreator filterCreator = new FilterCreator();

    private DataSampler dataSampler;

    private Validator cswValidator;

    private Validator isoValidator;

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
     * Issue an HTTP GET capabilities request with csw:Record and Queryable 'Identifier'.
     */
    @Test(description = "Implements A.1.2 GetRecord for DGIWG Basic CSW - 'csw:Record'/'Identifier'")
    public void issueGetCapabilities_identifier_DublinCore() {
        URI endpoint = getOperationEndpoint( this.capabilitiesDoc, GETRECORDS, POST );
        if ( endpoint == null )
            throw new SkipException( "No POST binding available for GetRecords request." );

        String identifier = findIdentifier();
        if ( identifier == null )
            throw new SkipException( "No identifier for available." );
        Element identifierFilter = filterCreator.createIdentifierFilter( DC, identifier );
        this.requestDocument = requestCreator.createGetRecordsRequest( DC, FULL, identifierFilter );

        this.response = this.cswClient.submitPostRequest( endpoint, this.requestDocument );
        assertEquals( this.response.getStatus(), 200, ErrorMessage.format( UNEXPECTED_STATUS ) );

        this.responseDocument = this.response.getEntity( Document.class );
        assertXPath( "//csw:GetRecordsResponse", this.responseDocument,
                     NamespaceBindings.withStandardBindings().getAllBindings(), "Response is not a GetRecordsResponse" );
        assertSchemaValid( this.cswValidator, new DOMSource( this.responseDocument ) );
    }

    /**
     * Issue an HTTP GET capabilities request with gmd:MD_Metadata and Queryable 'Identifier'.
     */
    @Test(description = "Implements A.1.2 GetRecord for DGIWG Basic CSW - 'gmd:MD_Metadata'/'Identifier'")
    public void issueGetCapabilities_identifier_Iso() {
        URI endpoint = getOperationEndpoint( this.capabilitiesDoc, GETRECORDS, POST );
        if ( endpoint == null )
            throw new SkipException( "No POST binding available for GetRecords request." );

        String identifier = findIdentifier();
        if ( identifier == null )
            throw new SkipException( "No identifier for available." );
        Element identifierFilter = filterCreator.createIdentifierFilter( ISO19193, identifier );
        this.requestDocument = requestCreator.createGetRecordsRequest( ISO19193, FULL, identifierFilter );

        this.response = this.cswClient.submitPostRequest( endpoint, this.requestDocument );
        assertEquals( this.response.getStatus(), 200, ErrorMessage.format( UNEXPECTED_STATUS ) );

        this.responseDocument = this.response.getEntity( Document.class );
        assertXPath( "//csw:GetRecordsResponse", this.responseDocument,
                     NamespaceBindings.withStandardBindings().getAllBindings(), "Response is not a GetRecordsResponse" );
        assertSchemaValid( this.isoValidator, new DOMSource( this.responseDocument ) );
    }

    private String findIdentifier() {
        Map<String, Node> records = dataSampler.getRecords();
        for ( String key : records.keySet() ) {
            if ( key != null )
                return key;
        }
        return null;
    }

}