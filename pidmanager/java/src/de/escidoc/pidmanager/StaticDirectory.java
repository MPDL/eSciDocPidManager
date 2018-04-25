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
package de.escidoc.pidmanager;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;

/**
 * RESTlet export directory for static files.
 * 
 * @author SWA
 * 
 */
public class StaticDirectory extends Application {

    private static Logger log = Logger.getLogger(StaticDirectory.class);

    private Configuration conf = null;

    /**
     * Create RESTlet export directory for static files.
     * 
     * @param context
     *            RESTlet context.
     */
    public StaticDirectory(final Context context, Configuration conf) {
        super(context);
        this.conf = conf;
        createRoot();
    }

    /**
     * Create Root entry for directory with export of static files.
     * 
     * @return RESTlet
     */
    @Override
    public Restlet createRoot() {
        String rootUri =
            this.conf.getStringValue(Constants.PID_MANAGER_STATIC_FILES);

        if (!rootUri.startsWith(".") && !rootUri.startsWith("/")) {
            rootUri = '/' + rootUri;
        }
        Directory directory = new Directory(getContext(), "file://" + rootUri);
        directory.setListingAllowed(true);
        directory.setDeeplyAccessible(true);

        log.debug("Static directory export added to RESTlet router (" + rootUri
            + ").");

        return directory;
    }

    /**
     * Load directory server class and connect it to the router. This is skipped
     * if the system runs not stand-alone.
     * 
     * @param router
     *            RESTlet router where all resources are to connect.
     * @throws Exception
     *             Thrown if StaticDiectory class loading fails.
     */
    public void attachToRouter(final Router router) throws Exception {
        router.attach(conf
            .getStringValue(Constants.PID_MANAGER_STATIC_FILES_PATH), this);
        log.debug(this.getClass().getName() + " attached to Router.");
    }

}
