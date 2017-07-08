package com.iiqtools.jdp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iiqtools.jdp.annotation.EOL;

public class ArtefactUtil {
	public static Map<String, Object> updateArtefact(File file, String xpath, String script, EOL eol) throws Exception {
		Map<String, Object> result = new HashMap<>();
		// try {
		Document document = getDocument(file);

		XPathExpression expr = XPathFactory.newInstance().newXPath().compile(xpath);
		NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);

		int length = 0;
		if (nodes != null) {
			length = nodes.getLength();
		}
		if (length != 1) {
			StringBuilder sb = new StringBuilder();
			sb.append("Cannot find unique match for xpath: ").append(System.lineSeparator()).append(xpath)
					.append(System.lineSeparator()).append("Found ").append(length).append(" matches.");
			throw new Exception(sb.toString());
		}

		Node node = nodes.item(0);

		node.setTextContent("");
		node.appendChild(document.createCDATASection(script));

		saveDocument(document, file, eol);

		result.put("status", "OK");
		// } catch (Exception e) {
		// result.put("status", "FAILED");
		// result.put("message", e.getMessage());
		// }

		return result;
	}

	/**
	 * To save the Document in xml file
	 * 
	 * @param xmlDoc
	 * @param filePath
	 * @throws Exception
	 * @throws TransformerException
	 */
	public static void saveDocument(Document xmlDoc, File file, EOL eol) throws Exception {
		DOMSource source = new DOMSource(xmlDoc);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DocumentType doctype = xmlDoc.getDoctype();
		if (doctype != null) {
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
		}

		if (eol == null) {
			eol = EOL.Target;
		}

		String lineSeparator = null;
		if (eol == EOL.Target) {
			lineSeparator = getLineSeparator(file);
		} else {
			lineSeparator = eol.lineSeparator();
		}

		if (lineSeparator == null) {
			lineSeparator = System.lineSeparator();
		}

		final byte[] eolBytes = lineSeparator.getBytes();

		try (final OutputStream fs = new FileOutputStream(file)) {
			OutputStream os = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					if (b == '\r') {
						// ignore
					} else if (b == '\n') {
						for (byte c : eolBytes) {
							fs.write(c);
						}
					} else {
						fs.write(b);
					}
				}
			};

			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);
		}
	}

	private static String getLineSeparator(File file) throws FileNotFoundException, IOException {
		String eol = null;
		try (InputStream is = new FileInputStream(file)) {
			int ch = is.read();
			while (ch != -1) {
				if (ch == '\n') {
					eol = "\n";
					break;
				}

				boolean cr = (ch == '\r');
				ch = is.read();

				if (cr && (ch == '\n')) {
					eol = "\r\n";
					break;
				}
			}
		}
		return eol;
	}

	public static Document getDocument(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}
		});
		return builder.parse(file);
	}
}
