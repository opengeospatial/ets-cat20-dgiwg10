package org.opengis.cite.cat20.dgiwg10.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Verifies the behavior of the URIUtils class.
 */
public class URIUtilsTest {

    @Test
    public void resolveClasspathResource()
                            throws SAXException, IOException, URISyntaxException {
        URL url = this.getClass().getResource( "../getcapabilities/GetCapabilities-response.xml" );
        Document doc = URIUtils.parseURI( url.toURI() );
        assertNotNull( doc );
        assertEquals( "Document element has unexpected [local name].", "Capabilities",
                      doc.getDocumentElement().getLocalName() );
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolveMissingClasspathResource()
                            throws SAXException, URISyntaxException, IOException {
        URL url = this.getClass().getResource( "/alpha.xml" );
        URI uri = ( null != url ) ? url.toURI() : null;
        Document doc = URIUtils.parseURI( uri );
        assertNull( doc );
    }

}
