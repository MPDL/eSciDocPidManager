package de.escidoc.pidmanager.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.escidoc.pidmanager.test.common.TestConfiguration;

/**
 * Test Base for PidManager test.
 * 
 * @author SWA
 * 
 */
public class TestBase {

    // public String paramSchemaLocation = pidManagerHost +
    // "static/xsd/param.xsd";
    //
    // public String xsl_style = pidManagerHost + "static/xsl/Style1.xsl";
    //
    // public String xsd_exception = pidManagerHost +
    // "static/xsd/exceptions.xsd";

    private String username = "pidsystem";

    private String passwd = "handle";

    private String hostname = "localhost";

    private int hostPort = 8080;

    private SchemaFactory sf = null;

    private HashMap<String, Schema> schemaCache = new HashMap<String, Schema>();

    private DocumentBuilder builder = null;

    private Boolean coldRunMode = null;

    private static final Pattern PATTERN_HANDLE_SUFFIX =
        Pattern.compile("hdl:[0-9]+:[0-9]+/(.)+$");

    private TestConfiguration conf = null;

    /**
     * Get the test properties.
     * 
     * @return config
     * @throws Exception
     *             Thrown if obtaining configuration failed.
     */
    public TestConfiguration getProp() throws Exception {
        if (this.conf == null) {
            this.conf = new TestConfiguration("./gwdg-test.properties");
        }
        return (this.conf);
    }

    /**
     * Get location of the XSD schema for param.
     * 
     * @return URL
     */
    public String getParamSchemaLocation() {
        return this.conf.get("PidManagerHost") + this.conf.get("schema_param");
    }

    /**
     * Get location of XML Style Sheet.
     * 
     * @return location of style sheet
     */
    public String getXslStyleLocation() {
        return this.conf.get("PidManagerHost") + this.conf.get("xsl_style_sh");
    }

    /**
     * Get location of Exception schema.
     * 
     * @return location of Exception schema
     */
    public String getExceptionSchema() {

        return this.conf.get("PidManagerHost")
            + this.conf.get("schema_exception");
    }

    /**
     * Get username of PidManager installation (which is to test).
     * 
     * @return username.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Get password for PidManager installation (which is to test).
     * 
     * @return password
     */
    public String getPassword() {
        return this.passwd;
    }

    /**
     * Get name of to test host.
     * 
     * @return host name
     */
    public String getHostname() {
        return this.hostname;
    }

    /**
     * Get port of to test host.
     * 
     * @return port
     */
    public int getPort() {
        return this.hostPort;
    }

    /**
     * Validate XML.
     * 
     * @param doc
     *            XML document to validate.
     * @param schemaUri
     *            URI of Schema.
     * @throws Exception
     *             Thrown if XML document is invalid or processing failed.
     */
    public void validateXML(final Document doc, final String schemaUri)
        throws Exception {

        Schema schema = getSchema(schemaUri);

        OutputFormat of = new OutputFormat("XML", "UTF-8", true);
        of.setIndent(1);
        of.setIndenting(true);

        XMLSerializer saxSerializer = new XMLSerializer(System.out, of);
        saxSerializer.serialize(doc);

        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(doc));
    }

    /**
     * Validate XML.
     * 
     * @param xml
     *            To validate XML document.
     * @param schemaUri
     *            URI of Schema.
     * @throws Exception
     *             Thrown if XML document is invalid or processing failed.
     */
    public void validateXML(final InputStream xml, final String schemaUri)
        throws Exception {

        validateXML(getDocumentBuilder().parse(new InputSource(xml)), schemaUri);
    }

    /**
     * Validate XML.
     * 
     * @param xml
     *            XML document to validate.
     * @param schemaUri
     *            URI of Schema.
     * @throws Exception
     *             Thrown if XML document is invalid or processing failed.
     */
    public void validateXML(final String xml, final String schemaUri)
        throws Exception {

        validateXML(getDocumentBuilder().parse(
            new InputSource(new StringReader(xml))), schemaUri);
    }

    /**
     * Get Schema. The Schema is cached an only retrieved with the first
     * request.
     * 
     * @param schemaUri
     *            The URI of the Schema.
     * @return Schema
     * @throws Exception
     *             Thrown if obtaining schema failed.
     */
    private Schema getSchema(final String schemaUri) throws Exception {

        if (sf == null) {
            sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        }

        if (!this.schemaCache.containsKey(schemaUri)) {
            URLConnection conn = new URL(schemaUri).openConnection();
            InputStream inStream = conn.getInputStream();

            System.out.println(inputStreamAsString(inStream));

            this.schemaCache.put(schemaUri, sf.newSchema(new SAXSource(
                new InputSource(inStream))));
        }

        return (this.schemaCache.get(schemaUri));
    }

    /**
     * Clear schema cache.
     */
    public void clearSchemaCache() {
        this.schemaCache.clear();
    }

    /**
     * Check if the PidManager runs in coldRun mode or not.
     * 
     * @return true if the coldRun mode of the PidManager is active, false
     *         otherwise.
     * @throws Exception
     *             Thrown if connection or obtaining coldRun value from response
     *             failed.
     */
    public Boolean isPidManagerColdRunMode() throws Exception {

        if (this.coldRunMode == null) {
            HttpClient client = new HttpClient();
            GetMethod get =
                new GetMethod(getProp().get("pidManagerHost") + "pid/handle/"
                    + getProp().get("handlePrefix") + "/123");

            client.getParams().setAuthenticationPreemptive(true);
            Credentials defaultcreds =
                new UsernamePasswordCredentials(this.username, this.passwd);
            client
                .getState().setCredentials(
                    new AuthScope(this.hostname, this.hostPort,
                        AuthScope.ANY_REALM), defaultcreds);

            client.executeMethod(get);

            Document doc =
                getDocumentBuilder().parse(
                    new InputSource(get.getResponseBodyAsStream()));

            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression xPathUrl = xpath.compile("/exception");
            String exception =
                (String) xPathUrl.evaluate(doc, XPathConstants.STRING);

            if (exception.equals("\nPid Resolve Exception\nHandle "
                + getProp().get("handlePrefix") + "/123 not found.\n")) {
                this.coldRunMode = false;
                return (this.coldRunMode);
            }

            String value =
                (String) xpath.evaluate("/param/pid/data", doc,
                    XPathConstants.STRING);

            if (value.equals("coldRun result")) {
                this.coldRunMode = true;
            }
            else {
                this.coldRunMode = false;
            }
        }

        return (this.coldRunMode);
    }

    /**
     * Convert InputStream to String.
     * 
     * @param stream
     *            The InputStream.
     * @return InputStream as String.
     * @throws IOException
     *             Thrown if read from InputStream fails.
     */
    public static String inputStreamAsString(final InputStream stream)
        throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = null;

        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }

        return sb.toString();
    }

    /**
     * Create a DocumentBuilder.
     * 
     * @return DocumentBuilder.
     * @throws ParserConfigurationException
     *             If parser configuration contains errors.
     */
    private DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException {
        if (this.builder == null) {

            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            this.builder = factory.newDocumentBuilder();
        }

        return (this.builder);
    }

    /**
     * Create a new handle.
     * 
     * @param newHandle
     *            The new identifier of the handle.
     * @throws Exception
     *             Thrown if creation of new hadnle fails.
     */
    public void createHandle(final String newHandle) throws Exception {

        String paramXml =
            "<param>\n\t<url>test-handle : " + newHandle + "</url>\n"
                + "</param>\n";

        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(this.username, this.passwd);
        httpclient.getState().setCredentials(
            new AuthScope(this.hostname, this.hostPort, AuthScope.ANY_REALM),
            defaultcreds);

        PutMethod put =
            new PutMethod(getProp().get("pidManagerHost") + "pid/handle/10168/"
                + newHandle);
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
     * Delete a Handle.
     * 
     * @param handle
     *            The handle which is to delete.
     * @throws Exception
     *             Thrown if deletion of handle fails.
     */
    public void deleteHandle(final String handle) throws Exception {

        HttpClient httpclient = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(this.username, this.passwd);
        httpclient.getState().setCredentials(
            new AuthScope(this.hostname, this.hostPort, AuthScope.ANY_REALM),
            defaultcreds);

        DeleteMethod delete =
            new DeleteMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/" + handle);

        try {
            int statusCode = httpclient.executeMethod(delete);
            assertTrue("Wrong HTTP response code. Expected " + HttpStatus.SC_OK
                + " or " + HttpStatus.SC_NOT_FOUND + " but got " + statusCode,
                (statusCode == HttpStatus.SC_OK)
                    || (statusCode == HttpStatus.SC_NOT_FOUND));
        }
        finally {
            delete.releaseConnection();
        }
    }

    /**
     * Resolve the handle.
     * 
     * @param handle
     *            The handle which is to resolve.
     * @return The entries from the handle resolver system.
     * @throws Exception
     *             Thrown if resolving fails.
     */
    public String resolveHandle(final String handle) throws Exception {

        HttpClient client = new HttpClient();
        // FIXME read prefix from config
        GetMethod get =
            new GetMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/" + handle);

        client.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds =
            new UsernamePasswordCredentials(this.username, this.passwd);
        client.getState().setCredentials(
            new AuthScope(this.hostname, this.hostPort, AuthScope.ANY_REALM),
            defaultcreds);

        int statusCode = client.executeMethod(get);
        assertEquals("Wrong HTTP response code", HttpStatus.SC_OK, statusCode);
        return (get.getResponseBodyAsString());
    }

    /**
     * Get suffix from Handle.
     * 
     * @param handle
     *            The handle.
     * @return The suffix of the handle.
     */
    public String getSuffix(final String handle) {

        String suffix = null;
        Matcher m = PATTERN_HANDLE_SUFFIX.matcher(handle);

        if (m.find()) {
            suffix = m.group(1);
        }

        return (suffix);
    }

}
