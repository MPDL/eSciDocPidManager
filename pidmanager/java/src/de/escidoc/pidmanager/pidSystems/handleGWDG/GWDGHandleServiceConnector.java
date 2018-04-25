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

import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.ResolutionRequest;
import net.handle.hdllib.ResolutionResponse;
import net.handle.hdllib.Util;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;

/**
 * Connector to GWDG Handle service.
 * 
 * GWDG handle service is an web service front end to the handle service.
 * 
 * @author SWA
 * 
 */
public class GWDGHandleServiceConnector {

    private static Logger log =
        Logger.getLogger(GWDGHandleServiceConnector.class);

    private Properties prop = null;

    // to resolve handles
    private HandleResolver resolver = new HandleResolver();

    private ConnectionUtility conUtil = null;

    private static final String CONFIG_SERVICE_URL = "service.url";

    private static final String CONFIG_SERVICE_USERNAME = "service.username";

    private static final String CONFIG_SERVICE_PASSWORD = "service.password";

    private String ACTION_CREATE = "write/create";

    // private String ACTION_SEARCH = "read/search";

    private static final Pattern PATTERN_PARSE_HANDLE =
        //Pattern.compile("http://hdl.handle.net/(\\d+/[^?]+)\\?noredirect.*");
        Pattern.compile("http://hdl.handle.net/([0-9A-Z.]+/[^?]+)\\?noredirect.*");

    /**
     * Connector to GWDG handle web service.
     * 
     * @param prop
     *            Connection values as Properties map.
     */
    public GWDGHandleServiceConnector(final Properties prop) {
        this.prop = prop;
        log.debug("Loading HandleConnector.");

        this.resolver.traceMessages =
            Boolean.valueOf(this.prop
                .getProperty(HandleConstants.DEBUG_TRACE_MESSAGES));

        this.conUtil = new ConnectionUtility();
    }

    /**
     * Retrieve values for handle from Handle resolver.
     * 
     * @param handle
     *            Handle
     * @return resolver entries
     * @throws Exception
     *             Thrown if retrieve failed.
     */
    public HandleValue[] resolve(final String handle) throws Exception {
        log.debug("resolve Handle " + handle);

        // use local handle service to resolve
        HandleValue[] values = null;
        byte[] someHandle = Util.encodeString(handle);

        ResolutionRequest request =
            new ResolutionRequest(someHandle, null, null, null);

        AbstractResponse response = this.resolver.processRequest(request);

        if (response.responseCode == AbstractMessage.RC_SUCCESS) {
            values = ((ResolutionResponse) response).getHandleValues();
        }
        else if (response.responseCode == AbstractMessage.RC_HANDLE_NOT_FOUND) {
            throw new HandleNotFoundException("Handle " + handle
                + " not found.");
        }

        return (values);
    }

    /**
     * Delete handle.
     * 
     * @param handle
     *            The handle.
     * @throws Exception
     *             Thrown if delete failed (ever).
     */
    public void delete(final String handle) throws Exception {

        throw new Exception(
            "Delete is not supported by Handle web service of GWDG.");
    }

    /**
     * Update handle.
     * 
     * @param handle
     *            Handle to update.
     * @param values
     *            New handle values.
     * @throws Exception
     *             Thrown if update failed.
     */
    public void update(final String handle, final HashMap<String, String> values)
        throws Exception {

        // connect GWDG web service
        // update handle
        throw new Exception("Currently not supported.");
    }

    /**
     * Add values to an handle.
     * 
     * @param handle
     *            The handle.
     * @param values
     *            The new values for handle.
     * @return created Handle
     * @throws Exception
     *             Thrown if creation or add failed.
     */
    public String add(final String handle, final HashMap<String, String> values)
        throws Exception {

        // create handle
        // connect GWDG web service
        String pid = handle;
        PostMethod post = null;
        if (handle == null) {
            // basic handle create
            String urlString =
                this.prop.getProperty(CONFIG_SERVICE_URL) + ACTION_CREATE;

            post =
                this.conUtil.postRequestURL(new URL(urlString), values,
                    this.prop.getProperty(CONFIG_SERVICE_USERNAME), this.prop
                        .getProperty(CONFIG_SERVICE_PASSWORD));

            // obtain pid from response.
            NameValuePair location = post.getResponseHeader("Location");

            // obtain pid from url
            String tmp = location.getValue();
            Matcher m = PATTERN_PARSE_HANDLE.matcher(tmp);
            if (m.find()) {
                pid = m.group(1);
            }
            else {
                throw new Exception(
                    "Failed to obtain Handle from service response.");
            }
        }
        else {
            // call verbose handle create
            throw new Exception("Missing implementation.");
        }

        return pid;
    }
}
