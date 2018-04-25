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
package de.escidoc.pidmanager.IdGenerators;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Response;

import de.escidoc.pidmanager.Constants;

/**
 * Connector to NOID (nice opaque identifier) service.
 * 
 * local or webservice
 * 
 * @author SWA
 * 
 */
public class NoidConnector {
    // TODO include local service

    private static Logger log = Logger.getLogger(NoidConnector.class);

    private Properties prop = null;

    /**
     * Connector to NOID minter.
     * 
     * @param prop
     *            Properties.
     */
    public NoidConnector(final Properties prop) {
        this.prop = prop;
    }

    /**
     * Get identifier from a minter service.
     * 
     * @param newMinterUrl
     *            The URL of the minter service.
     * @return id
     * @throws Exception
     *             Thrown if identifier generation failed.
     */
    public String getId(final String newMinterUrl) throws Exception {
        String id = null;

        Client client = new Client(Protocol.HTTP);
        Response response = client.get(newMinterUrl + "?mint+1");
        if (response == null) {
            throw new Exception("Connection to minter faild.");
        }
        if (response.getEntity() == null) {
            throw new Exception("No response from minter.");
        }

        id = response.getEntity().getText();
        if (id == null) {
            String msg = "Parsing Minter result faild.";
            log.error(msg);
            throw new Exception(msg);
        }
        return (id);
    }

    /**
     * Get identifier from Nice Opaque Identifier (noid).
     * 
     * @return id
     * @throws Exception
     *             Thrown if identifier generation failed.
     */
    public String getId() throws Exception {
        return (getId(this.prop.getProperty(Constants.ID_MINTER_URL)));
    }

}
