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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.handle.hdllib.HandleValue;

/**
 * Out put of PidManager (data structures).
 * 
 * @author SWA
 * 
 */
public class HandleOutput {

    /**
     * Get XML structure from Map.
     * 
     * @param values
     *            Value map
     * @return Handle XML represenation (ala PidManager).
     */
    public static String createXmlStructure(final HashMap<String, String> values) {

        return (createXmlStructure(values, true));
    }

    /**
     * Creates an XML snippet of the HandleValues.
     * 
     * @param values
     *            HandleValue Array
     * @return XML snippet with handle entries
     */
    public static String getXmlStructure(final HandleValue[] values) {

        Vector<HashMap<String, String>> valuesMap =
            new Vector<HashMap<String, String>>();

        for (int i = 0; i < values.length; i++) {
            HashMap<String, String> valueMap = new HashMap<String, String>();

            valueMap.put("type", values[i].getTypeAsString());
            valueMap.put("permission", values[i].getPermissionString());
            valueMap.put("data", values[i].getDataAsString());
            valueMap.put("timestamp", values[i].getTimestampAsString());
            valueMap.put("index", Integer.toString(values[i].getIndex()));

            // TODO skip values if short format is requested
            valueMap.put("TTLType", Integer.toString(values[i].getTTLType()));
            valueMap.put("TTL", Integer.toString(values[i].getTTL()));
            valueMap.put("index", Integer.toString(values[i].getIndex()));
            valueMap.put("type", values[i].getTypeAsString());

            valuesMap.add(valueMap);
        }

        // FIXME use XML writer
        return (createXmlStructure(valuesMap));
    }

    /**
     * Creates an XML snippet from values.
     * 
     * @param values
     *            HandleValue Array
     * @return XML snippet with handle entries
     */
    public static String createXmlStructure(
        final Vector<HashMap<String, String>> values) {

        String xml = "<param>\n";

        for (int i = 0; i < values.size(); i++) {
            xml += createXmlStructure(values.get(i), false);
        }

        xml += "</param>\n";
        return (xml);
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
    public static String createXmlStructure(
        final HashMap<String, String> values, final Boolean isRoot) {

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
