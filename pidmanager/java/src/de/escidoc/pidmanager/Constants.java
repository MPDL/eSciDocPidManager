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

public class Constants {

    public static final String PID_MANAGER_CONFIG_FILE =
        "PidManager.properties";

    public static final String PID_MANAGER_DEFAULT_CONFIG_FILE =
        "PidManager.default.properties";

    /**
     * Constants for properties
     */
    public static final String PID_MANAGER_LOG4JCONFIG =
        "PIDManager:LogConfigFile";

    public static final String PID_MANAGER_PORT = "PIDManager:Port";

    public static final String PID_MANAGER_STATIC_FILES =
        "PIDManager:StaticFilesPath";

    public static final String PID_MANAGER_EXPORT_STATIC_FILES =
        "PIDManager:ExportStaticFiles";

    public static final String PID_MANAGER_STATIC_FILES_PATH =
        "PIDManager:StaticFilesUrlPath";

    public static final String CLASSNAME = "ResourceClass";

    public static final String GLOBAL_PREFIX = "globalPrefix";

    public static final String SEPARATOR = "PID:separator";

    public static final String RESOURCES_DEFINITION = "Resources";

    public static final String RESOURCE_CONFIG = "Resource:";

    public static final String RESOURCE_PATH = "ResourcePath:";

    /**
     * PidSystemResource only properties
     */
    public static final String BINDING_CLASSES = "BindingClasses";

    public static final String BINDING_RESOURCES = "BindingResources";

    public static final String RESOURCE_CLASS = "ResourceClass";

    public static final String ID_GENERATOR_MODE = "IdGenerator:Mode";

    public static final String ID_GENERATOR_MODE_OVERRIDE =
        "IdGenerator:Mode.override";

    public static final String ID_GENERATOR_STORAGE = "IdGenerator:Storage";

    public static final String ID_MINTER_URL = "IdGenerator:NoidMinterURL";

    /**
     * Debug
     */
    public static final String DEBUG_COLD_RUN = "debug.coldRun";

    public static final String DEBUG_COLD_RUN_DUMMY_URL =
        "http://localhost/coldRun";

    public static final String DEBUG_IDENTIFIER = "debug.identifier";

    /**
     * Other
     */

    public static final int EXIT_CODE = 1;

    /**
     * XML param
     */
    public static final String LAST_MODIFICATION_DATE =
        "last-modification-date";

}
