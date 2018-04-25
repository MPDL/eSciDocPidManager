package de.escidoc.pidmanager.test.GWDGService;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.escidoc.pidmanager.test.TestBase;

/**
 * Test create Handles.
 * 
 * @author SWA
 * 
 */
public class TestCreateHandle extends TestBase {

    /**
     * Set up.
     * 
     * @throws Exception
     *             Thrown if obtaining created Handles failed.
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Tear down.
     * 
     * @throws Exception
     *             Thrown if remove/delete of created Handles failed.
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test create Handle through call HTTP POST on GWDG handle service.
     * Resource URL: http://www.escidoc.org/+nanoTime
     * 
     * Handle identifier is created by GWDG service. Use: Basic service
     * 
     * @throws Exception
     *             Thrown if creation fail.
     */
    @Test
    public void testCreateHandle() throws Exception {

        String urlR = "http://www.escidoc.org/" + System.nanoTime();
        String paramXml = "<param>\n\t<url>" + urlR + "</url>\n" + "</param>\n";

        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        httpclient.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        String url =
            getProp().get("pidManagerHost") + "pid/"
                + getProp().get("handlePrefix");
        PostMethod post = new PostMethod(url);

        RequestEntity requestEntity =
            new StringRequestEntity(paramXml, "text/xml", "UTF-8");
        post.setRequestEntity(requestEntity);

        try {
            int statusCode = httpclient.executeMethod(post);
            assertEquals("Wrong HTTP response code", HttpStatus.SC_OK,
                statusCode);
        }
        finally {
            post.releaseConnection();
        }
    }

    /**
     * Test create Handle through call HTTP POST on GWDG handle service.
     * Resource URL: http://www.escidoc.org/+nanoTime
     * 
     * Handle identifier is created by GWDG service. Use: extend service
     * 
     * @throws Exception
     *             Thrown if creation fail.
     */
    @Test
    public void testCreateHandle02() throws Exception {

        String timestamp = String.valueOf(System.nanoTime());
        String paramXml =
            "<param>\n\t<url>http://www.escidoc.org/"
                + timestamp
                + "</url>\n"
                + "<pid>11858/TEST-"
                + timestamp.substring(0, 7)
                + "</pid>\n"
                + "<checksum>sha1:1234567890123456789012345678901234567890</checksum>\n"
                + "<filesize>123</filesize>\n" + "<title>test-title</title>\n"
                + "<authors>Test Author 1, Test Author 2</authors>\n"
                + "<publication-date>2009</publication-date>\n" + "</param>\n";

        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        httpclient.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        String url =
            getProp().get("pidManagerHost") + "pid/"
                + getProp().get("handlePrefix");
        PostMethod post = new PostMethod(url);

        RequestEntity requestEntity =
            new StringRequestEntity(paramXml, "text/xml", "UTF-8");
        post.setRequestEntity(requestEntity);

        try {
            int statusCode = httpclient.executeMethod(post);
            if (statusCode != HttpStatus.SC_OK) {
                System.out.println(post.getResponseBodyAsString());
                assertEquals("Wrong HTTP response code", HttpStatus.SC_OK,
                    statusCode);
            }
        }
        finally {
            post.releaseConnection();
        }
    }

}
