package se.skltp.services.anp.intyg.wiretap;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;

import org.junit.Test;
import org.mule.api.MuleMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import org.soitoolkit.refapps.sd.sample.schema.v1.SampleResponse;
import org.soitoolkit.refapps.sd.sample.wsdl.v1.Fault;
 
public class NotifyIntegrationTest extends AbstractTestCase {
 
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(NotifyIntegrationTest.class);
 
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("wiretap-config");

	private static final String WIRETAP_1_QUEUE = rb.getString("WIRETAP_1_QUEUE");
 
	private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";

	private static final long TEST_TIMEOUT = 5000;

	private AbstractJmsTestUtil jmsUtil = null;

    public NotifyIntegrationTest() {
 
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

	protected String getConfigResources() {
		return "soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
  
		"wiretap-common.xml," +
        "notify-service.xml," +
		"teststub-services/notify-teststub-service.xml";
    }

    @Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		doSetUpJms();
     }

	private void doSetUpJms() {
		// TODO: Fix lazy init of JMS connection et al so that we can create jmsutil in the declaration
		// (The embedded ActiveMQ queue manager is not yet started by Mule when jmsutil is delcared...)
		if (jmsUtil == null) jmsUtil = new ActiveMqJmsTestUtil();
		
		// Clear queues used for error handling
		jmsUtil.clearQueues(ERROR_LOG_QUEUE, WIRETAP_1_QUEUE);
    }

    @Test
    public void test_ok() throws Fault, JMSException {
    	
    	String inputFile = "src/test/resources/testfiles/wiretap/request-input.xml";
		String input = MiscUtil.readFileAsString(inputFile);
    	
		MuleMessage mr = dispatchAndWaitForServiceComponent("jms://" + WIRETAP_1_QUEUE + "?connector=soitoolkit-jms-connector", input, null, "wiretap-1-notify-teststub-service", TEST_TIMEOUT);
		SampleResponse response = (SampleResponse)mr.getPayload();
		
		// Validate response
		assertEquals("Value1234567890",  response.getValue());
		
		// Expect no message on the error log queue
		assertEquals(0, jmsUtil.browseMessagesOnQueue(ERROR_LOG_QUEUE).size());

		// Sleep for a short time period  to allow the JMS response message to be delivered, otherwise ActiveMQ data store seems to be corrupt afterwards...
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
		
		// Expect zero messages on the wiretap queue, i.e. that it has been consumed
		List<Message> messagesOnQueue = jmsUtil.browseMessagesOnQueue(WIRETAP_1_QUEUE);
		assertEquals(0, messagesOnQueue.size());

	}
   
}