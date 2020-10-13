package org.argeo.suite.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.util.ISO9075;

/** Ease XPath generation for JCR requests */
public class XPathUtils {
	private final static Log log = LogFactory.getLog(XPathUtils.class);

	private final static String QUERY_XPATH = "xpath";

	public static String descendantFrom(String parentPath) {
		if (notEmpty(parentPath)) {
			if ("/".equals(parentPath))
				parentPath = "";
			// Hardcoded dependency to Jackrabbit. Remove
			String result = "/jcr:root" + ISO9075.encodePath(parentPath);
			if (log.isTraceEnabled()) {
				String result2 = "/jcr:root" + parentPath;
				if (!result2.equals(result))
					log.warn("Encoded Path " + result2 + " --> " + result);
			}
			return result;
		} else
			return "";
	}

	public static String localAnd(String... conditions) {
		StringBuilder builder = new StringBuilder();
		for (String condition : conditions) {
			if (notEmpty(condition)) {
				builder.append(" ").append(condition).append(" and ");
			}
		}
		if (builder.length() > 3)
			return builder.substring(0, builder.length() - 4);
		else
			return "";
	}

	public static String xPathNot(String condition) {
		if (notEmpty(condition))
			return "not(" + condition + ")";
		else
			return "";
	}

	public static String getFreeTextConstraint(String filter) throws RepositoryException {
		StringBuilder builder = new StringBuilder();
		if (notEmpty(filter)) {
			String[] strs = filter.trim().split(" ");
			for (String token : strs) {
				builder.append("jcr:contains(.,'*" + encodeXPathStringValue(token) + "*') and ");
			}
			return builder.substring(0, builder.length() - 4);
		}
		return "";
	}

	public static String getPropertyContains(String propertyName, String filter) throws RepositoryException {
		if (notEmpty(filter))
			return "jcr:contains(@" + propertyName + ",'*" + encodeXPathStringValue(filter) + "*')";
		return "";
	}

	private final static DateFormat jcrRefFormatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'+02:00'");

	/**
	 * @param propertyName
	 * @param calendar       the reference date
	 * @param lowerOrGreater "&lt;", "&gt;" TODO validate "&gt;="
	 * @return
	 * @throws RepositoryException
	 */
	public static String getPropertyDateComparaison(String propertyName, Calendar cal, String lowerOrGreater)
			throws RepositoryException {
		if (cal != null) {
			String jcrDateStr = jcrRefFormatter.format(cal.getTime());

			// jcrDateStr = "2015-08-03T05:00:03:000Z";
			String result = "@" + propertyName + " " + lowerOrGreater + " xs:dateTime('" + jcrDateStr + "')";
			return result;
		}
		return "";
	}

	public static String getPropertyEquals(String propertyName, String value) {
		if (notEmpty(value))
			return "@" + propertyName + "='" + encodeXPathStringValue(value) + "'";
		return "";
	}

	public static String encodeXPathStringValue(String propertyValue) {
		// TODO implement safer mechanism to escape invalid characters
		// Also check why we have used this regex in ResourceSerrviceImpl l 474
		// String cleanedKey = key.replaceAll("(?:')", "''");
		String result = propertyValue.replaceAll("'", "''");
		return result;
	}

	public static void andAppend(StringBuilder builder, String condition) {
		if (notEmpty(condition)) {
			builder.append(condition);
			builder.append(" and ");
		}
	}

	public static void appendOrderByProperties(StringBuilder builder, boolean ascending, String... propertyNames) {
		if (propertyNames.length > 0) {
			builder.append(" order by ");
			for (String propName : propertyNames)
				builder.append("@").append(propName).append(", ");
			builder = builder.delete(builder.length() - 2, builder.length());
			if (ascending)
				builder.append(" ascending ");
			else
				builder.append(" descending ");
		}
	}

	public static void appendAndPropStringCondition(StringBuilder builder, String propertyName, String filter)
			throws RepositoryException {
		if (notEmpty(filter)) {
			andAppend(builder, getPropertyContains(propertyName, filter));
		}
	}

	public static void appendAndNotPropStringCondition(StringBuilder builder, String propertyName, String filter)
			throws RepositoryException {
		if (notEmpty(filter)) {
			String cond = getPropertyContains(propertyName, filter);
			builder.append(xPathNot(cond));
			builder.append(" and ");
		}
	}

	public static Query createQuery(Session session, String queryString) throws RepositoryException {
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		// Localise JCR properties for XPATH
		queryString = localiseJcrItemNames(queryString);
		return queryManager.createQuery(queryString, QUERY_XPATH);
	}

	private final static String NS_JCR = "\\{http://www.jcp.org/jcr/1.0\\}";
	private final static String NS_NT = "\\{http://www.jcp.org/jcr/nt/1.0\\}";
	private final static String NS_MIX = "\\{http://www.jcp.org/jcr/mix/1.0\\}";

	/**
	 * Replace the generic namespace with the local "jcr:", "nt:", "mix:" values. It
	 * is a workaround that must be later cleaned
	 */
	public static String localiseJcrItemNames(String name) {
		name = name.replaceAll(NS_JCR, "jcr:");
		name = name.replaceAll(NS_NT, "nt:");
		name = name.replaceAll(NS_MIX, "mix:");
		return name;
	}

	private static boolean notEmpty(String stringToTest) {
		return !(stringToTest == null || "".equals(stringToTest.trim()));
	}

	/** Singleton. */
	private XPathUtils() {

	}
}
