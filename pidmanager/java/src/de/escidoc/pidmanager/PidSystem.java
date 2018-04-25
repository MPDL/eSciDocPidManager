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
import java.util.Properties;
import java.util.Vector;

/**
 * PID System (generic).
 * 
 * @author SWA
 * 
 */
public abstract class PidSystem {

	/**
	 * Set properties.
	 * 
	 * @param prop
	 *            Properties of PID system.
	 */
	public abstract void setProperties(Properties prop);

	/**
	 * Add PID to PID system.
	 * 
	 * @param value
	 *            Value to add.
	 * @param globalPrefix
	 *            Global PID prefix.
	 * @param separator
	 *            Separator between global prefix and suffix.
	 * @throws Exception
	 *             Thrown if adding failed.
	 */
	public abstract void add(final String value, final String globalPrefix,
			final String separator) throws Exception;

	/**
	 * Add PID to PID system.
	 * 
	 * @param pid
	 *            PID
	 * @param value
	 *            Value(s) of PID
	 * @throws Exception
	 *             Thrown if adding failed.
	 */
	public abstract void add(final String pid, final String value)
			throws Exception;

	/**
	 * Update values of PID within the PID System.
	 * 
	 * @param pid
	 *            Persistent Identifier
	 * @param value
	 *            Value (data structure) which is to update.
	 * @throws Exception
	 *             Thrown if update failed.
	 */
	public abstract void update(final String pid, final String value)
			throws Exception;

	/**
	 * Delete Persistent Identifier from PID System (This is not the standard
	 * use case for Persistent Identifier, but some systems support a delete.).
	 * 
	 * @param pid
	 *            Persistent Identifier.
	 * @throws Exception
	 *             Thrown if delete is not supported by the related PID system
	 *             or deletion failed.
	 */
	public abstract void delete(final String pid) throws Exception;

	/**
	 * Resolve a Persistent Identifier. All values are within the response
	 * message of the class.
	 * 
	 * @param pid
	 *            Persistent Identifier.
	 * @throws Exception
	 *             Thrown if resolve failed.
	 */
	public abstract void resolve(final String pid) throws Exception;

	/**
	 * Get the response message.
	 * 
	 * @return data structure of last operation.
	 */
	public abstract String getMessage();

	/**
	 * Creates an XML snippet from values.
	 * 
	 * @param values
	 *            HandleValue Array
	 * @return XML snippet with handle entries
	 */
	public String createXmlStructure(
			final Vector<HashMap<String, String>> values) {

		String xml = "<param>\n";

		for (int i = 0; i < values.size(); i++) {
			xml += createXmlStructure(values.get(i), false);
		}

		xml += "</param>\n";
		return (xml);
	}

	/**
	 * Get XML structure from Map.
	 * 
	 * @param values
	 *            Values
	 * @return XML representation of PID (as PidManager XML structure).
	 */
	public String createXmlStructure(final HashMap<String, String> values) {

		return (createXmlStructure(values, true));
	}

	/**
	 * Get XML structure from vector with value Maps.
	 * 
	 * @param values
	 *            Vector with value Maps.
	 * @param isRoot
	 *            Is the root element to add.
	 * @return XML structure of the value Map.
	 */
	public String createXmlStructure(final HashMap<String, String> values,
			final Boolean isRoot) {

		String xml = "";

		if (isRoot) {
			xml = "<param>\n";
		}
		Iterator<String> it = values.keySet().iterator();

		xml += "   <pid>\n";
		while (it.hasNext()) {
			String key = it.next();
			xml += "\t<" + key + ">" + values.get(key) + "</" + key + ">\n";
		}
		xml += "   </pid>\n";

		if (isRoot) {
			xml += "</param>\n";
		}

		return (xml);
	}
}
