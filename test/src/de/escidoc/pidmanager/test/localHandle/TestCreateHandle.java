package de.escidoc.pidmanager.test.localHandle;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.escidoc.pidmanager.test.TestAsserts;
import de.escidoc.pidmanager.test.TestBase;
import de.escidoc.pidmanager.test.common.CreatedHandles;

/**
 * Test create Handles.
 * 
 * @author SWA
 * 
 */
public class TestCreateHandle extends TestBase {

    private CreatedHandles createdHandles = null;

    /**
     * Set up.
     * 
     * @throws Exception
     *             Thrown if obtaining created Handles failed.
     */
    @Before
    public void setUp() throws Exception {
        if (this.createdHandles == null) {
            this.createdHandles = new CreatedHandles();
        }
    }

    /**
     * Tear down.
     * 
     * @throws Exception
     *             Thrown if remove/delete of created Handles failed.
     */
    @After
    public void tearDown() throws Exception {

        // remove all Handles
        Vector<String> handles = this.createdHandles.getAllHandles();

        for (int i = 0; i < handles.size(); i++) {
            deleteHandle(handles.get(i));
            this.createdHandles.remove(handles.get(i));
        }
    }

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

        String newHandle = "test-handle";

        String paramXml =
            "<param>\n\t<url>" + getProp().get("pidManagerHost") + "</url>\n"
                + "</param>\n";

        this.createdHandles.add(newHandle);

        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        httpclient.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        String url =
            getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/" + newHandle;
        PutMethod put = new PutMethod(url);

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

    /**
     * Test create Handle through suffix computing by service.
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
    public void testCreateHandle2() throws Exception {

        String newHandle = null;
        deleteHandle("test-handle");
        String paramXml =
            "<param>\n\t<url>" + getProp().get("pidManagerHost") + "</url>\n"
                + "</param>\n";

        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        httpclient.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        PostMethod post =
            new PostMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/");
        RequestEntity requestEntity =
            new StringRequestEntity(paramXml, "text/xml", "UTF-8");
        post.setRequestEntity(requestEntity);

        try {
            int statusCode = httpclient.executeMethod(post);
            assertEquals("Wrong HTTP response code", HttpStatus.SC_OK,
                statusCode);

            InputStream in = post.getResponseBodyAsStream();

            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(in));

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();

            XPathExpression xPathLastModDate = xPath.compile("/param/pid");
            newHandle =
                (String) xPathLastModDate.evaluate(doc, XPathConstants.STRING);

        }
        finally {
            post.releaseConnection();
        }

        TestAsserts.assertHandleSchema(newHandle);

        String suffix = getSuffix(newHandle);
        this.createdHandles.add(suffix);

        resolveHandle(suffix);
        deleteHandle(suffix);
    }

}
