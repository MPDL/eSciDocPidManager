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

import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

/**
 * PidManager - a PID system web service.
 * 
 * @author SWA
 * 
 */
public class PidManager extends Application {

    private static Logger log = Logger.getLogger(PidManager.class);

    private Configuration config = null;

    /**
     * PidManager (for stand-alone service).
     * 
     * @param args
     *            command line argument list
     */
    public static void main(final String[] args) {
        // configure log4j
        BasicConfigurator.configure();

        Configuration config = new Configuration(true);
        try {
            config.getDefaultConfiguration(getDefaultConfFileName());
            config.getConfiguration(getConfFileName());
        }
        catch (Exception e) {
            log.fatal(e);
            log.fatal("Abort application");
            System.exit(Constants.EXIT_CODE);
        }

        Component component = new Component();
        component.getServers().add(Protocol.HTTP,
            config.getIntValue(Constants.PID_MANAGER_PORT));
        component.getClients().add(Protocol.FILE);

        PidManager application = new PidManager(component.getContext(), config);

        component.getDefaultHost().attach(application);
        try {
            component.start();
        }
        catch (Exception e) {
            log.fatal("Unable to start service: " + e.toString());
            try {
                application.stop();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
            System.exit(Constants.EXIT_CODE);
        }
    }

    /**
     * PidManager constructor if the application run inside a servlet
     * container).
     * 
     * @param context
     *            RESTlet Context object.
     */
    public PidManager(final Context context) {
        super(context);
        this.config = new Configuration(false);
        // this.config.configureLogging();
        try {
            this.config.getDefaultConfiguration(getDefaultConfFileName());
            this.config.getConfiguration(getConfFileName());
        }
        catch (Exception e) {
            log.fatal(e);
            try {
                this.stop();
            }
            catch (Exception e1) {
                e1.printStackTrace();
            }
            System.exit(Constants.EXIT_CODE);
        }
    }

    /**
     * PidManager constructor if the applications runs stand-alone.
     * 
     * @param context
     *            RESTlet Context object.
     * @param config
     *            The configuration from main method.
     */
    public PidManager(final Context context, final Configuration config) {
        super(context);
        this.config = config;
        this.config.configureLogging();
    }

    /**
     * Creating the RESTlet root and attaching all configured resources.
     * 
     * @return Restlet
     */
    @Override
    public Restlet createRoot() {
        log.debug("create Root");
        Router router = new Router(getContext());

        // attach configured resources
        HashMap<String, String> resources = getResources();
        Iterator<String> it = resources.keySet().iterator();
        while (it.hasNext()) {
            String res = it.next();
            try {
                log.debug("load resource and attach: " + res
                    + " with properties: " + resources.get(res));
                PidSystemResource resource =
                    new PidSystemResource(this.config.getProperties(),
                        resources.get(res), this.config.getStandalone());
                resource.attachToRouter(router);
            }
            catch (Exception e) {
                log.warn("\n==========================================\n"
                    + "Resource not attached. " + e.toString()
                    + "\n==========================================");
            }
        }

        // attach static file exporter
        if (this.config.getStandalone()
            && this.config
                .getBooleanValue(Constants.PID_MANAGER_EXPORT_STATIC_FILES)) {
            StaticDirectory statDir =
                new StaticDirectory(getContext(), this.config);
            try {
                statDir.attachToRouter(router);
            }
            catch (Exception e) {
                log.warn("Export of directory with static files failed. "
                    + e.toString());
            }
        }

        return (router);
    }

    /**
     * Get the name of the global configuration file (properties) which is
     * either set via command line parameter or the default properties file
     * name.
     * 
     * @return name of global configuration file
     */
    private static String getConfFileName() {
        String confFile = System.getProperty("CONFIG");
        if (confFile == null) {
            confFile = Constants.PID_MANAGER_CONFIG_FILE;
        }
        return (confFile);
    }

    /**
     * Get the name of the global configuration file (properties) which is
     * either set via command line parameter or the default properties file
     * name.
     * 
     * @return name of global configuration file
     */
    private static String getDefaultConfFileName() {
        return (Constants.PID_MANAGER_DEFAULT_CONFIG_FILE);
    }

    /**
     * Get Map with the HTTP resources as couple of name of resource and name of
     * the properties file.
     * 
     * @return Map with name of resource and name of properties file
     */
    private HashMap<String, String> getResources() {
        HashMap<String, String> resources = new HashMap<String, String>();

        String resourcesString =
            this.config.getStringValue(Constants.RESOURCES_DEFINITION);

        if (resourcesString != null) {
            String[] resourceList = resourcesString.split(",");

            for (int i = 0; i < resourceList.length; i++) {
                // < name of resource, name of properties file>
                resources.put(resourceList[i],
                    this.config.getStringValue(Constants.RESOURCE_CONFIG
                        + resourceList[i]));
            }

        }
        else {
            log.info("no resources configured");
        }
        return (resources);
    }
}
