package de.escidoc.pidmanager.test.localHandle;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.junit.Before;
import org.junit.Test;

import de.escidoc.pidmanager.test.TestBase;
import de.escidoc.pidmanager.test.common.CreatedHandles;

public class TestDeleteHandle extends TestBase {

    private CreatedHandles createdHandles = null;

    @Before
    public void setUp() throws Exception {
        if (this.createdHandles == null) {
            this.createdHandles = new CreatedHandles();
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
        int statusCode = 0;

        // work against the cache
        HttpClient httpclient = new HttpClient();
        httpclient.getParams().setAuthenticationPreemptive(true);
        Credentials defaultcreds =
            new UsernamePasswordCredentials(getUsername(), getPassword());
        httpclient
            .getState().setCredentials(
                new AuthScope("localhost", 8080, AuthScope.ANY_REALM),
                defaultcreds);

        DeleteMethod delete =
            new DeleteMethod(getProp().get("pidManagerHost") + "pid/handle/"
                + getProp().get("handlePrefix") + "/" + newHandle);

        try {
            statusCode = httpclient.executeMethod(delete);
        }
        finally {
            delete.releaseConnection();
        }

        if (HttpStatus.SC_OK != statusCode) {
            createHandle(newHandle);
        }

        deleteHandle(newHandle);

        try {
            statusCode = httpclient.executeMethod(delete);
        }
        finally {
            delete.releaseConnection();
        }

        if (HttpStatus.SC_OK == statusCode) {
            throw new Exception("Handle " + newHandle
                + " still exists after delete.");
        }
    }

}
