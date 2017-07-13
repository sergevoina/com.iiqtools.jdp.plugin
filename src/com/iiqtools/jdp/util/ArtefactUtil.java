package com.iiqtools.jdp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iiqtools.jdp.annotation.EOL;

public class ArtefactUtil {

	private static class NodeInfo {
		public final int depth;
		public final String name;

		public NodeInfo(int depth, String name) {
			this.depth = depth;
			this.name = name;
		}
	}

	public static Map<String, Object> updateArtefact(IFile file, String script, ArtefactInfo artefactInfo)
			throws Exception {
		Map<String, Object> result = new HashMap<>();
		// try {
		Document document = getDocument(file);
		if (document == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Cannot open xml artefact: ").append(System.lineSeparator()).append(file.getName())
					.append(System.lineSeparator());
			throw new Exception(sb.toString());
		}

		Node node = getDocumentNode(document, artefactInfo.xpath);
		if (node == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Cannot find unique match for xpath: ").append(System.lineSeparator()).append(artefactInfo.xpath)
					.append(System.lineSeparator());
			throw new Exception(sb.toString());
		}

		node.setTextContent("");
		node.appendChild(document.createCDATASection(script));

		saveDocument(document, file, artefactInfo.eol);

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
	public static void saveDocument(Document xmlDoc, IFile file, EOL eol) throws Exception {
		// PipedInputStream is = new PipedInputStream();
		// final PipedOutputStream out = new PipedOutputStream(in);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		writeDocument(xmlDoc, os, getLineSeparator(file, eol));

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		file.setContents(is, 0, null);
	}

	public static IRegion getNodePosition(IFile file, String xpath) throws Exception {

		Assert.isNotNull(file, "file must not be null");
		Assert.isNotNull(xpath, "xpath must not be null");

		IRegion region = null;

		Document document = getDocument(file);
		if (document != null) {
			Node node = getDocumentNode(document, xpath);
			if (node != null) {
				Stack<NodeInfo> stack = getBreadcrumb(node);
				if (!stack.isEmpty()) {
					int offset = getBreadcrumbOffset(stack, file);
					if (offset != -1) {
						region = new Region(offset, 0);
					}
				}
			}
		}

		return region;
	}

	public static Node getDocumentNode(final Document xmlDoc, final String xpath) throws XPathExpressionException {
		Assert.isNotNull(xmlDoc, "xmlDoc must not be null");
		Assert.isNotNull(xpath, "xpath must not be null");

		XPathExpression expr = XPathFactory.newInstance().newXPath().compile(xpath);
		NodeList nodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);

		if (nodes != null && nodes.getLength() == 1) {
			return nodes.item(0);
		}

		return null;
	}

	public static void writeDocument(final Document xmlDoc, final OutputStream fs, String lineSeparator)
			throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DocumentType doctype = xmlDoc.getDoctype();
		if (doctype != null) {
			transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
		}

		if (lineSeparator == null) {
			lineSeparator = System.lineSeparator();
		}

		final byte[] eolBytes = lineSeparator.getBytes();

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

		// remove standalone="no" from XML declaration
		xmlDoc.setXmlStandalone(true);

		transformer.transform(new DOMSource(xmlDoc), new StreamResult(os));
	}

	public static String getLineSeparator(IFile file, EOL eol) throws CoreException, IOException {
		if (eol == null) {
			eol = EOL.Target;
		}

		String lineSeparator = null;
		if (eol == EOL.Target) {
			lineSeparator = getLineSeparator(file);
		} else {
			lineSeparator = eol.lineSeparator();
		}

		return lineSeparator;
	}

	public static String getLineSeparator(IFile file) throws CoreException, IOException {
		String eol = null;
		try (InputStream is = file.getContents()) {
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

	public static Document getDocument(IFile file) throws Exception {
		try (InputStream is = file.getContents()) {
			return getDocument(is);
		}
	}

	public static Document getDocument(InputStream is) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver() {

			@Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}
		});

		return builder.parse(is);
	}

	private static Stack<NodeInfo> getBreadcrumb(Node node) {
		Stack<NodeInfo> stack = new Stack<NodeInfo>();

		for (int depth = 0; node != null; node = node.getParentNode(), ++depth) {
			for (Node prev = node; prev != null; prev = prev.getPreviousSibling()) {
				if (prev.getNodeType() == Element.ELEMENT_NODE) {
					stack.push(new NodeInfo(depth, prev.getNodeName()));
				}
			}
		}
		return stack;
	}

	private static int getBreadcrumbOffset(Stack<NodeInfo> stack, IFile file)
			throws IOException, CoreException, XMLStreamException, FactoryConfigurationError {
		if (stack.isEmpty()) {
			return -1;
		}

		int depth = stack.peek().depth;

		try (InputStream is = file.getContents()) {
			XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(is);

			// parsing events
			while (reader.hasNext()) {
				switch (reader.getEventType()) {
				case XMLEvent.START_ELEMENT:
					if (!stack.isEmpty() && stack.peek().depth == depth) {
						NodeInfo nodeInfo = stack.pop();
						if (PluginUtil.areNotEqual(nodeInfo.name, reader.getLocalName())) {
							return -1;
						}
						if (stack.isEmpty()) {
							return reader.getLocation().getCharacterOffset();
						}
					}

					depth--;
					break;
				case XMLEvent.END_ELEMENT:
					depth++;
					break;
				}

				// next parsing event
				reader.next();
			}
		}

		return -1;
	}
}
