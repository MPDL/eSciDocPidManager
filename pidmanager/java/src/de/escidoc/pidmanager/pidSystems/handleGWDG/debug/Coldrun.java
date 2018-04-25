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
package de.escidoc.pidmanager.pidSystems.handleGWDG.debug;

import java.util.HashMap;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import de.escidoc.pidmanager.PidSystem;
import de.escidoc.pidmanager.IdGenerators.Identifier;

/**
 * Running PidManager module for GWDG handle service in cold run mode (no
 * entries are written to PID System).
 * 
 * @author SWA
 * 
 */
public class Coldrun extends PidSystem {

    private static Logger log = Logger.getLogger(Coldrun.class);

    private String message = "";

    private Properties prop = null;

    /**
     * Default Constructor (has to be there!).
     */
    public Coldrun() {

    }

    /**
     * Constructor.
     * 
     * @param prop
     *            Properties.
     */
    public Coldrun(final Properties prop) {
        this.prop = prop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#setProperties(java.util.Properties)
     */
    public void setProperties(Properties prop) {
        this.prop = prop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#add(java.lang.String)
     */
    public void add(
        final String value, final String globalPrefix, final String separator)
        throws Exception {

        Identifier identifier = new Identifier(this.prop);
        String pid = globalPrefix + separator + identifier.generateId(null);
        add(pid, value);
        this.message = "<param>\n   <pid>" + pid + "</pid>\n</param>";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#add(java.lang.String,
     * java.lang.String)
     */
    public void add(final String pid, final String value) throws Exception {

        log.debug("coldRun add of pid " + pid + " with value " + value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#update(java.lang.String,
     * java.lang.String)
     */
    public void update(final String pid, final String value) throws Exception {

        log.debug("coldRun update of pid " + pid + " with value " + value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#delete(java.lang.String)
     */
    public void delete(final String pid) throws Exception {

        log.debug("coldRun delete of pid " + pid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#resolve(java.lang.String)
     */
    public void resolve(final String pid) throws Exception {

        log.warn("resolve " + pid);
        HashMap<String, String> values = new HashMap<String, String>();

        values.put("pid", pid);
        values.put("type", "type value");
        values.put("permission", "-wr--r--r");
        values.put("data", "coldRun result");
        values.put("timestamp", getTimestamp("2007-11-14T08:23:010Z"));

        log.warn("create XML structure");
        this.message = createXmlStructure(values);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#getMessage()
     */
    public String getMessage() {
        return (this.message);
    }

    /**
     * Get valid XML timestamp.
     * 
     * @param timestamp
     * @return XML timestamp
     * @throws DatatypeConfigurationException
     *             If converting of timestamp to XML timestamp fail.
     */
    private String getTimestamp(final String timestamp)
        throws DatatypeConfigurationException {

        log.warn("create timestamp " + timestamp);
        XMLGregorianCalendar ts =
            DatatypeFactory.newInstance().newXMLGregorianCalendar(timestamp);

        log.warn("return timestamp");
        return (ts.toXMLFormat());
    }
}
