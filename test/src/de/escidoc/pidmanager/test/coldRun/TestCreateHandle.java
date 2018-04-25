package de.escidoc.pidmanager.test.coldRun;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import de.escidoc.pidmanager.test.TestBase;

/**
 * Test creating Handles.
 * 
 * @author SWA
 * 
 */
public class TestCreateHandle extends TestBase {

    /**
     * Test create Handle through call HTTP PUT on handle service. Resource URL:
     * 
     * <pre>
     * handle/&lt;globalPrefix&gt;/&lt;newSuffix&gt;
     * </pre>
     * 
     * .
     * 
     * @throws Exception
     *             Thrown if creation fail.
     */
    @Test
    public void testCreateHandle() throws Exception {

        String paramXml =
            "<param>\n\t<url>" + getProp().get("pidManagerHost") + "</url>\n"
                + "</param>\n";

        HttpClient httpclient = new HttpClient();
        httpclient.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        httpclient.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        PutMethod put =
            new PutMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/test-handle");
        RequestEntity requestEntity =
            new StringRequestEntity(paramXml, "text/xml", "UTF-8");
        put.setRequestEntity(requestEntity);

        try {
            int statusCode = httpclient.executeMethod(put);
            assertEquals("Wrong HTTP response code", HttpStatus.SC_OK,
                statusCode);
        }
        finally {
            put.releaseConnection();
        }
    }

}
