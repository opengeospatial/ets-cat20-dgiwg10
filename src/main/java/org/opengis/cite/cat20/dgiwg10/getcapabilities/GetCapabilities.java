package org.opengis.cite.cat20.dgiwg10.getcapabilities;

import static javax.xml.xpath.XPathConstants.BOOLEAN;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.ACCEPT_VERSIONS_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETCAPABILITIES;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.REQUEST_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_PARAM;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_TYPE;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.SERVICE_VERSION;
import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXPath;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.GET;
import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat20.dgiwg10.CommonFixture;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.ClientResponse;

/**
 * A.1.1 GetCapabilities for DGIWG Basic CSW
 * <p>
 * a) Test Purpose: Verify that the server implements DGIWG the following DGIWG requirements (Requirement 1, Requirement
 * 2, Requirement 3, Requirement 4, Requirement 5, Requirement 6 , Requirement 7, Requirement 8, Requirement 9,
 * Requirement 10, Requirement 11 ).
 * <p>
 * b) Test Method:
 * <p>
 * - Issue an HTTP GET capabilities request.
 * <p>
 * - Verify that the service responds without error to the request with a Capabilities document (Requirement 1 and
 * Requirement 3 ).
 * <p>
 * - Verify that the response indicates support for ‘English’ for queriables and returnables . (Requirement 2)
 * <p>
 * - Verify that the response metadata link includes the profile text described in requirement 4 .
 * <p>
 * - Verify that the XML response indicates support for csw:Record and gmd:MD_Metadata return types for the GetRecords
 * operation . (Requirement 6 ).
 * <p>
 * - Verify that the reported queriables and returnables for the GetRecords operation. at least include those defined in
 * section s 7.1.1 and 7.1.3 . (Requirements 5 , 8 ).
 * <p>
 * - Verify that the XML response indicates support for csw:Record and gmd:MD_Metadata return types for the GetRecord
 * ById operation. (Requirement 11 ).
 * <p>
 * - Verify that the reported queriables and returnables for the GetRecord ById operation. at least include those
 * defined in section s 7.1.1 and 7.1.3 . (Requirement 10 ).
 * <p>
 * c) References: Sections 7.3, 7.4, 7.5, 7.6.1
 * <p>
 * d) Test Type: Capability
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GetCapabilities extends CommonFixture {

    private static final String ABSTRACT_TEXT = "This service implements the DGIWG Catalogue Service for the Web ISO Profile version 1.0, DGIWG Basic CSW conformance class (http://www.dgiwg.org/std/csw/1.0/conf/basic)";

    private static List<String> ADDITIONAL_ISO_QUERYABLES = Arrays.asList( "AnyText", "Title", "Abstract",
                                                                           "Identifier", "Modified", "Type",
                                                                           "BoundingBox", "Source", "Association",
                                                                           "CRS", "RevisionDate", "AlternateTitle",
                                                                           "CreationDate", "PublicationDate",
                                                                           "OrganisationName",
                                                                           "HasSecurityConstraints",
                                                                           "ResourceIdentifier", "ParentIdentifier",
                                                                           "KeywordType", "TopicCategory",
                                                                           "ResourceLanguage",
                                                                           "GeographicDescriptionCode", "Denominator",
                                                                           "DistanceValue", "DistanceUOM",
                                                                           "ServiceType", "ServiceTypeVersion",
                                                                           "GeographicDescriptionCode", "OperatesOn",
                                                                           "OperatesOnIdentifier", "OperatesOnName",
                                                                           "CouplingType", "Operation" );

    private ClientResponse capabilitiesResponse;

    private Document capabilitiesDocument;

    /**
     * Issue an HTTP GET capabilities request.
     */
    @Test(description = "Implements A.1.1 GetCapabilities for DGIWG Basic CSW")
    public void issueGetCapabilities() {
        URI capabilitiesUrl = getOperationEndpoint( this.capabilitiesDoc, GETCAPABILITIES, GET );

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put( REQUEST_PARAM, GETCAPABILITIES );
        queryParams.put( SERVICE_PARAM, SERVICE_TYPE );
        queryParams.put( ACCEPT_VERSIONS_PARAM, SERVICE_VERSION );
        this.capabilitiesResponse = this.cswClient.submitGetRequest( capabilitiesUrl, queryParams );
        this.response = this.capabilitiesResponse;
    }

    /**
     * Verify that the service responds without error to the request with a Capabilities document (Requirement 1 and
     * Requirement 3).
     */
    @Test(description = "Implements A.1.1 GetCapabilities for DGIWG Basic CSW (Requirement 1, 3)", dependsOnMethods = "issueGetCapabilities")
    public void verifyNoError() {
        setCurrentResponse();
        int status = this.capabilitiesResponse.getStatus();
        assertEquals( status, 200, String.format( "Expected status code 200 but received %d.", status ) );

        this.capabilitiesDocument = this.response.getEntity( Document.class );
        this.responseDocument = this.capabilitiesDocument;
        assertXPath( "//csw:Capabilities", capabilitiesDocument, withStandardBindings().getAllBindings() );
    }

    /**
     * Verify that the response metadata link includes the profile text described in requirement 4.
     */
    @Test(description = "Implements A.1.1 GetCapabilities for DGIWG Basic CSW (Requirement 4)", dependsOnMethods = "verifyNoError")
    public void verifyMetadataLink() {
        setCurrentResponse();
        assertResponseDocument();
        String xpath = "contains(normalize-space(//csw:Capabilities/ows:ServiceIdentification/ows:Abstract), '"
                       + ABSTRACT_TEXT + "')";
        assertXPath( xpath, capabilitiesDocument, withStandardBindings().getAllBindings(),
                     "Abstract does not contain the expected text '" + ABSTRACT_TEXT + "'." );
    }

    /**
     * Verify that the XML response indicates support for csw:Record and gmd:MD_Metadata return types for the GetRecords
     * operation. (Requirement 6).
     */
    @Test(description = "Implements A.1.1 GetCapabilities for DGIWG Basic CSW (Requirement 6)", dependsOnMethods = "verifyNoError")
    public void verifySupportGetRecordsReturnType() {
        setCurrentResponse();
        assertResponseDocument();
        String xpath = "//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:Parameter[@name='typeNames']/ows:Value [text() = 'csw:Record' ] and "
                       + "//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:Parameter[@name='typeNames']/ows:Value[text() = 'gmd:MD_Metadata' ]";
        assertXPath( xpath, capabilitiesDocument, withStandardBindings().getAllBindings(),
                     "Return types csw:Record and/or gmd:MD_Metadata for the GetRecords operation are not supported." );
    }

    /**
     * Verify that the reported queriables and returnables for the GetRecords operation. At least include those defined
     * in section s 7.1.1 and 7.1.3. (Requirements 5, 8).
     * 
     * Requirement 5 is tested here for queryables.
     */
    @Test(description = "Implements A.1.1 GetCapabilities for DGIWG Basic CSW (Requirement 5, 8, GetRecords)", dependsOnMethods = "verifyNoError")
    public void verifyGetRecordsQueryables() {
        setCurrentResponse();
        assertResponseDocument();
        List<String> unsupportedQuerables = collectUnsupportedQueryables( "GetRecords" );
        assertTrue( unsupportedQuerables.isEmpty(), "Missing queryables for GetRecords operation: "
                                                    + unsupportedQuerables );
    }

    /**
     * Verify that the XML response indicates support for csw:Record and gmd:MD_Metadata return types for the
     * GetRecordById operation. (Requirement 11).
     */
    @Test(description = "Implements A.1.1 GetCapabilities for DGIWG Basic CSW (Requirement 11)", dependsOnMethods = "verifyNoError")
    public void verifySupportGetRecordByIdReturnType() {
        setCurrentResponse();
        assertResponseDocument();
        String xpath = "//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:Parameter[@name='typeNames']/ows:Value [text() = 'csw:Record' ] and "
                       + "//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:Parameter[@name='typeNames']/ows:Value[text() = 'gmd:MD_Metadata' ]";
        assertXPath( xpath, capabilitiesDocument, withStandardBindings().getAllBindings(),
                     "Return types csw:Record and/or gmd:MD_Metadata for the GetRecordById operation are not supported." );
    }

    private void setCurrentResponse() {
        this.response = this.capabilitiesResponse;
        this.responseDocument = this.capabilitiesDocument;
    }

    /**
     * For testing purposes only!
     *
     * @param rspDocument
     *            never <code>null</code>
     */
    void setResponseDocument( Document rspDocument ) {
        this.capabilitiesDocument = rspDocument;
    }

    private void assertResponseDocument() {
        if ( capabilitiesDocument == null )
            throw new SkipException( "Capabilities document could not be requested, test will be skipped." );
    }

    private List<String> collectUnsupportedQueryables( String operation ) {
        List<String> unsupportedQuerables = new ArrayList<>();

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext( withStandardBindings() );
        for ( String additionalQueryable : ADDITIONAL_ISO_QUERYABLES ) {
            String xpathExpr = createQueryablePropertiesXPath( operation, additionalQueryable );
            try {
                Boolean queryableExists = (Boolean) xpath.evaluate( xpathExpr, capabilitiesDocument, BOOLEAN );
                if ( !queryableExists )
                    unsupportedQuerables.add( additionalQueryable );
            } catch ( XPathExpressionException e ) {
                // XPath is correct
            }
        }
        return unsupportedQuerables;
    }

    private String createQueryablePropertiesXPath( String operationName, String additionalQueryable ) {
        StringBuilder xpath = new StringBuilder();
        // Per Operation
        xpath.append( "(" );
        xpath.append( "//ows:OperationsMetadata/ows:Operation[@name='" );
        xpath.append( operationName );
        xpath.append( "']/ows:Constraint[@name='SupportedISOQueryables']/ows:Value[text()='" );
        xpath.append( additionalQueryable );
        xpath.append( "' ]" );
        xpath.append( ")" );
        // OR
        xpath.append( " or " ).append( "\n" );
        // global
        xpath.append( "(" );
        xpath.append( "//ows:OperationsMetadata/ows:Constraint[@name='SupportedISOQueryables']/ows:Value[text()='" );
        xpath.append( additionalQueryable );
        xpath.append( "' ]" );
        xpath.append( ")" );
        return xpath.toString();
    }

}