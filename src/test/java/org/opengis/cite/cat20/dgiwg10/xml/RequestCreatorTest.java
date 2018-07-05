package org.opengis.cite.cat20.dgiwg10.xml;

import static org.junit.Assert.assertThat;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.transform.XmlConverters.the;

import java.net.URL;

import javax.xml.validation.Schema;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xmlmatchers.validation.SchemaFactory;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestCreatorTest {

    private final RequestCreator requestCreator = new RequestCreator();

    @Test
    public void testCreateGetRecordsRequest()
                            throws Exception {
        Document getRecordsRequest = requestCreator.createGetRecordsRequest( DC, FULL );

        assertThat( the( getRecordsRequest ), conformsTo( cswSchema() ) );
    }

    private Schema cswSchema()
                            throws Exception {
        // TODO: use local schema
        return SchemaFactory.w3cXmlSchemaFrom( new URL( "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" ) );
    }

}