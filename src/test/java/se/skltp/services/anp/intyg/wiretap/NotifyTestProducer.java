package se.skltp.services.anp.intyg.wiretap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;

import org.soitoolkit.refapps.sd.sample.schema.v1.Sample;
import org.soitoolkit.refapps.sd.sample.schema.v1.SampleResponse;
import org.soitoolkit.refapps.sd.sample.wsdl.v1.Fault;
import org.soitoolkit.refapps.sd.sample.wsdl.v1.SampleInterface;

@WebService(serviceName = "sampleService", portName = "samplePort", targetNamespace = "urn:org.soitoolkit.refapps.sd.sample.wsdl:v1", name = "sampleService")
public class NotifyTestProducer implements SampleInterface {

	private static final Logger log = LoggerFactory.getLogger(NotifyTestProducer.class);

	public SampleResponse sample(Sample request) throws Fault {

		log.info("NotifyTestProducer received the request: {}", request);

		String id = request.getId();

        // Produce the response
		SampleResponse response = new SampleResponse();
		response.setValue("Value" + id);
		return response;
	}
}


