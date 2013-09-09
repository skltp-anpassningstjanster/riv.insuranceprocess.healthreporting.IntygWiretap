package se.skltp.services.anp.intyg.wiretap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static se.skltp.services.anp.intyg.wiretap.WiretapMuleServer.getAddress;
import static se.skltp.services.anp.intyg.wiretap.WiretapTestProducer.TEST_ID_FAULT_INVALID_ID;
import static se.skltp.services.anp.intyg.wiretap.WiretapTestProducer.TEST_ID_FAULT_TIMEOUT;
import static se.skltp.services.anp.intyg.wiretap.WiretapTestProducer.TEST_ID_OK;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.ws.soap.SOAPFaultException;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soitoolkit.commons.mule.test.AbstractJmsTestUtil;
import org.soitoolkit.commons.mule.test.ActiveMqJmsTestUtil;
import org.soitoolkit.commons.mule.test.junit4.AbstractTestCase;
import org.soitoolkit.commons.mule.util.MiscUtil;
import org.soitoolkit.commons.mule.util.RecursiveResourceBundle;
import org.soitoolkit.refapps.sd.sample.schema.v1.SampleResponse;
import org.soitoolkit.refapps.sd.sample.wsdl.v1.Fault;

 
public class WiretapIntegrationTest extends AbstractTestCase {
 
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(WiretapIntegrationTest.class);
	
 
	@SuppressWarnings("unused")
	private static final String EXPECTED_ERR_TIMEOUT_MSG = "Read timed out";
	private static final String EXPECTED_ERR_TIMEOUT_LOG_MSG = "<message>java.net.SocketTimeoutException: Read timed out</message>";
 
	private static final RecursiveResourceBundle rb = new RecursiveResourceBundle("wiretap-config");

	private static final String DEFAULT_SERVICE_ADDRESS = getAddress("WIRETAP_1_INBOUND_URL");
	private static final String WIRETAP_QUEUE = rb.getString("WIRETAP_1_QUEUE");
 
	private static final String ERROR_LOG_QUEUE = "SOITOOLKIT.LOG.ERROR";
	private AbstractJmsTestUtil jmsUtil = null;

    public WiretapIntegrationTest() {
 
        // Only start up Mule once to make the tests run faster...
        // Set to false if tests interfere with each other when Mule is started only once.
        setDisposeContextPerClass(true);
    }

	protected String getConfigResources() {
		return "soitoolkit-mule-jms-connector-activemq-embedded.xml," + 
  
		"wiretap-common.xml," +
        "wiretap-service.xml," +
		"teststub-services/wiretap-teststub-service.xml";
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
		jmsUtil.clearQueues(ERROR_LOG_QUEUE, WIRETAP_QUEUE);
    }


    @Test
    public void test_ok() throws Fault, JMSException {
    	
    	String inputFile = "src/test/resources/testfiles/wiretap/request-input.xml";
    	String expectedResultFile = "src/test/resources/testfiles/wiretap/response-expected-result.xml";

		String input = MiscUtil.readFileAsString(inputFile);
		@SuppressWarnings("unused")
		String expectedResult = MiscUtil.readFileAsString(expectedResultFile);
    	
    	
    	String id = TEST_ID_OK;
    	WiretapTestConsumer consumer = new WiretapTestConsumer(DEFAULT_SERVICE_ADDRESS);
		SampleResponse response = consumer.callService(id);
		
		// Validate response
		assertEquals("Value" + id,  response.getValue());

		// Expect no message on the error log queue
		assertEquals(0, jmsUtil.browseMessagesOnQueue(ERROR_LOG_QUEUE).size());

		// Expect one message on the wiretap queue
		List<Message> messagesOnQueue = jmsUtil.browseMessagesOnQueue(WIRETAP_QUEUE);
		assertEquals(1, messagesOnQueue.size());

		// Verify that the message on the wiretap queue is the same as the request
		assertXml(input, getFirstMessage(messagesOnQueue));
	}

    @Test
	public void test_fault_invalidInput() throws Exception {
		try {
	    	String id = TEST_ID_FAULT_INVALID_ID;
	    	WiretapTestConsumer consumer = new WiretapTestConsumer(DEFAULT_SERVICE_ADDRESS);
			Object response = consumer.callService(id);
	        fail("expected fault, but got a response of type: " + ((response == null) ? "NULL" : response.getClass().getName()));

		} catch (SOAPFaultException e) {
	    	 assertEquals("Invalid Id: " + TEST_ID_FAULT_INVALID_ID, e.getMessage());
//	    	assertEquals("Internal Server Error, code: 500", e.getMessage());
	    }
		
		// FIXME. We should get a error-log message here!
		List<Message> messagesOnErrorLogQueue = jmsUtil.browseMessagesOnQueue(ERROR_LOG_QUEUE);
		assertEquals(0, messagesOnErrorLogQueue.size());

		// Verify that the processing error caused that no message was put on the wiretap queue
		assertEquals(0, jmsUtil.browseMessagesOnQueue(WIRETAP_QUEUE).size());
	}

    @Test
	public void test_fault_timeout() throws Fault, JMSException {
        try {
	    	String id = TEST_ID_FAULT_TIMEOUT;
	    	WiretapTestConsumer consumer = new WiretapTestConsumer(DEFAULT_SERVICE_ADDRESS);
			Object response = consumer.callService(id);
	        fail("expected fault, but got a response of type: " + ((response == null) ? "NULL" : response.getClass().getName()));

        } catch (SOAPFaultException e) {
            // assertTrue("Unexpected error message: " + e.getMessage(), e.getMessage().startsWith(EXPECTED_ERR_TIMEOUT_MSG));
            assertTrue("Unexpected error message: " + e.getMessage(), e.getMessage().startsWith("Error reading XMLStreamReader."));
        }

		// Sleep for a short time period  to allow the JMS response message to be delivered, otherwise ActiveMQ data store seems to be corrupt afterwards...
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		
		// Expect one timeout error message on the error log queue
		List<Message> messagesOnErrorLogQueue = jmsUtil.browseMessagesOnQueue(ERROR_LOG_QUEUE);
		assertEquals(1, messagesOnErrorLogQueue.size());
        String errMsg = getFirstMessage(messagesOnErrorLogQueue);
		assertTrue("Unexpected error message: " + errMsg, errMsg.contains(EXPECTED_ERR_TIMEOUT_LOG_MSG));
		
		// Verify that the processing error caused that no message was put on the wiretap queue
		assertEquals(0, jmsUtil.browseMessagesOnQueue(WIRETAP_QUEUE).size());
    }
 
    private String getFirstMessage(List<Message> messagesOnQueue) throws JMSException {
		return ((TextMessage)messagesOnQueue.get(0)).getText();
	}

    private void assertXml(String expected, String actual) {

    	// We're using Xml Unit to compare results
        // Ignore whitespace and comments

    	try {
	        XMLUnit.setIgnoreWhitespace(true);
	        XMLUnit.setIgnoreComments(true);
	        
	        // Check if XSL transformation went OK
	        Diff diff = new Diff(expected, actual);

	        assertTrue("XML compare failed " + diff, diff.similar());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}