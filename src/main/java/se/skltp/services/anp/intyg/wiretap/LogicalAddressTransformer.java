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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractTransformer;

// XML
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class LogicalAddressTransformer extends AbstractTransformer {

	private String newLogicalAddress;
	
	public void setNewLogicalAddress(String newLogicalAddress) {
		this.newLogicalAddress = newLogicalAddress;
	}

	@Override
	protected Object doTransform(Object src, String enc) throws TransformerException {

		// Only update the message with a new logical address if there is a new address to use
		if (newLogicalAddress != null && newLogicalAddress != "") {
			return updateValueInXml(src, newLogicalAddress);
		} else {
			
			// No new logical address, just return the original src
			return src;
		}
		
	}

	private Object updateValueInXml(Object src, String newValue) {
		
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		String xmlSrc = (String)src;
		
		String xpathExpression;
		if(xmlSrc.indexOf("LogicalAddress") > 0) {
			xpathExpression = "/*[local-name() = 'Envelope']/*[local-name() = 'Header']/*[local-name() = 'LogicalAddress']";
		} else {
			xpathExpression = "/*[local-name() = 'Envelope']/*[local-name() = 'Header']/*[local-name() = 'To']";
		}
		
		try {
			final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			final Document doc = docBuilder.parse(new InputSource(new StringReader(xmlSrc)));
			final XPath xpath = XPathFactory.newInstance().newXPath();
			
			NodeList nodes = (NodeList) xpath.evaluate(xpathExpression, doc, XPathConstants.NODESET);
			logger.debug("Number of nodes: " + nodes.getLength());
			
			
			if(nodes.getLength() == 0) {
				return src; //Return the unchanged xml
				
			} else if (nodes.getLength() == 1) {
				logger.debug("NodeValue: " + nodes.item(0).getNodeValue());
				logger.debug("NodeName: " + nodes.item(0).getNodeName());
				logger.debug("TextContent: " + nodes.item(0).getTextContent());
				
				// Replace the logical address
				nodes.item(0).setTextContent(newValue);
				
				final Transformer transformer = TransformerFactory.newInstance().newTransformer();
	            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	            
	            StreamResult result = new StreamResult(new StringWriter());
	            transformer.transform(new DOMSource(doc), result);
	          
	            return result.getWriter().toString();

			}

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (javax.xml.transform.TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return src;
	}

}
