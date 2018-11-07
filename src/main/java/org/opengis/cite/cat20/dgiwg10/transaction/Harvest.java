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
import static org.testng.Assert.assertNotNull;

import javax.xml.transform.dom.DOMSource;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Harvest extends TransactionalOperation {

    private static final String RECORD_TO_HARVEST = "https://gist.githubusercontent.com/lgoltz/57f0c19715f47fd9163f20261a2b1342/raw/6642528cc920277f9d731f139ec5a2e137651cb9/DMFMetadataRecord.xml";

    @Override
    String getOperationName() {
        return "Harvest";
    }

    @Test(description = "Precondition of A.1.4 DGIWG Transactional CSW - Harvest Request (Harvest DCP URL must be available)", dependsOnGroups = "isTransactional")
    public void supportsHarvesting() {
        assertNotNull( transactionUrl, "DCP URL for Operation 'Harvest' is not available" );
    }

    /**
     * Issue a HTTP POST Harvest Request and confirm that all records are returned (Requirement 18).
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Harvest Request (Requirement 20)", dependsOnMethods = "supportsHarvesting")
    public void issueHarvestRequest() {
        this.requestDocument = requestCreator.createHarvest( RECORD_TO_HARVEST );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        assertQualifiedName( responseDocument, CSW, "HarvestResponse" );
        assertSchemaValid( cswValidator, new DOMSource( this.responseDocument ) );

        int totalInserted = parseTotalInserted();
        assertTrue( totalInserted == 1, "Expected totalInserted 1 but was " + totalInserted );

        this.id = parseIdentifier();
        assertTrue( this.id != null, "Response does not contain the identifier of the harvested record." );
    }

    @Test(description = "Implements A.1.4 DGIWG Transactional CSW - Ensure Harvest (Requirement 20)", dependsOnMethods = "issueHarvestRequest")
    public void issueGetRecordById_EnsureHarvest() {
        this.requestDocument = requestCreator.createGetRecordById( DC, FULL, this.id );
        this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument, TRANSACTION_USERNAME,
                                                          TRANSACTION_PASSWORD );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );
        this.responseDocument = this.response.getEntity( Document.class );

        String xpath = String.format( "//csw:Record[dc:identifier = '%s']", this.id );
        assertXPath( this.responseDocument, xpath );
    }

}
