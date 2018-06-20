package org.opengis.cite.cat20.dgiwg10.util;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETCAPABILITIES;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.TRANSACTION;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.GET;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.POST;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationBindings;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;

import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.cat20.dgiwg10.ProtocolBinding;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class ServiceMetadataUtilsTest {

    private static DocumentBuilder docBuilder;

    @BeforeClass
    public static void setUpClass()
                            throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware( true );
        docBuilder = dbf.newDocumentBuilder();
    }

    @Test
    public void testGetOperationEndpoint()
                            throws Exception {
        InputStream is = ServiceMetadataUtilsTest.class.getResourceAsStream( "../getcapabilities/GetCapabilities-response.xml" );
        Document capabilitiesDocument = docBuilder.parse( is );
        URI operationEndpoint = getOperationEndpoint( capabilitiesDocument, GETCAPABILITIES, GET );

        assertThat( operationEndpoint, is( new URI( "http://demo.pycsw.org/cite/csw" ) ) );
    }

    @Test
    public void testGetOperationBindings()
                            throws Exception {
        InputStream is = ServiceMetadataUtilsTest.class.getResourceAsStream( "../getcapabilities/GetCapabilities-response.xml" );
        Document capabilitiesDocument = docBuilder.parse( is );
        Set<ProtocolBinding> operationBindingsGetCapabilities = getOperationBindings( capabilitiesDocument,
                                                                                      GETCAPABILITIES );

        assertThat( operationBindingsGetCapabilities.size(), is( 2 ) );
        assertThat( operationBindingsGetCapabilities, hasItem( GET ) );
        assertThat( operationBindingsGetCapabilities, hasItem( POST ) );

        Set<ProtocolBinding> operationBindingsTransaction = getOperationBindings( capabilitiesDocument, TRANSACTION );

        assertThat( operationBindingsTransaction.size(), is( 1 ) );
        assertThat( operationBindingsTransaction, hasItem( POST ) );

    }

}
