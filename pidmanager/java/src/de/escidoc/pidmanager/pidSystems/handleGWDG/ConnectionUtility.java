/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at license/ESCIDOC.LICENSE
 * or http://www.escidoc.de/license.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at license/ESCIDOC.LICENSE.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

/*
 * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
 * fuer wissenschaftlich-technische Information mbH and Max-Planck-
 * Gesellschaft zur Foerderung der Wissenschaft e.V.  
 * All rights reserved.  Use is subject to license terms.
 */
package de.escidoc.pidmanager.pidSystems.handleGWDG;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

/**
 * An utility class for HTTP requests.<br />
 * This class uses pools HTTP connections.
 * 
 * @author SWA
 * 
 */
public class ConnectionUtility {

    private static Logger log = Logger.getLogger(ConnectionUtility.class);

    private static final int HTTP_MAX_CONNECTIONS_PER_HOST = 30;

    private static final int HTTP_MAX_TOTAL_CONNECTIONS_FACTOR = 3;

    private static final int HTTP_RESPONSE_CLASS = 100;

    private HttpClient httpClient = null;

    private MultiThreadedHttpConnectionManager cm = null;

    /**
     * Constructor.
     */
    public ConnectionUtility() {

        // know idea why this is required
        System.setProperty("org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime",
            "false");
        System.setProperty(
            "org.apache.commons.logging.simplelog.log.httpclient.wire.header",
            "warn");
        System
            .setProperty(
                "org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient",
                "warn");

        this.cm = new MultiThreadedHttpConnectionManager();
    }

    /**
     * Get a GetMethod for the URL. If the URL contains an Authentication part
     * then is this used and stored for this connection. Be aware to reset the
     * authentication if the user name and password should not be reused for
     * later connection.
     * 
     * @param url
     *            The resource URL.
     * @return GetMethod.
     * @throws Exception
     *             Thrown if connection failed.
     */
    public GetMethod getRequestURL(final URL url) throws Exception {

        String username = null;
        String password = null;

        String userinfo = url.getUserInfo();
        if (userinfo != null) {
            String[] loginValues = userinfo.split(":");
            username = loginValues[0];
            password = loginValues[1];
        }
        return getRequestURL(url, username, password);
    }

    /**
     * Get the GetMethod with authentication. The username and password is
     * stored for this connection. Later connection to same URL doesn't require
     * to set the authentication again. Be aware that this could lead to an
     * security issue! To avoid reuse reset the authentication for the URL.
     * 
     * @param url
     *            URL of resource.
     * @param username
     *            User name for authentication.
     * @param password
     *            Password for authentication.
     * @return GetMethod
     * @throws Exception
     *             Thrown if connection failed.
     */
    public GetMethod getRequestURL(
        final URL url, final String username, final String password)
        throws Exception {

        setAuthentication(url, username, password);
        return get(url.toString());
    }

    /**
     * Get the PutMethod with authentication. Username and password is stored
     * for connection. Later connections to same URL doesn't require to set
     * authentication again. Be aware that this could lead to an security issue!
     * To avoid reuse reset the authentication for the URL.
     * 
     * @param url
     *            URL of resource.
     * @param body
     *            The body of HTTP request.
     * @param username
     *            User name for authentication.
     * @param password
     *            Password for authentication.
     * @return PutMethod
     * @throws Exception
     *             Thrown if connection failed.
     */
    public PutMethod putRequestURL(
        final URL url, final String body, final String username,
        final String password) throws Exception {

        setAuthentication(url, username, password);
        return put(url.toString(), body);
    }

    /**
     * Get the PostMethod with authentication. Username and password is stored
     * for connection. Later connections to same URL doesn't require to set
     * authentication again. Be aware that this could lead to an security issue!
     * To avoid reuse reset the authentication for the URL.
     * 
     * @param url
     *            URL of resource.
     * @param body
     *            The post body of HTTP request.
     * @param username
     *            User name for authentication.
     * @param password
     *            Password for authentication.
     * @return PostMethod
     * @throws Exception
     *             Thrown if connection failed.
     */
    public PostMethod postRequestURL(
        final URL url, final String body, final String username,
        final String password) throws Exception {

        setAuthentication(url, username, password);
        return post(url.toString(), body);
    }

    /**
     * Get the PostMethod with authentication. Username and password is stored
     * for connection. Later connections to same URL doesn't require to set
     * authentication again. Be aware that this could lead to an security issue!
     * To avoid reuse reset the authentication for the URL.
     * 
     * @param url
     *            URL of resource.
     * @param parameters
     *            The post parameters (key=value).
     * @param username
     *            User name for authentication.
     * @param password
     *            Password for authentication.
     * @return PostMethod
     * @throws Exception
     *             Thrown if connection failed.
     */
    public PostMethod postRequestURL(
        final URL url, final HashMap<String, String> parameters,
        final String username, final String password) throws Exception {

        setAuthentication(url, username, password);
        return post(url.toString(), parameters);
    }

    /**
     * Get the DeleteMethod with authentication. Username and password is stored
     * for connection. Later connections to same URL doesn't require to set
     * authentication again. Be aware that this could lead to an security issue!
     * To avoid reuse reset the authentication for the URL.
     * 
     * @param url
     *            URL of resource.
     * @param username
     *            User name for authentication.
     * @param password
     *            Password for authentication.
     * @return DeleteMethod
     * @throws Exception
     *             Thrown if connection failed.
     */
    public DeleteMethod deleteRequestURL(
        final URL url, final String username, final String password)
        throws Exception {

        setAuthentication(url, username, password);
        return delete(url.toString());
    }

    /**
     * Set Authentication stuff.
     * 
     * @param url
     *            URL of resource.
     * @param username
     *            User name for authentication
     * @param password
     *            Password for authentication.
     */
    public void setAuthentication(
        final URL url, final String username, final String password) {

        if (username != null && password != null) {
            AuthScope authScope =
                new AuthScope(url.getHost(), AuthScope.ANY_PORT,
                    AuthScope.ANY_REALM);
            UsernamePasswordCredentials creds =
                new UsernamePasswordCredentials(username, password);
            getHttpClient().getState().setCredentials(authScope, creds);

            // don't wait for auth request
            getHttpClient().getParams().setAuthenticationPreemptive(true);
            // try only BASIC auth; skip to test NTLM and DIGEST
            List<String> authPrefs = new ArrayList<String>(1);
            authPrefs.add(AuthPolicy.BASIC);
            this.httpClient.getParams().setParameter(
                AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
        }
    }

    /**
     * Delete a specific authentication entry from HTTPClient.
     * 
     * @param url
     *            The URL to the resource.
     */
    public void resetAuthentication(final URL url) {

        AuthScope authScope =
            new AuthScope(url.getHost(), AuthScope.ANY_PORT,
                AuthScope.ANY_REALM);
        UsernamePasswordCredentials creds =
            new UsernamePasswordCredentials("", "");
        this.httpClient.getState().setCredentials(authScope, creds);
    }

    /**
     * Get the HTTP Client (multi threaded).
     * 
     * @return HttpClient
     */
    public HttpClient getHttpClient() {
        if (this.httpClient == null) {

            this.cm.getParams().setMaxConnectionsPerHost(
                HostConfiguration.ANY_HOST_CONFIGURATION,
                HTTP_MAX_CONNECTIONS_PER_HOST);
            this.cm.getParams().setMaxTotalConnections(
                HTTP_MAX_CONNECTIONS_PER_HOST
                    * HTTP_MAX_TOTAL_CONNECTIONS_FACTOR);
            this.httpClient = new HttpClient(this.cm);
            String proxyHost = System.getProperty("http.proxyHost");
            if (proxyHost != null) {
                String proxyPort = System.getProperty("http.proxyPort");
                log.debug("Using HTTP Proxy " + proxyHost + ":" + proxyPort);

                this.httpClient.getHostConfiguration().setProxy(proxyHost,
                    Integer.valueOf(proxyPort));
            }
        }
        return this.httpClient;
    }

    /**
     * Call the GetMethod.
     * 
     * @param url
     *            The URL for the HTTP GET method.
     * @return GetMethod
     * @throws Exception
     *             If connection failed.
     */
    private GetMethod get(final String url) throws Exception {

        GetMethod get = null;
        try {
            get = new GetMethod(url);
            int responseCode = getHttpClient().executeMethod(get);
            if ((responseCode / HTTP_RESPONSE_CLASS) != (HttpURLConnection.HTTP_OK / HTTP_RESPONSE_CLASS)) {
                get.releaseConnection();
                log.debug("Connection to '" + url
                    + "' failed with response code " + responseCode);
                throw new Exception("HTTP connection to \"" + url
                    + "\" failed.");
            }
        }
        catch (HttpException e) {
            throw new Exception(e);
        }
        catch (IOException e) {
            throw new Exception(e);
        }

        return get;
    }

    /**
     * Call the DeleteMethod.
     * 
     * @param url
     *            The URL for the HTTP DELETE method.
     * @return GetMethod
     * @throws Exception
     *             If connection failed.
     */
    private DeleteMethod delete(final String url) throws Exception {

        DeleteMethod delete = null;
        try {
            delete = new DeleteMethod(url);
            int responseCode = getHttpClient().executeMethod(delete);
            if ((responseCode / HTTP_RESPONSE_CLASS) != (HttpURLConnection.HTTP_OK / HTTP_RESPONSE_CLASS)) {
                delete.releaseConnection();
                log.debug("Connection to '" + url
                    + "' failed with response code " + responseCode);
                throw new Exception("HTTP connection failed.");
            }
        }
        catch (HttpException e) {
            throw new Exception(e);
        }
        catch (IOException e) {
            throw new Exception(e);
        }

        return delete;
    }

    /**
     * Call the PutMethod.
     * 
     * @param url
     *            The URL for the HTTP PUT request
     * @param body
     *            The body for the PUT request.
     * @return PutMethod
     * @throws Exception
     *             If connection failed.
     */
    private PutMethod put(final String url, final String body) throws Exception {

        PutMethod put = null;
        RequestEntity entity;
        try {
            entity = new StringRequestEntity(body, "text/xml", "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new Exception(e);
        }

        try {
            put = new PutMethod(url);
            put.setRequestEntity(entity);

            int responseCode = getHttpClient().executeMethod(put);
            if ((responseCode / HTTP_RESPONSE_CLASS) != (HttpURLConnection.HTTP_OK / HTTP_RESPONSE_CLASS)) {
                put.releaseConnection();
                log.debug("Connection to '" + url
                    + "' failed with response code " + responseCode);
                throw new Exception("HTTP connection failed.");
            }
        }
        catch (HttpException e) {
            throw new Exception(e);
        }
        catch (IOException e) {
            throw new Exception(e);
        }

        return put;
    }

    /**
     * Call the PostMethod.
     * 
     * @param url
     *            The URL for the HTTP POST request
     * @param body
     *            The body for the POST request.
     * @return PostMethod
     * @throws Exception
     *             If connection failed.
     */
    private PostMethod post(final String url, final String body)
        throws Exception {

        PostMethod post = null;
        RequestEntity entity;
        try {
            entity = new StringRequestEntity(body, "text/xml", "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new Exception(e);
        }

        try {
            post = new PostMethod(url);
            post.setRequestEntity(entity);

            int responseCode = getHttpClient().executeMethod(post);
            if ((responseCode / HTTP_RESPONSE_CLASS) != (HttpURLConnection.HTTP_OK / HTTP_RESPONSE_CLASS)) {
                post.releaseConnection();
                log.debug("Connection to '" + url
                    + "' failed with response code " + responseCode);
                throw new Exception("HTTP connection failed.");
            }
        }
        catch (HttpException e) {
            throw new Exception(e);
        }
        catch (IOException e) {
            throw new Exception(e);
        }

        return post;
    }

    /**
     * Call the PostMethod.
     * 
     * @param url
     *            The URL for the HTTP POST request
     * @param parameters
     *            The parameters (key=value) for the POST request.
     * @return PostMethod
     * @throws Exception
     *             If connection failed.
     */
    private PostMethod post(
        final String url, final HashMap<String, String> parameters)
        throws Exception {

        PostMethod post = null;

        try {
            post = new PostMethod(url);
            post.setRequestBody(prepareValuePairs(parameters));

            int responseCode = getHttpClient().executeMethod(post);
            if ((responseCode / HTTP_RESPONSE_CLASS) != (HttpURLConnection.HTTP_OK / HTTP_RESPONSE_CLASS)) {
                String msg = post.getResponseBodyAsString();
                post.releaseConnection();

                String errorMessage = "unknown failure";
                Pattern patternMessage =
                    Pattern
                        .compile("<body><h1>(.+)</h1>.*<p><b>type</b>(.+)</p>"
                            + "<p><b>message</b>\\s*<u>(.*)</u></p>"
                            + "<p><b>description</b>\\s*<u>(.*)</u></p>");

                Matcher m = patternMessage.matcher(msg);
                if (m.find()) {
                    errorMessage = m.group(4);
                    // try message if description is empty
                    if (errorMessage.length() == 0) {
                        errorMessage = m.group(3);
                    }
                }
                log.debug("Request to '" + url + "' failed with response code "
                    + responseCode + "and message:\n" + errorMessage);
                throw new Exception("Service request failed: " + errorMessage);
            }
        }
        catch (HttpException e) {
            throw new Exception(e);
        }
        catch (IOException e) {
            throw new Exception(e);
        }

        return post;
    }

    /**
     * Transform Map data structure to HTTP post data structure.
     * 
     * @param values
     *            Map of values.
     * @return Values as HTTP body.
     */
    private NameValuePair[] prepareValuePairs(
        final HashMap<String, String> values) {

        NameValuePair[] data = null;

        if (values != null && values.size() > 0) {
            data = new NameValuePair[values.size()];

            Iterator<String> it = values.keySet().iterator();
            int i = 0;
            while (it.hasNext()) {
                String key = it.next();
                data[i] = new NameValuePair(key, values.get(key));
                i++;
            }
        }
        return data;
    }
}
