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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Configuration.
 * 
 * @author SWA
 * 
 */
public class Configuration {

    private static Logger log = Logger.getLogger(Configuration.class);

    private static final int LOGFILE_DELAY = 60 * 1000;

    private Boolean standalone = false;

    private static final String CATALINA_HOME = "catalina.home";

    private HashSet<String> confDirsStandalone = new HashSet<String>();

    private HashSet<String> confDirsContainer = new HashSet<String>();

    private Properties prop = null;

    private String propFilename = null;

    /**
     * Configuration.
     * 
     * @param standalone
     *            Set true if service runs as stand-alone application, set to
     *            false if service is deployed within application server.
     * @throws Exception
     *             Thrown if load of configuration failed.
     */
    public Configuration(final Boolean standalone) {

        this.standalone = standalone;
        initConfigDirs();
    }

    /**
     * Configuration.
     * 
     * @param filename
     *            Name of properties file which configures the service.
     * @param standalone
     *            Set true if service runs as stand-alone application, set to
     *            false if service is deployed within application server.
     * @throws Exception
     *             Thrown if load of configuration failed.
     */
    public Configuration(final String filename, final Boolean standalone)
        throws Exception {

        this.standalone = standalone;
        initConfigDirs();
        getConfiguration(filename);
    }

    /**
     * Load the default configuration from the given file and overrides the
     * default configuration.
     * 
     * @param filename
     *            Name of properties file.
     * @throws Exception
     *             Thrown if no file could be found with provided filename.
     */
    public void getDefaultConfiguration(final String filename) throws Exception {
        this.prop = getProperties(filename);
        log.debug("Default Configuration " + filename + " loaded.");
    }

    /**
     * Load the configuration from the given file and overrides the default
     * configuration.
     * 
     * @param filename
     *            Name of properties file.
     * @throws Exception
     *             Thrown if no file could be found with provided filename.
     */
    public void getConfiguration(final String filename) throws Exception {
        this.propFilename = filename;
        InputStream in = searchingFile(filename);
        if (in != null) {
            if (this.prop == null) {
                this.prop = new Properties();
            }
            this.prop.loadFromXML(in);
            log.debug("Configuration " + filename + " loaded.");
        }
        else {
            log.debug("Config file not loaded " + filename);
        }
    }

    /**
     * Get properties from properties file with provided name.
     * 
     * @param filename
     *            Name of properties file.
     * @return Properties
     * @throws Exception
     *             Thrown if no file could be found with provided filename.
     */
    public Properties getProperties(final String filename) throws Exception {
        if (filename == null) {
            String msg = "Properties files undefined (null).";
            log.warn(msg);
            throw new Exception(msg);
        }

        InputStream inputStream = searchingFile(filename);
        Properties prop = new Properties();
        prop.loadFromXML(inputStream);
        inputStream.close();
        log.debug("Properties from " + filename + " loaded.");
        return (prop);
    }

    /**
     * Get Properties
     * 
     * @return properties
     */
    public Properties getProperties() {
        return this.prop;
    }

    /**
     * Search for file with provided filename under some locations and open
     * Stream.
     * 
     * @param fileName
     *            Name of file which is to to open.
     * @return InputStream if file could be found.
     * @throws FileNotFoundException
     *             Thrown if file with provided filename could not be found.
     */
    public InputStream searchingFile(final String fileName)
        throws FileNotFoundException {
        File file = null;
        InputStream in = null;
        HashSet<String> confDir = getConfDirs();
        Iterator<String> it = confDir.iterator();

        while (it.hasNext()) {
            String pathConfig = it.next() + fileName;
            log.info("checking " + pathConfig);

            file = new File(pathConfig);
            if (file.exists() && file.canRead()) {
                log.debug("using " + pathConfig);
                break;
            }
            else {
                file = null;
            }
        }

        if (file == null) {
            in = this.getClass().getResourceAsStream("/" + fileName);
            if (in != null) {
                log.info("using getResourceAsStream /" + fileName);
            }
            else {
                log.info(fileName + " not found");
            }
        }
        else {
            in = new FileInputStream(file);
        }
        return (in);
    }

    /**
     * Set status if application runs stand-alone or inside a Servlet-Container.
     * 
     * @param status
     *            true if stand alone false otherwise.
     */
    public void setStandalone(final Boolean status) {
        this.standalone = status;
    }

    /**
     * Get the status if application runs stand-alone or inside a
     * Servlet-Container.
     * 
     * @return true if stand alone false otherwise.
     */
    public Boolean getStandalone() {
        return (this.standalone);
    }

    /**
     * Get the directories where the service checks for properties files. The
     * path of the main configuration file (usually PidManager.properties) is
     * additionally added.
     * 
     * @return String array with paths.
     */
    public HashSet<String> getConfDirs() {
        if (this.standalone) {
            String confFile = System.getProperty("CONFIG");
            if (confFile != null) {
                File file = new File(confFile);
                // cut of filename
                String filename = file.getName();
                String path = file.getAbsolutePath();
                path = path.substring(0, path.length() - filename.length());
                this.confDirsStandalone.add(path);
            }
            return (this.confDirsStandalone);
        }
        return (this.confDirsContainer);
    }

    /**
     * Get properties value as Integer.
     * 
     * @param param
     *            Name of the parameter.
     * @return Value to the Parameter.
     */
    public int getIntValue(final String param) {
        return (Integer.parseInt(getStringValue(param)));
    }

    /**
     * Get properties value as Boolean.
     * 
     * @param param
     *            Name of the parameter.
     * @return Value to the Parameter.
     */
    public Boolean getBooleanValue(final String param) {
        if (this.prop == null) {
            log.debug("param " + param + "missing. Set default to false.");
            return (false);
        }
        String p = this.prop.getProperty(param);
        if (p == null) {
            log.debug("param " + param + "missing. Set default to false.");
            return (false);
        }
        return (Boolean.valueOf(p));
    }

    /**
     * Get properties value as String.
     * 
     * @param param
     *            Name of the parameter.
     * @return Value to the Parameter.
     */
    public String getStringValue(final String param) {
        String value = this.prop.getProperty(param);
        if (value == null) {
            log.warn("Property " + param + " not defined in file "
                + this.propFilename);
        }
        return (value);
    }

    /**
     * Set default configuration directories.
     */
    private void initConfigDirs() {
        this.confDirsStandalone.add("");
        this.confDirsStandalone.add("./");
        this.confDirsStandalone.add("/etc/pidmanager/");

        this.confDirsContainer
            .add(System.getProperty(CATALINA_HOME) + "/conf/");
        this.confDirsContainer.add(System.getProperty(CATALINA_HOME)
            + "/conf/Catalina/localhost/");
    }

    /**
     * Configure log4j.
     */
    public void configureLogging() {
        String logConfigFile =
            getStringValue(Constants.PID_MANAGER_LOG4JCONFIG);
        if ((logConfigFile != null) && (logConfigFile.length() > 0)) {
            log.warn("Loading new log config from " + logConfigFile);
            DOMConfigurator.configureAndWatch(logConfigFile, LOGFILE_DELAY);
        }
    }

}
