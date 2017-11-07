/**
 * Copyright (c) 2014 Inera AB, <http://inera.se/>
 *
 * This file is part of SKLTP.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package se.skltp.services.anp.intyg.wiretap;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

public class WiretapExceptionThrower implements Callable {

	/**
	 * Only used for throwing WiretapHttpException 
	 * 
	 * Throws always an WiretapHttpException to indicate that we got a status code >= 400
	 * @throws WiretapHttpException
	 */
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		
		String status = eventContext.getMessage().getInboundProperty("http.status");
		String message = "Wiretap got a http error with status code: " + status;
		
		Message msg = MessageFactory.createStaticMessage(message);
		
		throw new WiretapHttpException(msg, eventContext.getMessage());
	}

}
