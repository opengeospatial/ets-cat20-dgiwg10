package org.opengis.cite.cat20.dgiwg10.util;

import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.CSW_PREFIX;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.DC;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.DC_PREFIX;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.OGC;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.OGC_PREFIX;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.OWS;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.OWS_PREFIX;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.XLINK;
import static org.opengis.cite.cat20.dgiwg10.Namespaces.XLINK_PREFIX;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Provides namespace bindings for evaluating XPath 1.0 expressions using the JAXP XPath API. A namespace name (URI) may
 * be bound to only one prefix.
 */
public class NamespaceBindings implements NamespaceContext {

    private Map<String, String> bindings = new HashMap<>();

    @Override
    public String getNamespaceURI( String prefix ) {
        String nsName = null;
        for ( Map.Entry<String, String> binding : bindings.entrySet() ) {
            if ( binding.getValue().equals( prefix ) ) {
                nsName = binding.getKey();
                break;
            }
        }
        return nsName;
    }

    @Override
    public String getPrefix( String namespaceURI ) {
        return bindings.get( namespaceURI );
    }

    @Override
    public Iterator<String> getPrefixes( String namespaceURI ) {
        return Arrays.asList( getPrefix( namespaceURI ) ).iterator();
    }

    /**
     * Adds a namespace binding that associates a namespace name with a prefix. If a binding for a given namespace name
     * already exists it will be replaced.
     * 
     * @param namespaceURI
     *            A String denoting a namespace name (an absolute URI value).
     * @param prefix
     *            A prefix associated with the namespace name.
     */
    public void addNamespaceBinding( String namespaceURI, String prefix ) {
        bindings.put( namespaceURI, prefix );
    }

    /**
     * Adds all of the supplied namespace bindings to the existing set of entries.
     * 
     * @param nsBindings
     *            A Map containing a collection of namespace bindings where the key is an absolute URI specifying the
     *            namespace name and the value denotes the associated prefix.
     */
    public void addAllBindings( Map<String, String> nsBindings ) {
        if ( null != nsBindings )
            bindings.putAll( nsBindings );
    }

    /**
     * Returns an unmodifiable view of the declared namespace bindings.
     * 
     * @return An immutable Map containing zero or more namespace bindings where the key is an absolute URI specifying
     *         the namespace name and the value is the associated prefix.
     */
    public Map<String, String> getAllBindings() {
        return Collections.unmodifiableMap( this.bindings );
    }

    /**
     * Creates a NamespaceBindings object that declares the following namespace bindings:
     * 
     * <ul>
     * <li>{@value org.opengis.cite.cat20.dgiwg10.Namespaces#OWS_PREFIX}:
     * {@value org.opengis.cite.cat20.dgiwg10.Namespaces#OWS}</li>
     * <li>{@value org.opengis.cite.cat20.dgiwg10.Namespaces#XLINK_PREFIX}:
     * {@value org.opengis.cite.cat20.dgiwg10.Namespaces#XLINK}</li>
     * <li>{@value org.opengis.cite.cat20.dgiwg10.Namespaces#CSW_PREFIX}:
     * {@value org.opengis.cite.cat20.dgiwg10.Namespaces#CSW}</li>
     * <li>{@value org.opengis.cite.cat20.dgiwg10.Namespaces#DC_PREFIX}:
     * {@value org.opengis.cite.cat20.dgiwg10.Namespaces#DC}</li>
     * <li>{@value org.opengis.cite.cat20.dgiwg10.Namespaces#OGC_PREFIX}:
     * {@value org.opengis.cite.cat20.dgiwg10.Namespaces#OGC}</li>
     * </ul>
     * 
     * @return A NamespaceBindings object.
     */
    public static NamespaceBindings withStandardBindings() {
        NamespaceBindings nsBindings = new NamespaceBindings();
        nsBindings.addNamespaceBinding( OWS, OWS_PREFIX );
        nsBindings.addNamespaceBinding( XLINK, XLINK_PREFIX );
        nsBindings.addNamespaceBinding( CSW, CSW_PREFIX );
        nsBindings.addNamespaceBinding( DC, DC_PREFIX );
        nsBindings.addNamespaceBinding( OGC, OGC_PREFIX );
        return nsBindings;
    }

    @Override
    public String toString() {
        return "NamespaceBindings:\n" + bindings;
    }
}
