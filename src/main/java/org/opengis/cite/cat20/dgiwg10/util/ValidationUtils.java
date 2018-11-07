package org.opengis.cite.cat20.dgiwg10.util;

import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.opengis.cite.validation.XmlSchemaCompiler;
import org.xml.sax.SAXException;

/**
 * A utility class that provides convenience methods to support schema validation.
 */
public class ValidationUtils {

    /**
     * Creates a single Schema object available from the passed schemaUrls.
     *
     * @param schemaUrls
     *            the URL of the xsd file, never <code>null</code>
     * @return an immutable Schema object, or <code>null</code> if one cannot be constructed.
     */
    public static Schema createSchema( URI... schemaUrls ) {
        URL entityCatalog = ValidationUtils.class.getResource( "/org/opengis/cite/cat20/dgiwg10/schema-catalog.xml" );
        XmlSchemaCompiler xsdCompiler = new XmlSchemaCompiler( entityCatalog );
        Schema cswSchema = null;
        try {
            Source[] xsdSources = new Source[schemaUrls.length];
            for ( int i = 0; i < schemaUrls.length; i++ ) {
                xsdSources[i] = new StreamSource( schemaUrls[i].toString() );
            }
            cswSchema = xsdCompiler.compileXmlSchema( xsdSources );
        } catch ( SAXException e ) {
            TestSuiteLogger.log( Level.WARNING, "Failed to create CSW Schema object.", e );
        }
        return cswSchema;
    }
}
