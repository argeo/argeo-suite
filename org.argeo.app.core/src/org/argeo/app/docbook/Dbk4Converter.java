package org.argeo.app.docbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.processor.TransformerFactoryImpl;
import org.argeo.jcr.JcrException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/** Convert from DocBook v4 to DocBook v5, using the official XSL. */
public class Dbk4Converter {
	private final Templates templates;

	public Dbk4Converter() {
		try (InputStream in = getClass().getResourceAsStream("db4-upgrade.xsl")) {
			Source xsl = new StreamSource(in);
			TransformerFactory transformerFactory = new TransformerFactoryImpl();
			templates = transformerFactory.newTemplates(xsl);
		} catch (IOException | TransformerConfigurationException e) {
			throw new RuntimeException("Cannot initialise DocBook v4 converter", e);
		}
	}

	public void importXml(Node baseNode, InputStream in) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setXIncludeAware(true);
			factory.setNamespaceAware(true);
			DocumentBuilder docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(new InputSource(in));
			Source xmlInput = new DOMSource(doc);

//			ContentHandler contentHandler = baseNode.getSession().getImportContentHandler(baseNode.getPath(),
//					ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

			Transformer transformer = templates.newTransformer();
			Result xmlOutput = new StreamResult(out);
			transformer.transform(xmlInput, xmlOutput);
			try (InputStream dbk5in = new ByteArrayInputStream(out.toByteArray())) {
				baseNode.getSession().importXML(baseNode.getPath(), dbk5in,
						ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
			}
		} catch (RepositoryException e) {
			throw new JcrException("Cannot import XML to " + baseNode, e);
		} catch (TransformerException | SAXException | ParserConfigurationException e) {
			throw new RuntimeException("Cannot import DocBook v4 to " + baseNode, e);
		}

	}

	public static void main(String[] args) {
		try {

			Source xsl = new StreamSource(new File("/usr/share/xml/docbook5/stylesheet/upgrade/db4-upgrade.xsl"));
			TransformerFactory transformerFactory = new TransformerFactoryImpl();
			Templates templates = transformerFactory.newTemplates(xsl);

			File inputDir = new File(args[0]);
			File outputDir = new File(args[1]);

			for (File inputFile : inputDir.listFiles()) {
				Result xmlOutput = new StreamResult(new File(outputDir, inputFile.getName()));

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setXIncludeAware(true);
				factory.setNamespaceAware(true);
				DocumentBuilder docBuilder = factory.newDocumentBuilder();
				Document doc = docBuilder.parse(inputFile);
				Source xmlInput = new DOMSource(doc);
				Transformer transformer = templates.newTransformer();
				transformer.transform(xmlInput, xmlOutput);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
