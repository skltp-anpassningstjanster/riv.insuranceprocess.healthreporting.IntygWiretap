package se.skltp.services.anp.intyg.wiretap;


import org.mule.api.MessagingException;
import org.mule.api.MuleMessage;
import org.mule.config.i18n.Message;


/**
 * Indicates that we got a specific exception related to wiretap.
 */
public class WiretapHttpException extends MessagingException {

	private static final long serialVersionUID = 6855928483109707595L;

	@SuppressWarnings("deprecation")
	public WiretapHttpException(Message message, MuleMessage msg) {
		super(message, msg);
	}

}
