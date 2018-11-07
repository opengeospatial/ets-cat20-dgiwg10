package org.opengis.cite.cat20.dgiwg10.transaction;

import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertQualifiedName;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertSchemaValid;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertStatusCode;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertTrue;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXPath;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXmlContentType;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.XSD;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.ValidationUtils.createSchemaResolver;
import static org.opengis.cite.cat20.dgiwg10.util.XMLUtils.evaluateXPath;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.cat20.dgiwg10.CommonFixture;
import org.opengis.cite.cat20.dgiwg10.ProtocolBinding;
import org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils;
import org.opengis.cite.cat20.dgiwg10.util.TestSuiteLogger;
import org.opengis.cite.cat20.dgiwg10.util.ValidationUtils;
import org.opengis.cite.cat20.dgiwg10.util.XMLUtils;
import org.opengis.cite.cat20.dgiwg10.xml.RequestCreator;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Transaction extends CommonFixture {

    private static final String CSWT_IDENTIFIER = "http://www.dgiwg.org/std/csw/1.0/conf/dgiwg_cswt";

    private static final String ABSTRACT_TEXT_19 = "This service implements the DGIWG Catalogue Service for the Web ISO Profile version 1.0, DGIWG Basic CSW conformance class (http://www.dgiwg.org/std/csw/1.0/conf/basic) and DGIWG Transactional CSW (”http://www.dgiwg.org/std/csw/1.0/conf/dgiwg_cswt)";

    private final RequestCreator requestCreator = new RequestCreator();

    private URI transactionUrl;

    private Validator cswValidator;

    private String insertedId;

    private final static String TRANSACTION_USERNAME = System.getProperty( "trn.user" );

    private final static String TRANSACTION_PASSWORD = System.getProperty( "trn.pw" );

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
    }

    @AfterClass
    public void removeInsertedRecords() {
        if ( insertedId != null ) {
            this.requestDocument = requestCreator.createDeleteRequest( this.insertedId );
            this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument,
                                                              TRANSACTION_USERNAME, TRANSACTION_PASSWORD );
        }
    }

    /**
     * Verify that the Abstract present in the service metadata includes basic identifier.
     *
     * @throws XPathExpressionException
     *             should never happen
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW")
    public void isTransactionalCsw()
                            throws XPathExpressionException {
        String xpath = "contains(//csw:Capabilities/ows:ServiceIdentification/ows:Abstract, '%s')";
        String xpathWithIdentifier = String.format( xpath, CSWT_IDENTIFIER );
        boolean isCswT = (boolean) evaluateXPath( capabilitiesDoc, xpathWithIdentifier,
                                                  withStandardBindings().getAllBindings(), XPathConstants.BOOLEAN );
        if ( !isCswT )
            throw new SkipException( "CSW is not transactional (missing identifier '" + CSWT_IDENTIFIER
                                     + "' in the abstract of the capabilities)." );
    }

    /**
     * Verify that the service provides a POST URL for Transaction requests.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW")
    public void hasTransactionPostUrl() {
        transactionUrl = ServiceMetadataUtils.getOperationEndpoint( capabilitiesDoc, "Transaction",
                                                                    ProtocolBinding.POST );
        if ( transactionUrl == null )
            throw new SkipException( "CSW does not provide a POST Url for Transaction requests." );
    }

    /**
     * Verify that the Abstract present in the service metadata includes the text defined in requirement 19.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW (Requirement 19)", dependsOnMethods = "isTransactionalCsw")
    public void verifyAbstract() {
        String xpath = "contains(normalize-space(//csw:Capabilities/ows:ServiceIdentification/ows:Abstract), '"
                       + ABSTRACT_TEXT_19 + "')";
        assertXPath( capabilitiesDoc, xpath, withStandardBindings().getAllBindings(),
                     "Abstract does not contain the expected text '" + ABSTRACT_TEXT_19 + "'." );
    }

    /**
     * Issue a HTTP POST CSW INSERT operation with DMF Compliant Metadata.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Insert (Requirement 18)", dependsOnMethods = {
                                                                                                                  "isTransactionalCsw",
                                                                                                                  "hasTransactionPostUrl" })
    public void issueInsertOperation() {
        this.requestDocument = requestCreator.createInsertRequest();
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        assertQualifiedName( responseDocument, CSW, "TransactionResponse" );
        assertSchemaValid( cswValidator, new DOMSource( this.responseDocument ) );

        int totalInserted = parseTotalInserted();
        assertTrue( totalInserted == 1, "Expected totalInserted 1 but was " + totalInserted );

        this.insertedId = parseIdentifier();
        assertTrue( this.insertedId != null, "Response does not contain the identifier of the inserted record." );
    }

    /**
     * Issue a HTTP POST GetRecords Request to confirm the item was inserted.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Insert (Requirement 18)", dependsOnMethods = "issueInsertOperation")
    public void issueGetRecords_EnsureInsert() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.insertedId );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "//csw:Record[dc:identifier = '%s']", this.insertedId );
        assertXPath( this.responseDocument, xpath );
    }

    /**
     * Issue a HTTP POST Update Operation with a revised DMF Compliant metadata file.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Update (Requirement 18)", dependsOnMethods = "issueGetRecords_EnsureInsert")
    public void issueUpdateOperation() {
        this.requestDocument = requestCreator.createUpdateRequest();
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        assertQualifiedName( responseDocument, CSW, "TransactionResponse" );
        assertSchemaValid( cswValidator, new DOMSource( this.responseDocument ) );

        int totalUpdated = parseTotalUpdated();
        assertTrue( totalUpdated == 1, "Expected totalUpdated 1 but was " + totalUpdated );
    }

    /**
     * Issue a HTTP POST GetRecords Request to confirm the item was updated (Requirement 18).
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Update (Requirement 18)", dependsOnMethods = "issueUpdateOperation")
    public void issueGetRecords_EnsureUpdate() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.insertedId );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "//csw:Record[dc:identifier = '%s']", this.insertedId );
        assertXPath( this.responseDocument, xpath );

        // TODO: ensure update
    }

    /**
     * Issue a HTTP POST Delete Operation with a revised DMF Compliant metadata file.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Update (Requirement 18)", dependsOnMethods = "issueGetRecords_EnsureUpdate")
    public void issueDeleteOperation() {
        this.requestDocument = requestCreator.createDeleteRequest( this.insertedId );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        assertQualifiedName( responseDocument, CSW, "TransactionResponse" );
        assertSchemaValid( cswValidator, new DOMSource( this.responseDocument ) );

        int totalDeleted = parseTotalDeleted();
        assertTrue( totalDeleted == 1, "Expected totalDeleted 1 but was " + totalDeleted );
        this.insertedId = null;
    }

    /**
     * Issue a HTTP POST GetRecords Request to confirm the item was deleted. (Requirement 18).
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Update (Requirement 18)", dependsOnMethods = "issueDeleteOperation")
    public void issueGetRecords_EnsureDelete() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.insertedId );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "not(//csw:Record[dc:identifier = '%s'])", this.insertedId );
        assertXPath( this.responseDocument, xpath );
    }

    private String parseIdentifier() {
        try {
            String xpath = "//csw:InsertResult/csw:BriefRecord/dc:identifier";
            return (String) XMLUtils.evaluateXPath( responseDocument, xpath, null, XPathConstants.STRING );
        } catch ( XPathExpressionException e ) {
            // XPath is correct
        }
        return null;
    }

    private int parseTotalInserted() {
        return parseAsInteger( "//csw:TransactionResponse/csw:TransactionSummary/csw:totalInserted" );
    }

    private int parseTotalUpdated() {
        return parseAsInteger( "//csw:TransactionResponse/csw:TransactionSummary/csw:totalUpdated" );
    }

    private int parseTotalDeleted() {
        return parseAsInteger( "//csw:TransactionResponse/csw:TransactionSummary/csw:totalDeleted" );
    }

    private int parseAsInteger( String s ) {
        try {
            return XMLUtils.parseAsInteger( responseDocument, s );
        } catch ( XPathExpressionException e ) {
            return -1;
        }
    }

}
