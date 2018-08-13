package org.opengis.cite.cat20.dgiwg10.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.cat20.dgiwg10.util.ElementSetName.FULL;
import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.ISO19193;
import static org.xmlmatchers.XmlMatchers.conformsTo;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.cite.cat20.dgiwg10.util.ElementSetName;
import org.opengis.cite.cat20.dgiwg10.util.OutputSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xmlmatchers.validation.SchemaFactory;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class RequestCreatorTest {

    private static DocumentBuilder docBuilder;

    private static Schema cswSchema;

    private final RequestCreator requestCreator = new RequestCreator();

    @BeforeClass
    public static void setupDocumentBuilder()
                            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        docBuilder = factory.newDocumentBuilder();
    }

    @BeforeClass
    public static void setupSchema()
                            throws MalformedURLException, SAXException {
        // TODO: use local schema
        cswSchema = SchemaFactory.w3cXmlSchemaFrom( new URL( "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" ) );
    }

    @Test
    public void testCreateGetRecordsRequest()
                            throws Exception {
        OutputSchema outputSchema = ISO19193;
        ElementSetName elementSetName = FULL;
        Document getRecordsRequest = requestCreator.createGetRecordsRequest( outputSchema, elementSetName );

        assertThat( the( getRecordsRequest ), conformsTo( cswSchema ) );
        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecords/@outputSchema", is( outputSchema.getOutputSchema() ),
                              withStandardBindings() ) );
        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecords/csw:Query/csw:ElementSetName",
                              is( elementSetName.name().toLowerCase() ), withStandardBindings() ) );
        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecords/csw:Query/@typeNames", is( outputSchema.getTypeName() ),
                              withStandardBindings() ) );
    }

    @Test
    public void testCreateGetRecordsRequestWithFilter()
                            throws Exception {
        Node filter = parseFilter( "propertyIsEqualTo-filter.xml" );
        OutputSchema outputSchema = DC;
        ElementSetName elementSetName = FULL;
        Document getRecordsRequest = requestCreator.createGetRecordsRequest( outputSchema, elementSetName, filter );

        assertThat( the( getRecordsRequest ), conformsTo( cswSchema ) );

        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecords/@outputSchema", is( outputSchema.getOutputSchema() ),
                              withStandardBindings() ) );
        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecords/csw:Query/csw:ElementSetName",
                              is( elementSetName.name().toLowerCase() ), withStandardBindings() ) );
        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecords/csw:Query/@typeNames", is( outputSchema.getTypeName() ),
                              withStandardBindings() ) );
    }

    @Test
    public void testCreateGetRecordByIdRequestWithFilter()
                            throws Exception {
        String id = "abc";
        OutputSchema outputSchema = DC;
        ElementSetName elementSetName = FULL;
        Document getRecordsRequest = requestCreator.createGetRecordById( outputSchema, elementSetName, id );

        assertThat( the( getRecordsRequest ), conformsTo( cswSchema ) );

        assertThat( the( getRecordsRequest ), hasXPath( "//csw:GetRecordById/csw:Id", is( id ), withStandardBindings() ) );

        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecordById/@outputSchema", is( outputSchema.getOutputSchema() ),
                              withStandardBindings() ) );
        assertThat( the( getRecordsRequest ),
                    hasXPath( "//csw:GetRecordById/csw:ElementSetName", is( elementSetName.name().toLowerCase() ),
                              withStandardBindings() ) );
    }

    private Node parseFilter( String filterResource )
                            throws IOException, SAXException {
        InputStream is = getClass().getResourceAsStream( filterResource );
        return docBuilder.parse( is ).getDocumentElement();
    }

}