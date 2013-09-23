package se.skltp.services.anp.intyg.wiretap;

import java.util.HashMap;
import java.util.Map;

import org.mule.api.MessagingException;
import org.mule.api.MuleContext;
import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.config.ExceptionHelper;
import org.mule.exception.DefaultMessagingExceptionStrategy;
import org.soitoolkit.commons.mule.api.log.EventLogMessage;
import org.soitoolkit.commons.mule.api.log.EventLogger;
import org.soitoolkit.commons.mule.log.EventLoggerFactory;

public class WiretapCustomNotifierExceptionStrategy extends DefaultMessagingExceptionStrategy {
	
	private final EventLogger eventLogger;
	
	private MuleMessage muleMessage;
	
	public boolean logNotifyPayloads = true;

	public void setLogNotifyPayloads(boolean logNotifyPayloads) {
		this.logNotifyPayloads = logNotifyPayloads;
	}

	public WiretapCustomNotifierExceptionStrategy(MuleContext muleContext) {
		super(muleContext);
		eventLogger = EventLoggerFactory.getEventLogger(muleContext);
		
	}

	@Override
	protected void logException(Throwable t) {
        
		MuleException muleException = ExceptionHelper.getRootMuleException(t);
        if (muleException != null) {
        	
        	if (muleException instanceof MessagingException) {
        		MessagingException me = (MessagingException)muleException;
        		EventLogMessage elm = new EventLogMessage();
        		elm.setMuleMessage(me.getMuleMessage());
        		
        		// Add extrainfo
        		elm.setExtraInfo(createExtraInfo());

        		Throwable ex = (me.getCause() == null ? me : me.getCause());

        		eventLogger.logErrorEvent(ex, elm);                

        	} else {
                @SuppressWarnings("unchecked")
				Map<String, Object> info = ExceptionHelper.getExceptionInfo(muleException);

                EventLogMessage elm = new EventLogMessage();
        		
        

        		if (this.muleMessage != null) {
        			elm.setMuleMessage(this.muleMessage);
        			
        			// Adding extrainfo
            		elm.setExtraInfo(createExtraInfo());
            		
            		// Set payload if necessary
            		if (info.get("Payload") == null && this.muleMessage.getPayload() != null) {
            			info.put("Payload", this.muleMessage.getPayload());
            		}
            		
        		}

        		
        		eventLogger.logErrorEvent(muleException, info.get("Payload"), elm);                
        	}
        	
        } else {
    		EventLogMessage elm = new EventLogMessage();
    		eventLogger.logErrorEvent(t, null, elm);
        }
	}
	

	@Override
	public MuleEvent handleException(Exception ex, MuleEvent event) {

		// Store the message for later usage
		this.muleMessage = event.getMessage();
		
		return super.handleException(ex, event);
	}
	
	
	
	@SuppressWarnings("deprecation")
	private Map<String, String> createExtraInfo() {
		
		Map<String,String> extraInfo = new HashMap<String, String>();
		
		if (this.muleMessage != null) {
			
			try {
				
				extraInfo.put("http.status", (String)this.muleMessage.getInboundProperty("http.status"));
				extraInfo.put("ExceptionPayload", this.muleMessage.getExceptionPayload().getMessage());
				
				
				if (this.logNotifyPayloads) {
					extraInfo.put("Payload", this.muleMessage.getPayloadAsString());
					extraInfo.put("Request", (String)this.muleMessage.getProperty("RequestPayload"));
				}

			
			} catch (Exception e) {
				logger.debug("Got a exception when setting extraInfo:" + e.getMessage());
			}
			
		} else {
			logger.debug("muleMessage is null");
		}

		
		return extraInfo;
	}
	
	

}
