package org.opengis.cite.cat20.dgiwg10.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class DataSamplerIT {

    @Test
    public void testAcquireRecords()
                            throws Exception {
        DataSampler dataSampler = new DataSampler( readCapabilitiesDocument() );
        dataSampler.acquireRecords();
        Map<String, Node> records = dataSampler.getRecords();
        assertThat( records.size(), is( 10 ) );
    }

    private Document readCapabilitiesDocument()
                            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        return docBuilder.parse( "http://demo.pycsw.org/cite/csw?service=CSW&acceptVersions=2.0.2&request=GetCapabilities" );
    }

}