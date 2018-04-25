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

import org.apache.log4j.Logger;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Resource constructor.
 * 
 * @author SWA
 * 
 */
public abstract class RestletResource extends Restlet {

	private static Logger log = Logger.getLogger(RestletResource.class);

	private final static String XML_PREAMPLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

	private final String exceptionSchema = "xmlns=\"http://www.escidoc.de/pidManager/exception/0.1\" "
			+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xsi:schemaLocation=\"http://www.escidoc.de/pidManager/exception/0.1 "
			+ " http://localhost:8080/pidmanager/static/xsd/exceptions.xsd\" ";

	/**
	 * RESTlet Resource.
	 */
	public RestletResource() {
	}

	/**
	 * RESTlet request/response handler.
	 * 
	 * @param request
	 *            HTTP request.
	 * @param response
	 *            HTTP response.
	 */
	@Override
	public void handle(final Request request, final Response response) {
		// Attention, this is not CNRI handle. This is RESTlet handle.
		PidSystemResponse resp = null;
		try {
			if (request.getMethod().equals(Method.GET)) {
				resp = httpGet(request); // resolve
			} else if (request.getMethod().equals(Method.PUT)) {
				resp = httpPut(request); // add/update
			} else if (request.getMethod().equals(Method.DELETE)) {
				resp = httpDelete(request); // delete
			} else if (request.getMethod().equals(Method.POST)) {
				resp = httpPost(request);
			} else {
				log.debug("Unsupported request method");
				resp = new PidSystemResponse("Unsupported request method",
						"Resolve without suffix not supported.");
				resp.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}

			if (resp.getStatus() == Status.SUCCESS_OK) {
				response.setStatus(resp.getStatus());
				response.setEntity(XML_PREAMPLE + resp.getMessage(),
						MediaType.TEXT_XML);
			} else {
				response.setStatus(resp.getStatus());
				response.setEntity(XML_PREAMPLE + "<exception "
						+ this.exceptionSchema + ">\n"
						+ resp.getMessageTitleXml() + resp.getMessageXml()
						+ "</exception>\n", MediaType.TEXT_XML);
			}

		} catch (Exception e) {
			// failure
			log.fatal("Internal Server Error\n\t" + e.toString());
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			response.setEntity(XML_PREAMPLE
					+ "<exception>\n\t<type>Internal Server Error</type>\n"
					+ "\t<message>" + e.getMessage() + "</message>\n"
					+ "\t<stackTrace>" + e.toString() + "</stackTrace>\n"
					+ "</exception>\n", MediaType.TEXT_XML);
		}
	}

	/**
	 * HTTP GET.
	 * 
	 * @param request
	 *            Request.
	 * @return PidSystemResponse
	 */
	abstract PidSystemResponse httpGet(Request request);

	/**
	 * HTTP PUT.
	 * 
	 * @param request
	 *            Request.
	 * @return PidSystemResponse
	 */
	abstract PidSystemResponse httpPut(Request request);

	/**
	 * HTTP DELETE.
	 * 
	 * @param request
	 *            Request.
	 * @return PidSystemResponse
	 */
	abstract PidSystemResponse httpDelete(Request request);

	/**
	 * HTTP POST.
	 * 
	 * @param request
	 *            Request.
	 * @return PidSystemResponse
	 */
	abstract PidSystemResponse httpPost(Request request);

}
