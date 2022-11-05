package org.argeo.app.geo.geonames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.argeo.cms.util.CsvParser;
import org.argeo.cms.util.CsvWriter;

/** Import GeoNames administrative division from the main table. */
public class ImportGeonamesAdmin {
	// private Log log = LogFactory.getLog(ImportGeonamesAdmin.class);
	private Map<Long, GeonamesAdm> geonamesAdms = new HashMap<>();

	/** Loads the data. */
	public void parse(InputStream in) {
		Map<String, Long> countryGeonameIds = new HashMap<>();
		Map<String, Long> admin1GeonameIds = new HashMap<>();
		CsvParser csvParser = new CsvParser() {

			@Override
			protected void processLine(Integer lineNumber, List<String> header, List<String> tokens) {
				if (!"A".equals(tokens.get(6)))
					return;
				GeonamesAdm geonamesAdm = new GeonamesAdm(tokens);
				geonamesAdms.put(geonamesAdm.getGeonameId(), geonamesAdm);
				if (geonamesAdm.getAdmLevel().equals("PCLI"))
					countryGeonameIds.put(geonamesAdm.getCountryCode(), geonamesAdm.getGeonameId());
				if (geonamesAdm.getAdmLevel().equals("ADM1"))
					admin1GeonameIds.put(geonamesAdm.getAdminCode1(), geonamesAdm.getGeonameId());
			}
		};
		csvParser.setSeparator('\t');
		csvParser.setNoHeader(true);
		csvParser.parse(in, StandardCharsets.UTF_8);

		// fill upper levels
		for (GeonamesAdm adm : geonamesAdms.values()) {
			adm.getUpperLevelIds()[0] = countryGeonameIds.get(adm.getCountryCode());
			if (adm.getLevel() > 0)
				adm.getUpperLevelIds()[1] = admin1GeonameIds.get(adm.getAdminCode1());
			adm.mapUpperLevels(geonamesAdms);
		}

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
//		String country = "allCountries";
		String country = "CI";
//		try (InputStream in = Files
//				.newInputStream(Paths.get(System.getProperty("user.home") + "/gis/data/geonames/" + country + ".txt"));
//				OutputStream out = Files.newOutputStream(
//						Paths.get(System.getProperty("user.home") + "/gis/data/geonames/" + country + "-adm.txt"))) {
//			ImportGeonamesAdmin.filterGeonamesAdm(in, out);
//		}
		try (InputStream in = Files.newInputStream(
				Paths.get(System.getProperty("user.home") + "/gis/data/geonames/" + country + "-adm.txt"))) {
			ImportGeonamesAdmin importGeonamesAdmin = new ImportGeonamesAdmin();
			importGeonamesAdmin.parse(in);
		}
	}

}
