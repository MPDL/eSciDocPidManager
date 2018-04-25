package de.escidoc.pidmanager.test.localHandle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

import de.escidoc.pidmanager.test.TestBase;

/**
 * Test resolve Handles.
 * 
 * @author SWA
 * 
 */
public class TestResolveHandle extends TestBase {

    /**
     * Test resolve with missing Handle suffix. Check ResponseCode and validate
     * XML.
     * 
     * @throws Exception
     *             If ResponseCode or the XMl is invalid or HTTP Request failed.
     */
    @Test
    public void testResolveMissingHandleSuffix() throws Exception {

        assertFalse("Could not check: PidManager is in coldRun mode!",
            isPidManagerColdRunMode());

        HttpClient client = new HttpClient();
        GetMethod get =
            new GetMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/");

        client.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        client.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        client.executeMethod(get);
        assertEquals("Wrong HTTP response code", HttpStatus.SC_BAD_REQUEST, get
            .getStatusCode());

        // validateXML(get.getResponseBodyAsString(), xsd_exception);
    }

    /**
     * Resolve non-existing Handle and check the response message.
     * 
     * @throws Exception
     *             Thrown if anything fails.
     */
    @Test
    public void testResolveNonExistingHandle() throws Exception {

        assertFalse("Could not check: PidManager is in coldRun mode!",
            isPidManagerColdRunMode());

        String handle = "test-handle";

        HttpClient client = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        client.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        GetMethod get =
            new GetMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/" + handle);
        client.executeMethod(get);

        assertEquals("Wrong HTTP response code", HttpStatus.SC_NOT_FOUND, get
            .getStatusCode());

        // validateXML(get.getResponseBodyAsString(), paramSchemaLocation);
    }

    /**
     * Resolve an existing Handle. The handle is created, resolved and deleted.
     * 
     * @throws Exception
     */
    @Test
    public void testResolveExistingHandle() throws Exception {

        assertFalse("Could not check: PidManager is in coldRun mode!",
            isPidManagerColdRunMode());

        String newHandle = "test-handle";

        createHandle(newHandle);

        HttpClient client = new HttpClient();
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        client.getState().setCredentials(
            new AuthScope(getHostname(), getPort(), AuthScope.ANY_REALM),
            defaultcreds);

        GetMethod get =
            new GetMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/" + newHandle);
        client.executeMethod(get);

        assertEquals("Wrong HTTP response code", HttpStatus.SC_OK, get
            .getStatusCode());

        // validateXML(get.getResponseBodyAsString(), paramSchemaLocation);

        deleteHandle(newHandle);
    }

}
