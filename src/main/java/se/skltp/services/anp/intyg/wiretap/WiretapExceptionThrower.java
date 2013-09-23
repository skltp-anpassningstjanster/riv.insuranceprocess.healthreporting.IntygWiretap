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
