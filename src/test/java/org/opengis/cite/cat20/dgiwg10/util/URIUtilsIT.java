package org.opengis.cite.cat20.dgiwg10.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class URIUtilsIT {

    @Test
    public void resolveHttpUriAsDocument()
                            throws SAXException, IOException {
        URI uriRef = URI.create( "https://www.w3schools.com/xml/note.xml" );
        Document doc = URIUtils.parseURI( uriRef );
        assertNotNull( doc );
        assertEquals( "Document element has unexpected [local name].", "note", doc.getDocumentElement().getLocalName() );
    }

    @Test
    public void resolveHttpUriAsFile()
                            throws IOException {
        URI uriRef = URI.create( "https://www.w3schools.com/xml/note.xml" );
        File file = URIUtils.dereferenceURI( uriRef );
        assertNotNull( file );
        assertTrue( "File should not be empty", file.length() > 0 );
    }

}
