package org.argeo.suite.library;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.argeo.util.DigestUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** Parses a .docx document, trying its best to extract text and table data. */
public class DocxExtractor {
	final static String T = "t";
	final static String TC = "tc";
	final static String TR = "tr";
	final static String TBL = "tbl";
	final static String P = "p";
	static boolean debug = false;

	final static String PROOF_ERR = "proofErr";
	final static String TYPE = "type";
	final static String SPELL_START = "spellStart";
	final static String SPELL_END = "spellEnd";

	protected List<Tbl> tables = new ArrayList<>();
	protected List<String> text = new ArrayList<>();
	protected Map<String, byte[]> media = new TreeMap<>();
	private Set<String> mediaDigests = new HashSet<>();

	protected void processTextItem(List<String> lines, String str) {
		lines.add(str);
	}

	protected boolean skipMedia(String digest) {
		return false;
	}

	class DocxHandler extends DefaultHandler {

		private StringBuilder buffer = new StringBuilder();
		private Tbl currentTbl = null;

		boolean inSpellErr = false;
		boolean inParagraph = false;

		@Override
		public void startElement(String uri, String name, String qName, Attributes attributes) throws SAXException {
			// System.out.println(localName + " " + qName + " " + uri.hashCode());
			if (P.equals(name)) {
				if (debug && currentTbl == null)
					System.out.println("# START PARA");
				inParagraph = true;
			} else if (PROOF_ERR.equals(name)) {
				String type = attributes.getValue(uri, TYPE);
				if (SPELL_START.equals(type))
					inSpellErr = true;
				else if (SPELL_END.equals(type))
					inSpellErr = false;

			} else if (TBL.equals(name)) {
				if (currentTbl != null) {
					Tbl childTbl = new Tbl();
					childTbl.parentTbl = currentTbl;
					currentTbl = childTbl;
					// throw new IllegalStateException("Already an active table");
				} else {
					currentTbl = new Tbl();
				}
			}
		}

		@Override
		public void endElement(String uri, String name, String qName) throws SAXException {
			if (name.equals(T)) {
//				if (inSpellErr) {
//					// do not reset the buffer
//					return;
//				}

				if (currentTbl != null) {
					currentTbl.appendText(buffer.toString());
				} else {
					String str = buffer.toString();
					// replace NO-BREAK SPACE by regular space.
					str = str.replace('\u00A0', ' ');
					str = str.strip();
					if (!"".equals(str)) {
						processTextItem(text, str);
					}
				}
			} else if (name.equals(P)) {
				if (debug && currentTbl == null)
					System.out.println("# END PARA");
				if (currentTbl != null) {
					currentTbl.currentRow.current.text.append('\n');
				} else {

				}
				inParagraph = false;
			} else if (name.equals(TC)) {
				if (currentTbl != null)
					currentTbl.closeColumn();
			} else if (name.equals(TR)) {
				if (currentTbl != null)
					currentTbl.closeRow();
			} else if (name.equals(TBL)) {
				if (currentTbl != null) {
					tables.add(currentTbl);
					if (currentTbl.parentTbl != null)
						currentTbl = currentTbl.parentTbl;
					else
						currentTbl = null;
				} else {
					throw new IllegalStateException("Closing a table while none was open.");
				}
			}
			// reset the buffer
			buffer.setLength(0);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			buffer.append(ch, start, length);
		}

	}

	public static class Tbl {
		Tbl parentTbl = null;
		Tr currentRow = new Tr();
		List<Tr> rows = new ArrayList<>();

		void appendText(String str) {
			currentRow.current.text.append(str);
		}

		void closeColumn() {
			currentRow.columns.add(currentRow.current);
			currentRow.current = new Tc();
		}

		void closeRow() {
			rows.add(currentRow);
			currentRow = new Tr();
		}

		public List<Tr> getRows() {
			return rows;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Tr tr : rows) {
				String txt = tr.toString();
				sb.append(txt).append('\n');
			}
			return sb.toString();
		}
	}

	public static class Tr {
		Tc current = new Tc();
		List<Tc> columns = new ArrayList<>();

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (Tc tc : columns) {
				sb.append("\"").append(tc.toString()).append("\"").append(',');
			}
			return sb.toString();
		}

		public List<Tc> getColumns() {
			return columns;
		}

	}

	public static class Tc {
		StringBuilder text = new StringBuilder();

		@Override
		public String toString() {
			return text.toString().trim();
		}

	}

	protected void parse(Reader in) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			SAXParser saxParser = spf.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new DocxHandler());
			xmlReader.parse(new InputSource(in));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			throw new RuntimeException("Cannot parse document", e);
		}
	}

	public List<String> getText() {
		return text;
	}

	public List<Tbl> getTables() {
		return tables;
	}

	public Map<String, byte[]> getMedia() {
		return media;
	}

	public void load(ZipInputStream zIn) {
		try {
			ZipEntry entry = null;
			while ((entry = zIn.getNextEntry()) != null) {
				if ("word/document.xml".equals(entry.getName())) {
					try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
						byte[] buffer = new byte[2048];
						int len = 0;
						while ((len = zIn.read(buffer)) > 0) {
							out.write(buffer, 0, len);
						}
						try (Reader reader = new InputStreamReader(new ByteArrayInputStream(out.toByteArray()),
								StandardCharsets.UTF_8)) {
							parse(reader);
						}
					}
				} else if (entry.getName().startsWith("word/media")) {
					String fileName = entry.getName().substring(entry.getName().lastIndexOf('/') + 1);
					int dotIndex = fileName.lastIndexOf('.');
					String ext = fileName.substring(dotIndex + 1).toLowerCase();
					// we ignore .jfif
					if ("jpeg".equals(ext))
						ext = "jpg";
					fileName = fileName.substring(0, dotIndex) + "." + ext;
					switch (ext) {
					case "png":
					case "jpg":
					case "gif":
					case "bmp":
					case "tiff":
						try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
							byte[] buffer = new byte[2048];
							int len = 0;
							while ((len = zIn.read(buffer)) > 0) {
								out.write(buffer, 0, len);
							}
							byte[] bytes = out.toByteArray();
							String digest = DigestUtils.digest(DigestUtils.MD5, bytes);
							if (skipMedia(digest))
								break;
							if (!mediaDigests.contains(digest)) {
								media.put(fileName, bytes);
								mediaDigests.add(digest);
							}
						}
						break;
					default:
						break;
					}
				} else {
					// System.out.println(entry.getName());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// throw new IllegalArgumentException("No document.xml found");

	}

//	public static Reader extractDocumentXml(ZipInputStream zIn) throws IOException {
//		ZipEntry entry = null;
//		while ((entry = zIn.getNextEntry()) != null) {
//			if ("word/document.xml".equals(entry.getName())) {
//				try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//					byte[] buffer = new byte[2048];
//					int len = 0;
//					while ((len = zIn.read(buffer)) > 0) {
//						out.write(buffer, 0, len);
//					}
//					return new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), StandardCharsets.UTF_8);
//				}
//			} else {
//				System.out.println(entry.getName());
//			}
//		}
//		throw new IllegalArgumentException("No document.xml found");
//	}

//	protected static ZipInputStream openAsZip(String file) throws IOException {
//		ZipInputStream zIn;
//		Path path = Paths.get(file);
//		zIn = new ZipInputStream(Files.newInputStream(path));
//		return zIn;
//	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0)
			throw new IllegalArgumentException("Provide a file path");
		Path p = Paths.get(args[0]);

		DocxExtractor importer = new DocxExtractor();
		try (ZipInputStream zIn = new ZipInputStream(Files.newInputStream(p))) {
			importer.load(zIn);
		}
		// display
		System.out.println("## TEXT");
		for (int i = 0; i < importer.text.size(); i++) {
			String str = importer.text.get(i);
			System.out.println(str);
		}

		System.out.println("\n");

		for (int i = 0; i < importer.tables.size(); i++) {
			Tbl tbl = importer.tables.get(i);
			System.out.println("## TABLE " + i);
			System.out.println(tbl);
		}

		System.out.println("## MEDIA");
		for (String fileName : importer.media.keySet()) {
			int sizeKb = importer.media.get(fileName).length / 1024;
			System.out.println(fileName + " " + sizeKb + " kB");
		}
	}

}
