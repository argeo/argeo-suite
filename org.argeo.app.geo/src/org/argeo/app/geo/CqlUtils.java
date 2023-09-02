package org.argeo.app.geo;

import org.argeo.api.acr.NamespaceUtils;
import org.argeo.api.acr.search.AndFilter;
import org.argeo.api.acr.search.BasicSearch;
import org.argeo.api.acr.search.ContentFilter;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

/** Utilities around the CQL query format. */
public class CqlUtils {

	public static void filter(BasicSearch search, String cql) {
		try {
			filter(search, CQL.toFilter(cql));
		} catch (CQLException e) {
			throw new IllegalArgumentException("Cannot parse CQL: " + cql, e);
		}
	}

	public static void filter(BasicSearch search, Filter filter) {
		search.where((where) -> {
			if (filter instanceof And and) {
				processAnd(where, and);
			} else if (filter instanceof PropertyIsEqualTo propertyIsEqualTo) {
				processIsEqualTo(where, propertyIsEqualTo);
			} else {
				throw new IllegalArgumentException("Unsupported filter " + filter.getClass());
			}
		});
	}

	private static void processAnd(AndFilter contentFilter, And filter) {
		for (Filter child : filter.getChildren()) {
			if (child instanceof PropertyIsEqualTo propertyIsEqualTo) {
				processIsEqualTo(contentFilter, propertyIsEqualTo);
			}
		}

	}

	private static void processIsEqualTo(ContentFilter<?> contentFilter, PropertyIsEqualTo propertyIsEqualTo) {
		// TODO properly deal with types etc.
		// see GeoTools org.geotools.filter.text.commons.ExpressionToText
		PropertyName propertyName = (PropertyName) propertyIsEqualTo.getExpression1();
		Literal value = (Literal) propertyIsEqualTo.getExpression2();
		// String escaped = literal.toString().replaceAll("'", "''");
		contentFilter.eq(NamespaceUtils.parsePrefixedName(propertyName.toString()), value.toString());
	}

	/** singleton */
	private CqlUtils() {
	}
}
