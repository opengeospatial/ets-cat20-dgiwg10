package org.opengis.cite.cat20.dgiwg10.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opengis.cite.cat20.dgiwg10.util.NamespaceBindings.withStandardBindings;
import static org.opengis.cite.cat20.dgiwg10.util.OutputSchema.DC;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Element;

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
    public void testCreateIdentifierFilter() {
        String identifier = "abc";
        Element filter = filterCreator.createIdentifierFilter( DC, identifier );

        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsEqualTo/ogc:PropertyName", is( "dc:identifier" ),
                              withStandardBindings() ) );
        assertThat( the( filter ),
                    hasXPath( "//ogc:Filter/ogc:PropertyIsEqualTo/ogc:Literal", is( identifier ),
                              withStandardBindings() ) );
    }

}
