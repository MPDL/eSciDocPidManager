package de.escidoc.pidmanager.test.coldRun;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import de.escidoc.pidmanager.test.TestBase;

/**
 * Test update Handle.
 * 
 * @author SWA
 * 
 */
public class TestUpdate extends TestBase {

    private static Logger log = Logger.getLogger(TestUpdate.class);

    /**
     * 
     * @throws Exception
     *             Actually not thrown.
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test if style sheet is accessible.
     * 
     * @throws Exception
     *             Thrown if anything fails.
     */
    @Test
    public void testStaticFiles() throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(getXslStyleLocation());
        assertEquals("Missing stylesheet", HttpStatus.SC_OK, resp
            .getResponseCode());
        resp = wc.getResponse(getExceptionSchema());
        assertEquals("Missing XSD for exceptions", HttpStatus.SC_OK, resp
            .getResponseCode());

    }

    /**
     * Test the defined resources of response.
     * 
     * @throws Exception
     *             Thrown if anything fails.
     */
    @Test
    public void testResourceConnection() throws Exception {

        WebConversation wc = new WebConversation();
        WebRequest req =
            new GetMethodWebRequest(getProp().get("pidManagerHost")
                + "pid/handle/" + getProp().get("handlePrefix") + "/");
        WebResponse resp = wc.getResource(req);

        if (resp.getResponseCode() == HttpStatus.SC_UNAUTHORIZED) {
            log.info("Need HTTP Authentication for " + req.getQueryString());
            wc.setAuthorization(getProp().getPidManagerUsername(), getProp()
                .getPidManagerPassword());

        }

        assertEquals("Wrong HTTP response code", HttpStatus.SC_BAD_REQUEST,
            resp.getResponseCode());

        req = new GetMethodWebRequest(getProp().get("pidManagerHost") + "pid/");
        resp = wc.getResource(req);
        assertEquals("Wrong HTTP response code", HttpStatus.SC_NOT_FOUND, resp
            .getResponseCode());
    }

    /**
     * Test response if no resource is addressed (/). The RESTlet has to return
     * an exception and a HTTP status code of 404 (Resource not Found). The
     * exception XML is validated against the Schema.
     * 
     * @throws Exception
     *             Thrown if anything fails.
     */
    @Test
    public void testXmlValidateExceptions() throws Exception {
        // 
        GetMethod get = new GetMethod(getProp().get("pidManagerHost") + "pid/");
        HttpClient httpclient = new HttpClient();

        try {
            int statusCode = httpclient.executeMethod(get);
            assertEquals("Wrong HTTP response code", HttpStatus.SC_NOT_FOUND,
                statusCode);

            // validateXML(get.getResponseBodyAsStream(), xsd_exception);
        }
        finally {
            get.releaseConnection();
        }
    }

    /**
     * Test the Handle resource GET request if only the global Prefix is part of
     * the URL. The service has to answer with an HTTP status code of 400 (HTTP
     * Bad Request) and an XML exception body. The body is validated against the
     * schema.
     * 
     * @throws Exception
     *             Thrown if anything fails.
     */
    @Test
    public void testHandleExceptions() throws Exception {
        // 
        GetMethod get =
            new GetMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/");
        HttpClient httpclient = new HttpClient();

        try {
            int statusCode = httpclient.executeMethod(get);
            assertEquals("Wrong HTTP response code", HttpStatus.SC_BAD_REQUEST,
                statusCode);

            // get.getResponseBodyAsString()
            validateXML(get.getResponseBodyAsStream(), getExceptionSchema());
        }
        finally {
            get.releaseConnection();
        }
    }

    /**
     * Test id generator for ids based on serial numbers.
     * 
     * @throws Exception
     *             Thrown in case of any failure.
     */
    @Test
    public void testIdGeneratorSerial() throws Exception {
        // check service infopage

        String paramXml =
            "<param>\n\t<url>http://somewhere</url>\n" + "</param>\n";

        String pid = null;
        String mode = null;

        HttpClient httpclient = new HttpClient();
        PostMethod put =
            new PostMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/");
        RequestEntity requestEntity =
            new StringRequestEntity(paramXml, "text/xml", "UTF-8");
        put.setRequestEntity(requestEntity);

        try {
            int statusCode = httpclient.executeMethod(put);
            System.out.println(put.getResponseBodyAsString());
            assertEquals("Wrong HTTP response code", HttpStatus.SC_OK,
                statusCode);
            // could also be 201

            validateXML(put.getResponseBodyAsStream(), getParamSchemaLocation());
        }
        finally {
            put.releaseConnection();
        }

        DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(put.getResponseBodyAsStream());

        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("pid");
        if (nodeList.getLength() > 0) {
            Node pidNode = doc.getFirstChild();
            pid = pidNode.getTextContent();
        }

        assertNotSame(pid, "");

        // check if coldRun mode is activated
        nodeList = doc.getElementsByTagName("mode");
        if (nodeList.getLength() > 0) {
            Node modeNode = doc.getFirstChild();
            mode = modeNode.getTextContent();
        }
        if (!mode.equals("coldRun")) {
            // check if pid is in resolver
            // check if resolver has same URL
        }

        // check if pid can't recreated
        // check if new pid has different value
    }

    /**
     * Next Step Test Cases
     */
    // remove key file and test if the IOException is thrown
}
