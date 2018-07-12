package org.opengis.cite.cat20.dgiwg10.util;

import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;
import static org.opengis.cite.cat20.dgiwg10.DGIWG1CAT2.GETRECORDS;
import static org.opengis.cite.cat20.dgiwg10.ProtocolBinding.POST;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.ServiceMetadataUtils.getOperationEndpoint;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.opengis.cite.cat20.dgiwg10.xml.RequestCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DataSampler {

    private Document capabilitiesDocument;

    private Map<String, Node> records = new HashMap<>();

    /**
     * Instantiates a new DataSampler, call #acquireRecords() to collection records
     *
     * @param capabilitiesDocument
     *            never <code>nul</code>
     */
    public DataSampler( Document capabilitiesDocument ) {
        this.capabilitiesDocument = capabilitiesDocument;
    }

    /**
     * Requests 10 records in dublin core (full) from the CSW.
     */
    public void acquireRecords() {
        URI endpoint = getOperationEndpoint( this.capabilitiesDocument, GETRECORDS, POST );
        if ( endpoint == null ) {
            throw new IllegalArgumentException( "No POST binding available for GetRecords request." );
        }
        RequestCreator requestCreator = new RequestCreator();
        Document request = requestCreator.createGetRecordsRequest( DC, FULL);

        CSWClient cswClient = new CSWClient( this.capabilitiesDocument );
        ClientResponse getRecordsResponse = cswClient.submitPostRequest( endpoint, request );
        if ( getRecordsResponse.getStatus() != 200 )
            return;
        try {
            XPath xpath = createXPath();
            Document getRecordsResponseDoc = getRecordsResponse.getEntity( Document.class );
            NodeList records = (NodeList) xpath.evaluate( "//csw:Record", getRecordsResponseDoc, NODESET );
            for ( int nodeIndex = 0; nodeIndex < records.getLength(); nodeIndex++ ) {
                Node record = records.item( nodeIndex );
                String identifier = (String) xpath.evaluate( "dc:identifier", record, STRING );
                this.records.put( identifier, record );
            }
        } catch ( XPathExpressionException xpe ) {
            // should never happens...
        }
    }

    private XPath createXPath() {
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext( NamespaceBindings.withStandardBindings() );
        return xpath;
    }

    /**
     * @return the requested records, may be <code>null</code> if the service does not provide any records, an error
     *         occurred during requesting the records or #acquireRecords() was not invoked before
     */
    public Map<String, Node> getRecords() {
        return records;
    }

}
