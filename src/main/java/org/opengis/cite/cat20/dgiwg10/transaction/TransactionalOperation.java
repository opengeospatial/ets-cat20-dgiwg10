package org.opengis.cite.cat20.dgiwg10.transaction;

import static org.opengis.cite.cat20.dgiwg10.Namespaces.XSD;
import static org.opengis.cite.cat20.dgiwg10.util.ValidationUtils.createSchemaResolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public abstract class TransactionalOperation extends CommonFixture {

    protected final RequestCreator requestCreator = new RequestCreator();

    protected URI transactionUrl;

    protected Validator cswValidator;

    protected String id;

    protected final static String TRANSACTION_USERNAME = System.getProperty( "trn.user" );

    protected final static String TRANSACTION_PASSWORD = System.getProperty( "trn.pw" );

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

    @BeforeClass
    public void setTransactionPostUrl() {
        transactionUrl = ServiceMetadataUtils.getOperationEndpoint( capabilitiesDoc, getOperationName(),
                                                                    ProtocolBinding.POST );
    }

    @AfterClass
    public void removeInsertedRecords() {
        if ( id != null ) {
            this.requestDocument = requestCreator.createDeleteRequest( this.id );
            this.response = this.cswClient.submitPostRequest( transactionUrl, this.requestDocument,
                                                              TRANSACTION_USERNAME, TRANSACTION_PASSWORD );
        }
    }

    abstract String getOperationName();

    protected String parseIdentifier() {
        try {
            String xpath = "//csw:InsertResult/csw:BriefRecord/dc:identifier";
            return (String) XMLUtils.evaluateXPath( responseDocument, xpath, null, XPathConstants.STRING );
        } catch ( XPathExpressionException e ) {
            // XPath is correct
        }
        return null;
    }

    protected int parseTotalInserted() {
        return parseAsInteger( "//csw:TransactionResponse/csw:TransactionSummary/csw:totalInserted" );
    }

    protected int parseTotalUpdated() {
        return parseAsInteger( "//csw:TransactionResponse/csw:TransactionSummary/csw:totalUpdated" );
    }

    protected int parseTotalDeleted() {
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
