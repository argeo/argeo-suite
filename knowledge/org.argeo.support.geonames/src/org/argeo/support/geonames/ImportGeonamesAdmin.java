package org.argeo.support.geonames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.argeo.util.CsvParser;
import org.argeo.util.CsvWriter;

/** Import GeoNames administrative division from the main table. */
public class ImportGeonamesAdmin {
	// private Log log = LogFactory.getLog(ImportGeonamesAdmin.class);
	private Map<Long, GeonamesAdm> geonamesAdms = new HashMap<>();

	/** Loads the data. */
	public void parse(InputStream in) {
		CsvParser csvParser = new CsvParser() {

			@Override
			protected void processLine(Integer lineNumber, List<String> header, List<String> tokens) {
				if (!"A".equals(tokens.get(6)))
					return;
				GeonamesAdm geonamesAdm = new GeonamesAdm(tokens);
				geonamesAdms.put(geonamesAdm.getGeonameId(), geonamesAdm);
//				String featureName = tokens.get(7);
//				String geonameId = tokens.get(0);
//				String name = tokens.get(1);
//				Double lat = Double.parseDouble(tokens.get(4));
//				Double lng = Double.parseDouble(tokens.get(5));
//				switch (featureName) {
//				case "ADM1":
//				case "ADM4":
//					String adminCode1 = tokens.get(10);
//					System.out.println(
//							geonameId + " " + featureName + " " + lat + "," + lng + " " + adminCode1 + " " + name);
//					break;
//				default:
//					break;
//				}

			}
		};
		csvParser.setSeparator('\t');
		csvParser.setNoHeader(true);
		csvParser.parse(in, StandardCharsets.UTF_8);
	}

	public Map<Long, GeonamesAdm> getGeonamesAdms() {
		return geonamesAdms;
	}

	/**
	 * Copies only the Geonames of feature class 'A' (administrative subdivisions).
	 */
	public static void filterGeonamesAdm(InputStream in, OutputStream out) {
		CsvWriter csvWriter = new CsvWriter(out, StandardCharsets.UTF_8);
		csvWriter.setSeparator('\t');
		CsvParser csvParser = new CsvParser() {

			@Override
			protected void processLine(Integer lineNumber, List<String> header, List<String> tokens) {
				if (tokens.size() < 7 || !"A".equals(tokens.get(6)))
					return;
				csvWriter.writeLine(tokens);
			}
		};
		csvParser.setSeparator('\t');
		csvParser.setNoHeader(true);
		csvParser.parse(in, StandardCharsets.UTF_8);
	}

	public static void main(String[] args) throws IOException {
		String country = "allCountries";
		// String country = "CI";
		try (InputStream in = Files
				.newInputStream(Paths.get(System.getProperty("user.home") + "/gis/data/geonames/" + country + ".txt"));
				OutputStream out = Files.newOutputStream(
						Paths.get(System.getProperty("user.home") + "/gis/data/geonames/" + country + "-adm.txt"))) {
			ImportGeonamesAdmin.filterGeonamesAdm(in, out);
		}
//		try (InputStream in = Files.newInputStream(
//				Paths.get(System.getProperty("user.home") + "/gis/data/geonames/" + country + "-adm.txt"))) {
//			ImportGeonamesAdmin importGeonamesAdmin = new ImportGeonamesAdmin();
//			importGeonamesAdmin.parse(in);
//		}
	}

}
