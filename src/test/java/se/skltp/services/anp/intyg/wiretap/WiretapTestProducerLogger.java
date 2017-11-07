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

import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiretapTestProducerLogger extends AbstractMessageTransformer {

	private static final Logger log = LoggerFactory.getLogger(WiretapTestProducerLogger.class);

	private static String lastSenderId = null;
	private static String lastOriginalConsumer = null;
	private static String lastVpInstance = null;
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {

		@SuppressWarnings("unchecked")
		Map<String, Object> httpHeaders = (Map<String, Object>)message.getInboundProperty("http.headers");
		
		String senderId = (String)httpHeaders.get(WiretapConstants.X_VP_SENDER_ID);
		log.info("Wiretap Test producer called with {}: {}", WiretapConstants.X_VP_SENDER_ID, senderId);
		lastSenderId = senderId;
		
		String vpInstance = (String)httpHeaders.get(WiretapConstants.X_VP_INSTANCE_ID);
		log.info("Wiretap Test producer called with {}: {}", WiretapConstants.X_VP_INSTANCE_ID, vpInstance);
		lastVpInstance = vpInstance;
		
		String orgConsumer = (String)httpHeaders.get(WiretapConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID);
		log.info("Wiretap Test producer called with {}: {}", WiretapConstants.X_RIVTA_ORIGINAL_SERVICE_CONSUMER_HSA_ID, orgConsumer);
		lastOriginalConsumer = orgConsumer;

		return message;
	}

	public static String getLastVpInstance() {
		return lastVpInstance;
	}

	public static String getLastSenderId() {
		return lastSenderId;
	}

	public static String getLastOriginalConsumer() {
		return lastOriginalConsumer;
	}

	public static void reset() {
		lastOriginalConsumer = null;
		lastSenderId = null;
		lastVpInstance = null;
	}
	
	
}