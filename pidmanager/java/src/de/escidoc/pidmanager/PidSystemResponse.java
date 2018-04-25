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

import org.restlet.data.Status;

public class PidSystemResponse {

    private Status status = Status.SERVER_ERROR_INTERNAL;

    private String messageTitle = "";

    private String message = "";

    PidSystemResponse() {
    }

    /**
     * 
     * @param msg
     */
    PidSystemResponse(final String msgTitle, final String msg) {
        this.messageTitle = msgTitle;
        this.message = msg;
    }

    /**
     * 
     * @return
     */
    public Status getStatus() {
        return status;
    }

    /**
     * 
     * @param status
     */
    public void setStatus(final int status) {
        this.status = new Status(status);
    }

    /**
     * 
     * @param status
     */
    public void setStatus(final Status status) {
        this.status = status;
    }

    /**
     * 
     * @return
     */
    public String getMessageTitle() {
        return messageTitle;
    }

    /**
     * 
     * @return
     */
    public String getMessageTitleXml() {
        return ("<title>" + this.messageTitle + "</title>\n");
    }

    /**
     * 
     * @param message
     */
    public void setMessageTitle(final String msgTitle) {
        this.messageTitle = msgTitle;
    }

    /**
     * 
     * @return
     */
    public String getMessage() {
        return message;
    }

    /**
     * 
     * @return
     */
    public String getMessageXml() {
        return ("<message>" + this.message + "</message>\n");
    }

    /**
     * 
     * @param message
     */
    public void setMessage(final String msg) {
        this.message = msg;
    }

}
