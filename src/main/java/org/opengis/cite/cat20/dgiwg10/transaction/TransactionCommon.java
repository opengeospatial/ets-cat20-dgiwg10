package org.opengis.cite.cat20.dgiwg10.transaction;

import static org.opengis.cite.cat20.dgiwg10.ETSAssert.assertXPath;
import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;
import static org.opengis.cite.cat20.dgiwg10.util.XMLUtils.evaluateXPath;

import java.net.URI;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.opengis.cite.cat20.dgiwg10.CommonFixture;
import org.opengis.cite.cat20.dgiwg10.ProtocolBinding;
import org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TransactionCommon extends CommonFixture {

    protected static final String CSWT_IDENTIFIER = "http://www.dgiwg.org/std/csw/1.0/conf/dgiwg_cswt";

    private static final String ABSTRACT_TEXT_19 = "This service implements the DGIWG Catalogue Service for the Web ISO Profile version 1.0, DGIWG Basic CSW conformance class (http://www.dgiwg.org/std/csw/1.0/conf/basic) and DGIWG Transactional CSW (”http://www.dgiwg.org/std/csw/1.0/conf/dgiwg_cswt)";

    /**
     * Verify that the Abstract present in the service metadata includes basic identifier.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW", groups = "isTransactional")
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
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW", groups = "isTransactional")
    public void hasTransactionPostUrl() {
        URI transactionUrl = ServiceMetadataUtils.getOperationEndpoint( capabilitiesDoc, "Transaction",
                                                                        ProtocolBinding.POST );
        if ( transactionUrl == null )
            throw new SkipException( "CSW does not provide a POST Url for Transaction requests." );
    }

    /**
     * Verify that the Abstract present in the service metadata includes the text defined in requirement 19.
     */
    @Test(description = "Implements A.1.4 DGIWG Transactional CSW (Requirement 19)", groups = "isTransactional", dependsOnMethods = "isTransactionalCsw")
    public void verifyAbstract() {
        String xpath = "contains(normalize-space(//csw:Capabilities/ows:ServiceIdentification/ows:Abstract), '"
                       + ABSTRACT_TEXT_19 + "')";
        assertXPath( capabilitiesDoc, xpath, withStandardBindings().getAllBindings(),
                     "Abstract does not contain the expected text '" + ABSTRACT_TEXT_19 + "'." );
    }

}
