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

import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.handle.hdllib.HandleValue;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.escidoc.pidmanager.PidSystem;
import de.escidoc.pidmanager.IdGenerators.Identifier;

/**
 * PidManager GWDG resource.
 * 
 * @author SWA
 * 
 */
public class Handle extends PidSystem {

    private static Logger log = Logger.getLogger(Handle.class);

    private static final String URI_SCHEMA = "hdl:";

    private String message = "";

    private GWDGHandleServiceConnector hdlCon = null;

    private Properties prop = null;

    private DocumentBuilder builder = null;

    /**
     * Default Constructor.
     */
    public Handle() {
    }

    /**
     * 
     * @param prop
     *            Properties
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

        String pid = null;
        if (this.prop.getProperty("IdGenerator:Mode", "service").equals(
            "service")) {
            pid =
                getServiceConnector().add(null, getHandleValuesFromXml(value));
        }
        else {
            Identifier identifier = new Identifier(this.prop);
            pid = globalPrefix + separator + identifier.generateId(null);
            getServiceConnector().add(pid, getHandleValuesFromXml(value));
        }
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

        getServiceConnector().add(pid, getHandleValuesFromXml(value));
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
            getServiceConnector().update(pid, getHandleValuesFromXml(value));
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

        getServiceConnector().delete(pid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.escidoc.pidmanager.PidSystem#resolve(java.lang.String)
     */
    public void resolve(final String pid) throws Exception {

        HandleValue[] values = getServiceConnector().resolve(pid);
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
     * Get service connector.
     * 
     * @return GWDGHandleServiceConnector
     */
    private GWDGHandleServiceConnector getServiceConnector() {

        if (this.hdlCon == null) {
            this.hdlCon = new GWDGHandleServiceConnector(this.prop);
        }
        return (this.hdlCon);
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
    private HashMap<String, String> getHandleValuesFromXml(final String value)
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

        //long timestamp = getTimestamp(lastModDate);

        HashMap<String, String> values = new HashMap<String, String>();
        values.put("url", url.toString());

        // filesize
        XPathExpression xPathP = xPath.compile("/param/filesize");
        String tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("size", tmp);
        }
        // checksum
        xPathP = xPath.compile("/param/checksum");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("checksum", tmp);
        }
        // title
        xPathP = xPath.compile("/param/title");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("title", tmp);
        }
        // authors
        xPathP = xPath.compile("/param/authors");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("authors", tmp);
        }

        // publication date
        xPathP = xPath.compile("/param/publication-date");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("pubdate", tmp);
        }
        // expiry date
        xPathP = xPath.compile("/param/expire-date");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("expdate", tmp);
        }
        // metadata URL
        xPathP = xPath.compile("/param/md-record-url");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("metadata_url", tmp);
        }
        // suffix
        xPathP = xPath.compile("/param/pid");
        tmp = (String) xPathP.evaluate(doc, XPathConstants.STRING);
        if (tmp != null && tmp.length() > 0) {
            values.put("suffix", tmp.substring(tmp.lastIndexOf("/") + 1));
        }

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
            timestamp = date.getTime();
        }
        else {
            timestamp = System.currentTimeMillis();
        }

        return (timestamp);
    }

    /**
     * Create a DocumentBuilder.
     * 
     * @return DocumentBuilder.
     * @throws ParserConfigurationException
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
