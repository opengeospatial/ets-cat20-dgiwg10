package org.opengis.cite.cat20.dgiwg10.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.ISO19193;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.xpath.XpathReturnType.returningABoolean;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xmlmatchers.XmlMatchers;
import org.xmlmatchers.validation.SchemaFactory;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class FilterCreatorTest {

    private static DocumentBuilder docBuilder;

    private final FilterCreator filterCreator = new FilterCreator();

    @BeforeClass
    public static void setupDocumentBuilder()
                            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        docBuilder = factory.newDocumentBuilder();
    }

    @Test
    public void testCreateIdentifierFilter()
                            throws Exception {
        String identifier = "abc";
        Element filter = filterCreator.createIdentifierFilter( DC, identifier );

        assertThat( the( filter ),
                    hasXPath( "exists(//ogc:Filter/ogc:PropertyIsEqualTo)", withStandardBindings(),
                              returningABoolean(), is( true ) ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsEqualTo/ogc:PropertyName", is( "dc:identifier" ),
                              withStandardBindings() ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsEqualTo/ogc:Literal", is( identifier ),
                              withStandardBindings() ) );
        assertThat( the( filter ), XmlMatchers.conformsTo( cswSchema() ) );
    }

    @Test
    public void testCreateTitleFilter()
                            throws Exception {
        String title = "xyz";
        Element filter = filterCreator.createTitleFilter( ISO19193, title );

        assertThat( the( filter ),
                    hasXPath( "exists(//ogc:Filter/ogc:PropertyIsEqualTo)", withStandardBindings(),
                              returningABoolean(), is( true ) ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsEqualTo/ogc:PropertyName", is( "Title" ),
                              withStandardBindings() ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsEqualTo/ogc:Literal", is( title ), withStandardBindings() ) );
        assertThat( the( filter ), XmlMatchers.conformsTo( cswSchema() ) );
    }

    @Test
    public void testCreateAnyTextFilter()
                            throws Exception {
        String searchValue = "hij";
        Element filter = filterCreator.createAnyTextFilter( ISO19193, searchValue );

        assertThat( the( filter ),
                    hasXPath( "exists(//ogc:Filter/ogc:PropertyIsLike)", withStandardBindings(), returningABoolean(),
                              is( true ) ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsLike/ogc:PropertyName", is( "AnyText" ),
                              withStandardBindings() ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsLike/ogc:Literal", is( searchValue ), withStandardBindings() ) );
        assertThat( the( filter ), XmlMatchers.conformsTo( cswSchema() ) );
    }

    private Schema cswSchema()
                            throws Exception {
        // TODO: use local schema
        return SchemaFactory.w3cXmlSchemaFrom( new URL( "http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd" ) );
    }

}
