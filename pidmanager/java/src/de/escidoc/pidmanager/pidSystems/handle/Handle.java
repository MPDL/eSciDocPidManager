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

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.Common;
import net.handle.hdllib.Encoder;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.Util;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.escidoc.pidmanager.PidSystem;
import de.escidoc.pidmanager.IdGenerators.Identifier;

/**
 * Handle.
 * 
 * @author SWA
 * 
 */
public class Handle extends PidSystem {

    private static Logger log = Logger.getLogger(Handle.class);

    private static final String URI_SCHEMA = "hdl:";

    private String message = "";

    private HandleConnector hdlCon = null;

    private Properties prop = null;

    private DocumentBuilder builder = null;

    /**
     * Default Constructor (has to be there!).
     */
    public Handle() {
    }

    /**
     * 
     * @param prop
     *            Properties for Handle pid system.
     */
    public Handle(final Properties prop) {
        this.prop = prop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#setProperties(java.util.Properties)
     */
    public void setProperties(final Properties prop) {
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
        this.message =
            "<param>\n   <pid>" + URI_SCHEMA + pid + "</pid>\n</param>";
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#add(java.lang.String,
     * java.lang.String)
     */
    public void add(final String pid, final String value) throws Exception {

        getHandleConnector().add(pid, getHandleValuesFromXml(value));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#update(java.lang.String,
     * java.lang.String)
     */
    public void update(final String pid, final String value) throws Exception {

        // split up value (XML structure) to HandleValues[];
        try { // FIXME remove this
            getHandleConnector().update(pid, getHandleValuesFromXml(value));
        }
        catch (Exception e) {
            log.warn(e.toString());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#delete(java.lang.String)
     */
    public void delete(final String pid) throws Exception {

        getHandleConnector().delete(pid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#resolve(java.lang.String)
     */
    public void resolve(final String pid) throws Exception {

        HandleValue[] values = getHandleConnector().resolve(pid);
        this.message = HandleOutput.getXmlStructure(values);
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
     * Instantiate the Handle connector.
     * 
     * @return HandleConnector
     */
    private HandleConnector getHandleConnector() {

        if (this.hdlCon == null) {
            this.hdlCon = new HandleConnector(this.prop);
        }
        return (this.hdlCon);
    }

    /**
     * Get and prepare the HandleValue for Administration.
     * 
     * @param timestamp
     *            Time stamp which is to write to PID (Handle.net).
     * @return HandleValue
     * @throws UnsupportedEncodingException
     *             Thrown if Handle Authentication contain unsupported
     *             characters.
     */
    private HandleValue getAdminValue(final long timestamp)
        throws UnsupportedEncodingException {

        AdminRecord admin =
            new AdminRecord(getAuthHandle(), getAuthIndex(), true, true, true,
                true, true, true, true, true, true, true, true, true);

        HandleValue adminValue =
            new HandleValue(100, Common.STD_TYPE_HSADMIN, Encoder
                .encodeAdminRecord(admin), HandleValue.TTL_TYPE_RELATIVE,
                86400, (int) timestamp, null, true, true, true, false);

        return (adminValue);
    }

    /**
     * Get the param values as HandleValue.
     * 
     * @param value
     *            XML param structure
     * @return The parameter of param as HandleValue.
     * @throws Exception
     *             Thrown if XML parsing or mapping fails.
     */
    private HandleValue[] getHandleValuesFromXml(final String value)
        throws Exception {

        String url = null;
        String lastModDate = null;

        Document doc =
            getDocumentBuilder()
                .parse(new InputSource(new StringReader(value)));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();

        XPathExpression xPathLastModDate =
            xPath.compile("/param/@last-modification-date");
        lastModDate =
            (String) xPathLastModDate.evaluate(doc, XPathConstants.STRING);

        XPathExpression xPathUrl = xPath.compile("/param/url");
        url = (String) xPathUrl.evaluate(doc, XPathConstants.STRING);

        if (url == null) {
            String msg = "Missing URL in param.";
            log.debug(msg);
            throw new Exception(msg);
        }
        // --------------------------------------

        long timestamp = getTimestamp(lastModDate);

        HandleValue uri =
            new HandleValue(1, Common.STD_TYPE_URL, Util.encodeString(url));

        HandleValue[] values =
            new HandleValue[] { uri, getAdminValue(timestamp) };

        return (values);
    }

    /**
     * Get the timestamp for Handle from the last-modification-date or from
     * current System time.
     * 
     * @param lastModDate
     *            The last-modification-date in the DATA_FORMAT_PATTERN or null.
     *            If null then is the current system time used as timestamp.
     * @return timestamp in msec.
     * @throws ParseException
     *             Thrown if parsing of last-modification-date failed.
     */
    private long getTimestamp(final String lastModDate) throws ParseException {
        long timestamp = 0;

        if ((lastModDate != null) && (lastModDate.length() > 0)) {
            DateFormat dfm =
                new SimpleDateFormat(HandleConstants.DATE_FORMAT_PATTERN);
            Date date = dfm.parse(lastModDate);
            // Date date = Iso8601Util.parseIso8601(lastModDate);
            timestamp = date.getTime();
        }
        else {
            timestamp = System.currentTimeMillis();
        }

        return (timestamp);
    }

    /**
     * Get configured Authentication Index.
     * 
     * @return authIndex
     */
    private int getAuthIndex() {
        return (Integer.valueOf(this.prop.getProperty("admin.authIndex"))
            .intValue());
    }

    /**
     * Get Authentication Handle from properties.
     * 
     * @return adminAuthHandle
     * @throws UnsupportedEncodingException
     *             Thrown if byte transformation to selected encoding fails.
     */
    private byte[] getAuthHandle() throws UnsupportedEncodingException {
        return (this.prop
            .getProperty(HandleConstants.RESOLVER_ADMIN_AUTH_HANDLE)
            .getBytes(HandleConstants.CHAR_ENCODING));
    }

    /**
     * Create a DocumentBuilder.
     * 
     * @return DocumentBuilder.
     * @throws ParserConfigurationException
     *             If parser configuration contains failures.
     */
    private DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException {
        if (this.builder == null) {

            DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
            this.builder = factory.newDocumentBuilder();
        }

        return (this.builder);
    }

}
