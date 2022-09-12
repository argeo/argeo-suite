package org.argeo.app.swt.docbook;

import static org.argeo.app.docbook.DbkAcrUtils.isDbk;
import static org.argeo.app.docbook.DbkType.para;
import static org.argeo.app.docbook.DbkType.title;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.argeo.api.acr.Content;
import org.argeo.app.docbook.DbkType;

/** Based on HTML with a few Wiki-like shortcuts. */
public class DbkTextInterpreter implements TextInterpreter {

	private TransformerFactory transformerFactory = TransformerFactory.newDefaultInstance();

	private String linkCssClass = DbkType.link.name();

	@Override
	public void write(Content node, String content) {
		if (isDbk(node, para) || isDbk(node, title)) {
			String raw = convertToStorage(node, content);
			validateBeforeStoring(raw);

			String jcrUuid = null;// node.getIdentifier();
//					if (node.hasProperty(Property.JCR_UUID))
//						jcrUuid = node.getProperty(Property.JCR_UUID).getString();
//					else {
//						// TODO use time based
//						jcrUuid = UUID.randomUUID().toString();
//						node.setProperty(Property.JCR_UUID, jcrUuid);
//						node.getSession().save();
//					}

			StringBuilder namespaces = new StringBuilder();
			namespaces.append(" xmlns:dbk=\"http://docbook.org/ns/docbook\"");
			namespaces.append(" xmlns:jcr=\"http://www.jcp.org/jcr/1.0\"");
			namespaces.append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
			raw = "<" + node.getName() + " jcr:uuid=\"" + jcrUuid + "\"" + namespaces + ">" + raw + "</"
					+ node.getName() + ">";
//					System.out.println(raw);
//					try (InputStream in = new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8))) {
//						node.getSession().importXML(node.getParent().getPath(), in,
//								ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
//						// node.getSession().save();
//					} catch (IOException e) {
//						throw new IllegalArgumentException("Cannot parse raw content of " + node, e);
//					}

//					try {
//						DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//						Document document;
//						try (Reader in = new StringReader(raw)) {
//							document = documentBuilder.parse(new InputSource(in));
//						}
//						NodeList nl = document.getChildNodes();
//						for (int i = 0; i < nl.getLength(); i++) {
//							org.w3c.dom.Node n = nl.item(i);
//							if (node instanceof Text) {
//
//							}
//						}
//					} catch (ParserConfigurationException | SAXException | IOException e) {
//						throw new IllegalArgumentException("Cannot parse raw content of " + node, e);
//					}

//					Node jcrText;
//					if (!node.hasNode(Jcr.JCR_XMLTEXT))
//						jcrText = node.addNode(Jcr.JCR_XMLTEXT, JcrxType.JCRX_XMLTEXT);
//					else
//						jcrText = node.getNode(Jcr.JCR_XMLTEXT);
//					jcrText.setProperty(Jcr.JCR_XMLCHARACTERS, raw);
		} else {
			throw new IllegalArgumentException("Don't know how to interpret " + node);
		}
	}

	@Override
	public String read(Content item) {
		String raw = raw(item);
		return convertFromStorage(item, raw);
	}

	@Override
	public String raw(Content node) {
		if (isDbk(node, para) || isDbk(node, title)) {
			Source source = node.adapt(Source.class);

			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);

			try {
				transformerFactory.newTransformer().transform(source, result);
				return stringWriter.toString();
			} catch (TransformerException e) {
				throw new RuntimeException("Could not convert " + node + " to XML", e);
			}

//					StringBuilder sb = new StringBuilder();
//					readXml(node, sb);
//					NodeIterator nit = node.getNodes();
//					while (nit.hasNext()) {
//						Node child = nit.nextNode();
//						if (child.getName().equals(Jcr.JCR_XMLTEXT)) {
//							Node jcrText = node.getNode(Jcr.JCR_XMLTEXT);
//							String txt = jcrText.getProperty(Jcr.JCR_XMLCHARACTERS).getString();
//							// TODO make it more robust
//							// txt = txt.replace("\n", "").replace("\t", "");
//							txt = txt.replace("\t", "  ");
//							sb.append(txt);
//						} else {
//							try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//								child.getSession().exportDocumentView(child.getPath(), out, true, false);
//								sb.append(new String(out.toByteArray(), StandardCharsets.UTF_8));
//							} catch (IOException e) {
//								throw new IllegalStateException("Cannot export " + child, e);
//							}
//						}
//					}
//					return sb.toString();
		} else {
			throw new IllegalArgumentException("Don't know how to interpret " + node);
		}
	}

//	private void readXml(Content node, StringBuilder sb){
//		
//		NodeIterator nit = node.getNodes();
//		while (nit.hasNext()) {
//			Node child = nit.nextNode();
//			if (child.getName().equals(Jcr.JCR_XMLTEXT)) {
//				String txt = child.getProperty(Jcr.JCR_XMLCHARACTERS).getString();
//				// TODO make it more robust
//				// txt = txt.replace("\n", "").replace("\t", "");
//				txt = txt.replace("\t", "  ");
//				sb.append(txt);
//			} else {
//				sb.append('<').append(child.getName());
//				PropertyIterator pit = child.getProperties();
//				properties: while (pit.hasNext()) {
//					Property p = pit.nextProperty();
//					if (p.getName().startsWith("jcr:"))
//						continue properties;
//					sb.append(' ').append(p.getName()).append("=\"").append(p.getString()).append('\"');
//				}
//				sb.append('>');
//				readXml(child, sb);
////				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
////					child.getSession().exportDocumentView(child.getPath(), out, true, false);
////					sb.append(new String(out.toByteArray(), StandardCharsets.UTF_8));
////				} catch (IOException e) {
////					throw new IllegalStateException("Cannot export " + child, e);
////				}
//				sb.append("</").append(child.getName()).append('>');
//			}
//		}
//	}

	private void readAsSimpleHtml(Content node, StringBuilder sb) {
//		NodeIterator nit = node.getNodes();
//		while (nit.hasNext()) {
//			Node child = nit.nextNode();
//			if (child.getName().equals(Jcr.JCR_XMLTEXT)) {
//				String txt = child.getProperty(Jcr.JCR_XMLCHARACTERS).getString();
//				// TODO make it more robust
//				// txt = txt.replace("\n", "").replace("\t", "");
//				txt = txt.replace("\t", "  ");
//				String html = textToSimpleHtml(txt);
//				sb.append(html);
//			} else if (child.getName().equals(DbkType.link.get())) {
//				if (child.hasProperty(DbkAttr.XLINK_HREF)) {
//					String href = child.getProperty(DbkAttr.XLINK_HREF).getString();
//					// TODO deal with other forbidden XML characters?
//					href = href.replace("&", "&amp;");
//					sb.append("<a class='" + linkCssClass + "' href='").append(href).append("'>");
//					readAsSimpleHtml(child, sb);
//					sb.append("</a>");
//				}
//			} else {
//				// ignore
//			}
//		}
	}

	private String textToSimpleHtml(String raw) {
		// FIXME the saved data should be corrected instead.
		if (raw.indexOf('&') >= 0) {
			raw = raw.replace("&", "&amp;");
		}
		if (raw.indexOf('<') >= 0) {
			raw = raw.replace("<", "&lt;");
		}
		if (raw.indexOf('>') >= 0) {
			raw = raw.replace(">", "&gt;");
		}
		if (raw.indexOf('\"') >= 0) {
			raw = raw.replace("\"", "&quot;");
		}
		if (raw.indexOf('\'') >= 0) {
			raw = raw.replace("\'", "&apos;");
		}
//		raw = "<span style='text-align:justify'>" + raw + "</span>";
		if (raw.length() == 0)
			return raw;
		try (StringReader reader = new StringReader(raw)) {
			List<String> lines = IOUtils.readLines(reader);
			if (lines.size() == 1)
				return lines.get(0);
			StringBuilder sb = new StringBuilder(raw.length() + lines.size() * BR_LENGTH);
			for (int i = 0; i < lines.size(); i++) {
				if (i != 0)
					sb.append("<br/>");
				sb.append(lines.get(i));
			}
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	final static int BR_LENGTH = "<br/>".length();

	public String readSimpleHtml(Content item) {
		StringBuilder sb = new StringBuilder();
//			sb.append("<div style='text-align: justify;'>");
		readAsSimpleHtml(item, sb);
//			sb.append("</div>");
//			System.out.println(sb);
		return sb.toString();
	}

	// EXTENSIBILITY
	/**
	 * To be overridden, in order to make sure that only valid strings are being
	 * stored.
	 */
	protected void validateBeforeStoring(String raw) {
	}

	/** To be overridden, in order to support additional formatting. */
	protected String convertToStorage(Content item, String content) {
		return content;

	}

	/** To be overridden, in order to support additional formatting. */
	protected String convertFromStorage(Content item, String content) {
		return content;
	}
}
