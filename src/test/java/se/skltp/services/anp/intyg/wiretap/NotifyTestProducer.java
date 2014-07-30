package se.skltp.services.anp.intyg.wiretap;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceProvider;

import org.xml.sax.SAXException;

@WebServiceProvider
public class NotifyTestProducer implements Provider<DOMSource> {

	@Override
	public DOMSource invoke(DOMSource request) {
		try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            DOMSource source = new DOMSource(builder.parse(new File(inputFile)));
            
            return source;
        } catch (ParserConfigurationException e) {
            throw wrap(e);
        } catch (SAXException e) {
            throw wrap(e);
        } catch (IOException e) {
            throw wrap(e);
		}
	}

	private RuntimeException wrap(Exception e) {
		return new RuntimeException();
		
	}

	public static String inputFile;
}
