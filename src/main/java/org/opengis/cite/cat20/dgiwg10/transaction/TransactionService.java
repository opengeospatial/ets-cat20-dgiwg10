package org.opengis.cite.cat20.dgiwg10.transaction;

import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertQualifiedName;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertSchemaValid;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertStatusCode;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertTrue;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXPath;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXmlContentType;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.xml.RequestCreator.RECORDTYPE.SERVICE;

import javax.xml.transform.dom.DOMSource;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

/**
 * Implements A.1.5 DGIWG Transactional CSW
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TransactionService extends TransactionalOperation {

    @Override
    String getOperationName() {
        return "Transaction";
    }

    /**
     * Issue a HTTP POST CSW INSERT operation with DMF Compliant Metadata.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Insert (Requirement 17)", dependsOnGroups = "isTransactional")
    public void issueInsertOperation_Service() {
        this.requestDocument = requestCreator.createInsertRequest( SERVICE );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        assertQualifiedName( responseDocument, CSW, "TransactionResponse" );
        assertSchemaValid( cswValidator, new DOMSource( this.responseDocument ) );

        int totalInserted = parseTotalInserted();
        assertTrue( totalInserted == 1, "Expected totalInserted 1 but was " + totalInserted );

        this.id = parseIdentifier();
        assertTrue( this.id != null, "Response does not contain the identifier of the inserted record." );
    }

    /**
     * Issue a HTTP POST GetRecords Request to confirm the item was inserted (Requirement 17).
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Insert (Requirement 17)", dependsOnMethods = "issueInsertOperation_Service")
    public void issueGetRecords_EnsureInsert_Service() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.id );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "//csw:Record[dc:identifier = '%s']", this.id );
        assertXPath( this.responseDocument, xpath );
    }

    /**
     * Issue a HTTP POST Update Operation with a revised DMF Compliant metadata file.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Update (Requirement 17)", dependsOnMethods = "issueGetRecords_EnsureInsert_Service")
    public void issueUpdateOperation_Service() {
        this.requestDocument = requestCreator.createUpdateRequest( SERVICE );
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
     * Issue a HTTP POST GetRecords Request to confirm the item was updated (Requirement 17).
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Update (Requirement 18)", dependsOnMethods = "issueUpdateOperation_Service")
    public void issueGetRecords_EnsureUpdate_Service() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.id );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "//csw:Record[dc:identifier = '%s']", this.id );
        assertXPath( this.responseDocument, xpath );

        // TODO: ensure update
    }

    /**
     * Issue a HTTP POST Delete Operation with a revised DMF Compliant metadata file.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Update (Requirement 17)", dependsOnMethods = "issueGetRecords_EnsureUpdate_Service")
    public void issueDeleteOperation_Service() {
        this.requestDocument = requestCreator.createDeleteRequest( this.id );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        assertQualifiedName( responseDocument, CSW, "TransactionResponse" );
        assertSchemaValid( cswValidator, new DOMSource( this.responseDocument ) );

        int totalDeleted = parseTotalDeleted();
        assertTrue( totalDeleted == 1, "Expected totalDeleted 1 but was " + totalDeleted );
        this.id = null;
    }

    /**
     * Issue a HTTP POST GetRecords Request to confirm the item was deleted. (Requirement 17).
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Update (Requirement 17)", dependsOnMethods = "issueDeleteOperation_Service")
    public void issueGetRecords_EnsureDelete_Service() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.id );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "not(//csw:Record[dc:identifier = '%s'])", this.id );
        assertXPath( this.responseDocument, xpath );
    }

}
