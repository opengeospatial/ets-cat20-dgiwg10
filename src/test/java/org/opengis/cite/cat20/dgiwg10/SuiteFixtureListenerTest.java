package org.opengis.cite.cat20.dgiwg10;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.testng.ISuite;
import org.testng.xml.XmlSuite;

public class SuiteFixtureListenerTest {

    private static XmlSuite xmlSuite;

    private static ISuite suite;

    @BeforeClass
    public static void setUpClass() {
        xmlSuite = mock( XmlSuite.class );
        suite = mock( ISuite.class );
        when( suite.getXmlSuite() ).thenReturn( xmlSuite );
    }

    @Test
    public void onStart()
                            throws URISyntaxException {
        URL url = this.getClass().getResource( "getcapabilities/GetCapabilities-response.xml" );
        Map<String, String> params = new HashMap<>();
        params.put( TestRunArg.IUT.toString(), url.toURI().toString() );
        when( xmlSuite.getParameters() ).thenReturn( params );
        SuiteFixtureListener iut = new SuiteFixtureListener();
        iut.onStart( suite );
        verify( suite ).setAttribute( Matchers.eq( SuiteAttribute.TEST_SUBJ_FILE.getName() ), Matchers.isA( File.class ) );
    }

    @Test(expected = IllegalArgumentException.class)
    public void onStart_noSuiteParameters() {
        Map<String, String> params = new HashMap<>();
        when( xmlSuite.getParameters() ).thenReturn( params );
        SuiteFixtureListener iut = new SuiteFixtureListener();
        iut.onStart( suite );
    }

}
