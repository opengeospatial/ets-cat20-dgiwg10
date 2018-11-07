package org.opengis.cite.cat20.dgiwg10.getrecords;

import static javax.xml.xpath.XPathConstants.NODE;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETRECORDS;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertQualifiedName;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertSchemaValid;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertStatusCode;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXmlContentType;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.XSD;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.POST;
import static org.opengis.cite.cat20.dgiwg10.returnables.Returnables.assertReturnablesDublinCore;
import static org.opengis.cite.cat20.dgiwg10.returnables.Returnables.assertReturnablesIso;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.ISO19193;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;
import static org.opengis.cite.cat20.dgiwg10.util.ValidationUtils.createSchemaResolver;
import static org.opengis.cite.cat20.dgiwg10.util.XMLUtils.evaluateXPath;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.cat20.dgiwg10.CommonFixture;
import org.opengis.cite.cat20.dgiwg10.SuiteAttribute;
import org.opengis.cite.cat20.dgiwg10.util.DataSampler;
import org.opengis.cite.cat20.dgiwg10.util.TestSuiteLogger;
import org.opengis.cite.cat20.dgiwg10.util.ValidationUtils;
import org.opengis.cite.cat20.dgiwg10.xml.FilterCreator;
import org.opengis.cite.cat20.dgiwg10.xml.RequestCreator;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    private Map<String, Document> queryableToResponseDublinCore = new HashMap<>();

    private Map<String, Document> queryableToResponseIso = new HashMap<>();

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

    @DataProvider(name = "queryables")
    public Iterator<Object[]> queryables() {
        List<Object[]> collectionsData = new ArrayList<>();
        collectionsData.add( new Object[] { "Identifier" } );
        collectionsData.add( new Object[] { "Title" } );
        collectionsData.add( new Object[] { "AnyText" } );
        return collectionsData.iterator();
    }

    @DataProvider(name = "queryableAndFilter")
    public Iterator<Object[]> queryableAndFilter() {
        List<Object[]> collectionsData = new ArrayList<>();
        collectionsData.add( new Object[] { "Identifier", createIdentifierFilter() } );
        collectionsData.add( new Object[] { "Title", createTitleFilter() } );
        collectionsData.add( new Object[] { "AnyText", createAnyTextFilter() } );
        return collectionsData.iterator();
    }

    /**
     * Issue an HTTP GetRecords request with csw:Record.
     *
     * @param queryable
     *            the queryable to test
     * @param filter
     *            the filter used in the request
     */
    @Test(description = "Implements A.1.2 GetRecord for DGIWG Basic CSW - 'csw:Record' (Requirement 6, 7)", dataProvider = "queryableAndFilter")
    public void issueGetRecords_DublinCore( String queryable, Element filter ) {
        URI endpoint = getOperationEndpoint( this.capabilitiesDoc, GETRECORDS, POST );
        if ( endpoint == null )
            throw new SkipException( "No POST binding available for GetRecords request." );

        if ( filter == null )
            throw new SkipException( "No value available for Queryable '" + queryable + "'." );

        this.requestDocument = requestCreator.createGetRecordsRequest( DC, FULL, filter );
        this.response = this.cswClient.submitPostRequest( endpoint, this.requestDocument );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );

        this.responseDocument = this.response.getEntity( Document.class );
        assertQualifiedName( this.responseDocument, CSW, "GetRecordsResponse" );

        this.queryableToResponseDublinCore.put( queryable, this.responseDocument );
        assertSchemaValid( this.cswValidator, new DOMSource( this.responseDocument ) );
    }

    /**
     * Issue an HTTP GetRecords request with gmd:MD_Metadata.
     *
     * @param queryable
     *            the queryable to test
     * @param filter
     *            the filter used in the request
     */
    @Test(description = "Implements A.1.2 GetRecord for DGIWG Basic CSW - 'gmd:MD_Metadata' (Requirement 6, 7)", dataProvider = "queryableAndFilter")
    public void issueGetRecords_Iso( String queryable, Element filter ) {
        URI endpoint = getOperationEndpoint( this.capabilitiesDoc, GETRECORDS, POST );
        if ( endpoint == null )
            throw new SkipException( "No POST binding available for GetRecords request." );

        if ( filter == null )
            throw new SkipException( "No value available for Queryable '" + queryable + "'." );

        this.requestDocument = requestCreator.createGetRecordsRequest( ISO19193, FULL, filter );
        this.response = this.cswClient.submitPostRequest( endpoint, this.requestDocument );
        assertStatusCode( this.response.getStatus(), 200 );
        assertXmlContentType( this.response.getHeaders() );

        this.responseDocument = this.response.getEntity( Document.class );
        assertQualifiedName( this.responseDocument, CSW, "GetRecordsResponse" );

        this.queryableToResponseIso.put( queryable, this.responseDocument );
        assertSchemaValid( this.isoValidator, new DOMSource( this.responseDocument ) );
    }

    /**
     * Verify that all metadata returnables are present in the result (csw:Record).
     *
     * @param queryable
     *            the queryable to test
     * @throws XPathExpressionException
     *             should never happen
     */
    @Test(description = "Implements A.1.2 GetRecord for DGIWG Basic CSW - 'csw:Record', returnables (Requirement 8)", dependsOnMethods = "issueGetRecords_DublinCore", alwaysRun = true, dataProvider = "queryables")
    public void issueGetRecords_Returnables_DublinCore( String queryable )
                            throws XPathExpressionException {
        Document response = this.queryableToResponseDublinCore.get( queryable );
        if ( response == null )
            throw new SkipException( "No response available for queryable " + queryable );

        Node record = (Node) evaluateXPath( response, "//csw:Record[1]", null, NODE );
        if ( record == null )
            throw new AssertionError( "No csw:Record record available" );
        assertReturnablesDublinCore( record );
    }

    /**
     * Verify that all metadata returnables are present in the result (gmd:MD_Metadata).
     *
     * @param queryable
     *            the queryable to test
     * @throws XPathExpressionException
     *             should never happen
     */
    @Test(description = "Implements A.1.2 GetRecord for DGIWG Basic CSW - 'gmd:MD_Metadata', returnables (Requirement 8)", dependsOnMethods = "issueGetRecords_Iso", alwaysRun = true, dataProvider = "queryables")
    public void issueGetRecords_Returnables_Iso( String queryable )
                            throws XPathExpressionException {
        Document response = this.queryableToResponseIso.get( queryable );
        if ( response == null )
            throw new SkipException( "No response available for queryable " + queryable );

        Node record = (Node) evaluateXPath( response, "//gmd:MD_Metadata[1]", null, NODE );
        if ( record == null )
            throw new AssertionError( "No gmd:MD_Metadata record available" );
        assertReturnablesIso( record );
    }

    private Element createIdentifierFilter() {
        Map<String, Node> records = dataSampler.getRecords();
        for ( String identifier : records.keySet() ) {
            if ( identifier != null )
                return filterCreator.createIdentifierFilter( ISO19193, identifier );
        }
        return null;
    }

    private Element createTitleFilter() {
        Map<String, Node> records = dataSampler.getRecords();
        for ( Node record : records.values() ) {
            String title = findTitle( record );
            if ( title != null )
                return filterCreator.createTitleFilter( DC, title );
        }
        return null;
    }

    private Object createAnyTextFilter() {
        Map<String, Node> records = dataSampler.getRecords();
        for ( Node record : records.values() ) {
            String title = findTitle( record );
            if ( title != null )
                return filterCreator.createAnyTextFilter( DC, title );
        }
        return null;
    }

    private String findTitle( Node record ) {
        try {
            return (String) evaluateXPath( record, "//dc:title", null, XPathConstants.STRING );
        } catch ( XPathExpressionException e ) {
            // XPath is fine
        }
        return null;
    }

}