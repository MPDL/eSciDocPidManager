package de.escidoc.pidmanager.test.coldRun;

import static org.junit.Assert.assertEquals;

import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.w3c.dom.Document;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import de.escidoc.pidmanager.test.TestBase;

/**
 * Test resolve Handle.
 * 
 * @author SWA
 * 
 */
public class TestResolveHandle extends TestBase {

    /**
     * Test resolve of non existing handle. Check ResponseCode and validate XML.
     * 
     * @throws Exception
     *             If ResponseCode or the XMl is invalid or HTTP Request failed.
     */
    @Test
    public void testResolveNonExistingHandle() throws Exception {

        WebConversation wc = new WebConversation();
        WebRequest req =
            new GetMethodWebRequest(getProp().get("pidManagerHost")
                + "pid/handle/" + getProp().get("handlePrefix") + "/");
        WebResponse resp = wc.getResource(req);

        String text = resp.getText();
        System.out.println(text);

        assertEquals("Wrong HTTP response code", HttpStatus.SC_BAD_REQUEST,
            resp.getResponseCode());

    }

    /**
     * Resolve existing Handle and check the response message.
     * 
     * @throws Exception
     *             Thrown if anything fails.
     */
    @Test
    public void testResolveHandle() throws Exception {

        WebConversation wc = new WebConversation();
        WebRequest req =
            new GetMethodWebRequest(getProp().get("pidManagerHost")
                + "pid/handle/" + getProp().get("handlePrefix")
                + "/test-handle");
        WebResponse resp = wc.getResource(req);

        String text = resp.getText();
        System.out.println(text);

        assertEquals("Wrong HTTP response code", HttpStatus.SC_OK, resp
            .getResponseCode());

        Document doc = resp.getDOM();
        validateXML(doc, getParamSchemaLocation());
    }

}
