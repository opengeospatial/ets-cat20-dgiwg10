package org.opengis.cite.cat20.dgiwg10.returnables;

import static javax.xml.xpath.XPathConstants.BOOLEAN;
import static org.opengis.cite.cat20.dgiwg10.util.XMLUtils.evaluateXPath;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class Returnables {

    // mandatory returnables from "Table 7: Additional Queryables/Returnables"
    enum Returnable {

        IDENTIFIER( "Identifier", "dc:identifier", "./gmd:fileIdentifier" ),

        TITLE( "Title", "dc:title", "./gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:title" ),

        SUBJECT(
                "Subject",
                "dc:subject",
                "./gmd:identificationInfo/*/gmd:descriptiveKeywords/gmd:MD_Keywords/gmd:keyword | ./gmd:identificationInfo/*/gmd:topicCategory" ),

        ABSTRACT( "Abstract", "dc:description", "./gmd:identificationInfo/*/gmd:abstract" ),

        TYPE( "Type", "dc:type", "./gmd:hierarchyLevel/gmd:MD_ScopeCode/@codeListValue" ),

        FORMAT( "Format", "dc:format",
               "./gmd:distributionInfo/gmd:MD_Distribution/gmd:distributionFormat/gmd:MD_Format/gmd:name" ),

        LANGUAGE( "Language", "dc:language", "./gmd:language" );

        private String name;

        private String dublinCoreXpath;

        private String isoXpath;

        Returnable( String name, String dublinCoreXpath, String isoXpath ) {
            this.name = name;
            this.dublinCoreXpath = dublinCoreXpath;
            this.isoXpath = isoXpath;
        }
    }

    public static void assertReturnablesDublinCore( Node record )
                            throws XPathExpressionException {
        assertReturnables( record, r -> r.dublinCoreXpath );
    }

    public static void assertReturnablesIso( Node record )
                            throws XPathExpressionException {
        assertReturnables( record, r -> r.isoXpath );
    }

    public static void assertReturnables( Node record, Function<Returnable, String> f )
                            throws XPathExpressionException {
        List<String> invalidReturnables = new ArrayList<>();
        for ( Returnable returnable : Returnable.values() ) {
            String returnableXPath = f.apply( returnable );
            boolean hasReturnable = (boolean) evaluateXPath( record, returnableXPath, null, BOOLEAN );
            if ( !hasReturnable )
                invalidReturnables.add( returnable.name );
        }

        assertTrue( invalidReturnables.isEmpty(),
                    "Missing returnables in GetRecords response: " + String.join( ",", invalidReturnables ) );
    }
}
