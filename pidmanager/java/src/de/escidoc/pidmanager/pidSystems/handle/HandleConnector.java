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
package de.escidoc.pidmanager.pidSystems.handle;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.util.Properties;

import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.DeleteHandleRequest;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.ResolutionRequest;
import net.handle.hdllib.ResolutionResponse;
import net.handle.hdllib.Util;

import org.apache.log4j.Logger;

import de.escidoc.pidmanager.Configuration;

/**
 * Connector for the local Handle resolution service. This class does the main
 * job regarding Handle service.
 * 
 * @author SWA
 * 
 */
public class HandleConnector {

    private static Logger log = Logger.getLogger(HandleConnector.class);

    private Properties prop = null;

    private HandleResolver resolver = new HandleResolver();

    private PublicKeyAuthenticationInfo auth = null;

    private PrivateKey privkey = null;

    /**
     * Connector for local Handle resolution service.
     * 
     * @param prop
     *            Connection values as Properties map.
     */
    public HandleConnector(final Properties prop) {
        this.prop = prop;
        log.debug("Loading HandleConnector.");

        this.resolver.traceMessages =
            Boolean.valueOf(this.prop
                .getProperty(HandleConstants.DEBUG_TRACE_MESSAGES));
    }

    /**
     * Retrieve values for handle from Handle resolver.
     * 
     * @param handle
     *            Handle
     * @return resolver entries
     * @throws Exception
     *             Thrown if resolving failed.
     */
    public HandleValue[] resolve(final String handle) throws Exception {
        log.debug("resolve Handle " + handle);

        HandleValue[] values = null;
        byte[] someHandle = Util.encodeString(handle);

        ResolutionRequest request =
            new ResolutionRequest(someHandle, null, null, getAuth());

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
     *             Thrown if deletion failed.
     */
    public void delete(final String handle) throws Exception {

        DeleteHandleRequest req =
            new DeleteHandleRequest(handle
                .getBytes(HandleConstants.CHAR_ENCODING), getAuth());

        AbstractResponse response = this.resolver.processRequest(req);

        if (response.responseCode == AbstractMessage.RC_HANDLE_NOT_FOUND) {
            throw new HandleNotFoundException("Handle " + handle
                + " not found.");
        }
        else if (response.responseCode != AbstractMessage.RC_SUCCESS) {
            throw new Exception(response.toString());
        }
    }

    /**
     * Update Handle.
     * 
     * @param handle
     *            Handle identifier.
     * @param values
     *            HandleValue
     * @throws Exception
     *             Thrown if update failed.
     */
    public void update(final String handle, final HandleValue[] values)
        throws Exception {

        CreateHandleRequest req =
            new CreateHandleRequest(handle
                .getBytes(HandleConstants.CHAR_ENCODING), values, getAuth());

        AbstractResponse response = this.resolver.processRequest(req);

        if (response.responseCode == AbstractMessage.RC_HANDLE_ALREADY_EXISTS) {
            throw new HandleNotFoundException("Handle " + handle
                + " already exists.");
        }
        else if (response.responseCode != AbstractMessage.RC_SUCCESS) {
            throw new Exception(response.toString());
        }
    }

    /**
     * Add values to an handle.
     * 
     * @param handle
     *            The handle.
     * @param values
     *            The new values for handle.
     * @throws Exception
     *             Thrown if adding of Handle failed.
     */
    public void add(final String handle, final HandleValue[] values)
        throws Exception {

        update(handle, values);
    }

    /**
     * Get Public Key Authentication Info.
     * 
     * @return PublicKeyAuthenticationInfo
     * @throws Exception
     *             Get authentication handle.
     */
    private PublicKeyAuthenticationInfo getAuth() throws Exception {
        if (this.auth == null) {
            String adminAuthIndex =
                this.prop
                    .getProperty(HandleConstants.RESOLVER_ADMIN_AUTH_INDEX);

            this.auth =
                new PublicKeyAuthenticationInfo(this.prop.getProperty(
                    HandleConstants.RESOLVER_ADMIN_AUTH_HANDLE).getBytes(
                    HandleConstants.CHAR_ENCODING), Integer.valueOf(
                    adminAuthIndex).intValue(), getPrivateKey());
        }

        return (this.auth);
    }

    /**
     * Get private key for authentication at Handle system.
     * 
     * @return PrivateKey
     * @throws Exception
     *             Thrown if obtaining private key failed.
     */
    private PrivateKey getPrivateKey() throws Exception {
        if (this.privkey == null) {
            byte[] key = null;
            byte[] secKey = null;

            key =
                readKey(prop
                    .getProperty(HandleConstants.RESOLVER_ADMIN_PRIV_KEYFILE));

            if (Util.requiresSecretKey(key)) {
                secKey =
                    this.prop.getProperty(
                        HandleConstants.RESOLVER_ADMIN_PASSPHRASE).getBytes(
                        HandleConstants.CHAR_ENCODING);
            }
            key = Util.decrypt(key, secKey);

            this.privkey = Util.getPrivateKeyFromBytes(key, 0);

        }

        return (this.privkey);
    }

    /**
     * Read the private admin key from file. Check different directory for
     * location.
     * 
     * @param keyFile
     *            name of key file
     * @return key as byte array
     * @throws IOException
     *             Thrown in case of key file cannot be found or has wrong
     *             content.
     */
    private byte[] readKey(final String keyFile) throws IOException {

        int n = 0;
        byte[] key = null;

        InputStream in = null;
        Boolean standalone =
            Boolean.valueOf(this.prop.getProperty("standalone"));
        Configuration conf = new Configuration(standalone);

        in = conf.searchingFile(keyFile);
        if (in == null) {
            throw new IOException("Could not load Handle key file.");
        }

        key = new byte[in.available()];
        while (n < key.length) {
            key[n++] = (byte) in.read();
        }
        in.read(key);
        in.close();

        return (key);
    }

}
