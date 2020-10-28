package org.argeo.support.geonames;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

/** A Geonames administrative subdivision. */
public class GeonamesAdm {
	private Long geonameId;
	private String countryCode;
	private String admLevel;
	private Integer level;
	private String name;
	private String asciiName;
	private List<String> alternateNames;
	private Double lat;
	private Double lng;
	private LocalDate lastUpdated;
	private ZoneId timeZone;

	private List<String> row;

	public GeonamesAdm() {
	}

	/** Initialise from a row in the main Geonames table. */
	public GeonamesAdm(List<String> row) {
		geonameId = Long.parseLong(row.get(0));
		admLevel = row.get(7);
		countryCode = row.get(8);
		if (admLevel.startsWith("ADM") && !admLevel.endsWith("H")) {
			level = Integer.parseInt(admLevel.substring(3));
		} else if (admLevel.equals("PCLI")) {
			level = 0;
		}
		name = row.get(1);
		asciiName = row.get(2);
		alternateNames = Arrays.asList(row.get(3).split(","));
		lat = Double.parseDouble(row.get(4));
		lng = Double.parseDouble(row.get(5));
		lastUpdated = LocalDate.parse(row.get(18));
		timeZone = ZoneId.of(row.get(17));
		this.row = row;
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
