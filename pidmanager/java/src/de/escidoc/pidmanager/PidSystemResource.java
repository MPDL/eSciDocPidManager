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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.restlet.Guard;
import org.restlet.Router;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Status;

import de.escidoc.pidmanager.exceptions.IdentifierException;
import de.escidoc.pidmanager.exceptions.PidResolveException;

/**
 * PID System Resource. Setup module instances and handles requests and
 * responses.
 * 
 * @author SWA
 * 
 */
public class PidSystemResource extends RestletResource {

    private static Logger log = Logger.getLogger(PidSystemResource.class);

    private String classPath; // path to PidSystem classes

    // private URL classUrl = null;

    private PidSystem pidSystem;

    private Properties prop = null;

    private Properties globalProp = null;

    private PidSystemResponse response = new PidSystemResponse();

    /**
     * PidSystemResource creates a new PidSystemResource instance and binds the
     * configured class to handle the PID System requests.
     * 
     * @param globalProperties
     *            Global Properties (which could be extended by local
     *            properties.)
     * @param propertiesFile
     *            Name of properties file
     * @param standalone
     *            True if application runs stand-alone false otherwise. This
     *            values is used to configure the right directories for the
     *            properties files. The service lookup different directories for
     *            the properties files.
     * @throws Exception
     *             Thrown if Resource creation fails.
     */
    PidSystemResource(final Properties globalProperties,
        final String propertiesFile, final Boolean standalone) throws Exception {

        log
            .debug("Loading PidSystemResource with properties "
                + propertiesFile);
        this.globalProp = globalProperties;

        Configuration conf = new Configuration(standalone);
        this.prop = conf.getProperties(propertiesFile);
        this.classPath = this.prop.getProperty(Constants.RESOURCE_CLASS);
        // this.classUrl = new File(this.classPath).toURI().toURL();
        // URLClassLoader cl = new URLClassLoader(new URL[] { this.classUrl });
        // Class<?> c = cl.loadClass(getClassname());

        if (Boolean.valueOf(this.prop.getProperty(Constants.DEBUG_COLD_RUN,
            "false"))) {
            log.info("setup debug mode with coldRun");
            this.classPath =
                this.classPath
                    .substring(0, this.classPath.lastIndexOf(".") + 1)
                    + "debug.Coldrun";
        }
        Class<?> c = Class.forName(this.classPath);
        if (c != null) {
            this.pidSystem = (PidSystem) c.newInstance();
            this.prop.setProperty("standalone", standalone.toString());
            this.pidSystem.setProperties(this.prop);

            log.debug("PidSystemResource " + getClassname() + " created.");
        }
        else {
            log.warn("Could not find class " + this.classPath);
        }
    }

    /**
     * Attach all supported HTTP paths (including the variables) to the router.
     * 
     * @param router
     *            The RESTlet router where all paths for this resource are to
     *            attach.
     * @throws Exception
     *             Thrown if attaching of resource fails.
     */
    public void attachToRouter(final Router router) throws Exception {

        Guard guard =
            new Guard(getContext(), ChallengeScheme.HTTP_BASIC, "PidManager");
        guard.getSecrets().put(this.globalProp.getProperty("PIDManager:User"),
            this.globalProp.getProperty("PIDManager:Password").toCharArray());
        guard.setNext(this);

        Iterator<String> it = getResourcePaths().iterator();

        while (it.hasNext()) {
            String path = it.next();
            if (path != null) {
                router.attach(path, guard);
                log.info("attach the resource " + this.getClassname()
                    + " to path " + path);
            }
            else {
                log.warn("Resource " + this.getClassname()
                    + " not attached! Path is null");
            }
        }
    }

    // ---------------------------------------------------------------
    /**
     * HTTP Get Method handler.
     * 
     * @param request
     *            The Restlet Request.
     * @return PidSystemResponse
     */
    @Override
    public PidSystemResponse httpGet(final Request request) {

        if (!validateAccess(request)) {
            log.warn("Authentication failed.");
            response.setMessageTitle("Authentication failed.");
            response.setMessage("Wrong login or password.");
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return (response);
        }

        String suffix = (String) request.getAttributes().get("suffix");
        String pid = getGlobalPrefix() + getSeparator() + suffix;

        if (suffix == null) {
            log.warn("Request with suffix=null");
            response.setMessageTitle("Bad Request");
            response.setMessage("Persistent Identifier suffix is null.");
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return (response);
        }

        try {
            this.pidSystem.resolve(pid);
            response.setMessage(this.pidSystem.getMessage());
            response.setStatus(Status.SUCCESS_OK);
        }
        catch (PidResolveException pre) {
            response.setMessageTitle("Pid Resolve Exception");
            response.setMessage(pre.getMessage());
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        catch (Exception e) {
            response.setMessageTitle("Internal System Exception");
            response.setMessage(e.getMessage());
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (response);
    }

    /**
     * HTTP Put method handler.
     * 
     * @param request
     *            The Restlet Request.
     * @return PidSystemResponse
     */
    @Override
    public PidSystemResponse httpPut(final Request request) {

        if (!validateAccess(request)) {
            log.warn("Authentication failed.");
            response.setMessageTitle("Authentication failed.");
            response.setMessage("Wrong login or password.");
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return (response);
        }

        try {
            String suffix = (String) request.getAttributes().get("suffix");
            String pid = getGlobalPrefix() + getSeparator() + suffix;
            String value = request.getEntityAsDom().getText();

            this.pidSystem.update(pid, value);

            response.setMessage(this.pidSystem.getMessage());
            response.setStatus(Status.SUCCESS_OK);
        }
        catch (Exception e) {
            response.setMessageTitle("Internal System Exception");
            response.setMessage(e.getMessage());
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (response);
    }

    /**
     * HTTP Delete method handler.
     * 
     * @param request
     *            The Restlet Request.
     * @return PidSystemResponse
     */
    @Override
    public PidSystemResponse httpDelete(final Request request) {

        if (!validateAccess(request)) {
            log.warn("Authentication failed.");
            response.setMessageTitle("Authentication failed.");
            response.setMessage("Wrong login or password.");
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return (response);
        }

        try {
            String suffix = (String) request.getAttributes().get("suffix");
            String pid = getGlobalPrefix() + getSeparator() + suffix;

            this.pidSystem.delete(pid);
            response.setMessage(this.pidSystem.getMessage());
            response.setStatus(Status.SUCCESS_OK);
        }
        catch (PidResolveException pre) {
            response.setMessageTitle("Pid Resolve Exception");
            response.setMessage(pre.getMessage());
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
        catch (Exception e) {
            response.setMessageTitle("Internal System Exception");
            response.setMessage(e.getMessage());
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (response);
    }

    /**
     * HTTP Post method handler.
     * 
     * @param request
     *            The Restlet Request.
     * @return PidSystemResponse
     */
    @Override
    public PidSystemResponse httpPost(final Request request) {

        if (!validateAccess(request)) {
            log.warn("Authentication failed.");
            response.setMessageTitle("Authentication failed.");
            response.setMessage("Wrong login or password.");
            response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return (response);
        }

        try {
            String suffix = (String) request.getAttributes().get("suffix");
            String value = request.getEntityAsDom().getText();
            if (suffix == null) {
                this.pidSystem.add(value, getGlobalPrefix(), getSeparator());
            }
            else {
                String pid = getGlobalPrefix() + getSeparator() + suffix;
                this.pidSystem.add(pid, value);
            }

            response.setMessage(this.pidSystem.getMessage());
            response.setStatus(Status.SUCCESS_OK);
        }
        catch (IdentifierException ie) {
            response.setMessageTitle("Identifier Exception");
            response.setMessage(ie.getMessage());
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        catch (Exception e) {
            response.setMessageTitle("Internal System Exception");
            response.setMessage(e.getMessage());
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }

        return (response);
    }

    // ---------------------------------------------------------------
    /**
     * Get the path where the resource is accessible.
     * 
     * @return path
     */
    public String getPath() {
        return (this.prop.getProperty(Constants.RESOURCE_PATH));
    }

    /**
     * Get the class name of the resource.
     * 
     * @return class name
     */
    public String getClassname() {
        return (this.prop.getProperty(Constants.CLASSNAME));
    }

    /**
     * Get the global prefix.
     * 
     * @return global prefix
     */
    public String getGlobalPrefix() {
        return (this.prop.getProperty(Constants.GLOBAL_PREFIX, ""));
    }

    /**
     * Get separator.
     * 
     * @return get separator
     */
    public String getSeparator() {
        return (this.prop.getProperty(Constants.SEPARATOR, "/"));
    }

    /**
     * Get the paths for the Resource. These are the paths for the HTTP
     * interface like it is to define for RESTlet. These paths can also contain
     * variables.
     * 
     * @return Set of path for the resource.
     */
    public HashSet<String> getResourcePaths() {
        HashSet<String> paths = new HashSet<String>();

        String resourcesString =
            this.prop.getProperty(Constants.BINDING_RESOURCES);

        if (resourcesString != null) {
            String[] resourceList = resourcesString.split(";");

            for (int i = 0; i < resourceList.length; i++) {

                paths.add(this.prop.getProperty(Constants.RESOURCE_PATH
                    + resourceList[i]));
            }

        }
        else {
            log.info("no path for resource configured.");
        }

        return (paths);
    }

    /**
     * Validate login and password with global properties.
     * 
     * @param request
     *            The Restlet request.
     * @return true if login and password are equal, false otherwise.
     */
    private Boolean validateAccess(final Request request) {

        ChallengeResponse chalResp = request.getChallengeResponse();
        if (chalResp == null) {
            return (true);
        }

        String login = request.getChallengeResponse().getIdentifier();
        char[] passwdChar = request.getChallengeResponse().getSecret();

        if (this.globalProp == null) {
            return (false);
        }

        if (passwdChar == null) {
            return false;
        }
        String passwd = String.valueOf(passwdChar);

        if (login != null || passwd != null) {
            if (login.equals(this.globalProp.getProperty("PIDManager:User"))
                && passwd.equals(this.globalProp
                    .getProperty("PIDManager:Password"))) {
                return (true);
            }
        }
        return (false);
    }
}
