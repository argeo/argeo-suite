package org.argeo.app.geo.geonames;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** A Geonames administrative subdivision. */
public class GeonamesAdm {
	private final Long geonameId;
	private final String countryCode;
	private final String adminCode1;
	private final String admLevel;
	private final Integer level;
	private final String name;
	private final String asciiName;
	private final List<String> alternateNames;
	private final Double lat;
	private final Double lng;
	private final LocalDate lastUpdated;
	private final ZoneId timeZone;

	private final Long[] upperLevelIds = new Long[5];
	private final List<GeonamesAdm> upperLevels = new ArrayList<>();

	private List<String> row;

	/** Initialise from a row in the main Geonames table. */
	public GeonamesAdm(List<String> row) {
		geonameId = Long.parseLong(row.get(0));
		admLevel = row.get(7);
		countryCode = row.get(8);
		adminCode1 = row.get(10);
		if (admLevel.startsWith("ADM")) {
			if (admLevel.endsWith("H"))
				level = Integer.parseInt(admLevel.substring(3, admLevel.length() - 1));
			else
				level = Integer.parseInt(admLevel.substring(3));
		} else if (admLevel.equals("PCLI")) {
			level = 0;
		} else {
			throw new IllegalArgumentException("Unsupported admin level " + admLevel);
		}
		name = row.get(1);
		asciiName = row.get(2);
		alternateNames = Arrays.asList(row.get(3).split(","));
		lat = Double.parseDouble(row.get(4));
		lng = Double.parseDouble(row.get(5));
		lastUpdated = LocalDate.parse(row.get(18));
		timeZone = ZoneId.of(row.get(17));
		// upper levels
		if (row.get(11) != null && !row.get(11).trim().equals(""))
			upperLevelIds[2] = Long.parseLong(row.get(11));
		if (row.get(12) != null && !row.get(12).trim().equals(""))
			upperLevelIds[3] = Long.parseLong(row.get(12));
		if (row.get(13) != null && !row.get(13).trim().equals(""))
			upperLevelIds[4] = Long.parseLong(row.get(13));
		this.row = row;
	}

	public void mapUpperLevels(Map<Long, GeonamesAdm> index) {
		for (int i = 0; i < level; i++) {
			Long geonameId = upperLevelIds[i];
			upperLevels.add(i, index.get(geonameId));
		}
	}

	public Long getGeonameId() {
		return geonameId;
	}

	public Integer getLevel() {
		return level;
	}

	public String getName() {
		return name;
	}

	public String getName(Function<String, String> transform) {
		if (transform != null)
			return transform.apply(name);
		else
			return name;

	}

	public String getAsciiName() {
		return asciiName;
	}

	public List<String> getAlternateNames() {
		return alternateNames;
	}

	public Double getLat() {
		return lat;
	}

	public Double getLng() {
		return lng;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public String getAdmLevel() {
		return admLevel;
	}

	public List<String> getRow() {
		return row;
	}

	public LocalDate getLastUpdated() {
		return lastUpdated;
	}

	public ZoneId getTimeZone() {
		return timeZone;
	}

	public String getAdminCode1() {
		return adminCode1;
	}

	public Long[] getUpperLevelIds() {
		return upperLevelIds;
	}

	public List<GeonamesAdm> getUpperLevels() {
		return upperLevels;
	}

	@Override
	public int hashCode() {
		return geonameId.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof GeonamesAdm))
			return false;
		GeonamesAdm other = (GeonamesAdm) obj;
		return geonameId.equals(other.geonameId);
	}

	@Override
	public String toString() {
		return name + " (ADM" + level + " " + geonameId + ")";
	}

}
