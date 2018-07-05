package org.opengis.cite.cat20.dgiwg10.xml;

import static org.junit.Assert.assertThat;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.transform.XmlConverters.the;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlmatchers.validation.SchemaFactory;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestCreatorTest {

    private static DocumentBuilder docBuilder;

    private final RequestCreator requestCreator = new RequestCreator();

    @BeforeClass
    public static void setupDocumentBuilder()
                            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        docBuilder = factory.newDocumentBuilder();
    }

    @Test
    public void testCreateGetRecordsRequest()
                            throws Exception {
        Document getRecordsRequest = requestCreator.createGetRecordsRequest( DC, FULL );

        assertThat( the( getRecordsRequest ), conformsTo( cswSchema() ) );
    }

    @Test
    public void testCreateGetRecordsRequestWithFilter()
                            throws Exception {
        Node filter = parseFilter( "propertyIsEqualTo-filter.xml" );
        Document getRecordsRequest = requestCreator.createGetRecordsRequest( DC, FULL, filter );

        assertThat( the( getRecordsRequest ), conformsTo( cswSchema() ) );
    }

    private Schema cswSchema()
                            throws Exception {
        // TODO: use local schema
        return SchemaFactory.w3cXmlSchemaFrom( new URL( "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" ) );
    }

    private Node parseFilter( String filterResource )
                            throws IOException, SAXException {
        InputStream is = getClass().getResourceAsStream( filterResource );
        return docBuilder.parse( is ).getDocumentElement();
    }

}