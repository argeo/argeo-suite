package org.argeo.suite;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A container for an object whose relevance can be ranked. Typically used in an
 * OSGi context with the service.ranking property.
 */
public class RankedObject<T> {
	private final static Log log = LogFactory.getLog(RankedObject.class);

	private final static String SERVICE_RANKING = "service.ranking";
//	private final static String SERVICE_ID = "service.id";

	private T object;
	private Map<String, Object> properties;
	private final Long rank;

	public RankedObject(T object, Map<String, Object> properties) {
		this(object, properties, extractRanking(properties));
	}

	public RankedObject(T object, Map<String, Object> properties, Long rank) {
		super();
		this.object = object;
		this.properties = properties;
		this.rank = rank;
	}

	private static Long extractRanking(Map<String, Object> properties) {
		if (properties == null)
			return 0l;
		if (properties.containsKey(SERVICE_RANKING))
			return Long.valueOf(properties.get(SERVICE_RANKING).toString());
//		else if (properties.containsKey(SERVICE_ID))
//			return (Long) properties.get(SERVICE_ID);
		else
			return 0l;
	}

	public T get() {
		return object;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Long getRank() {
		return rank;
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RankedObject))
			return false;
		RankedObject<?> other = (RankedObject<?>) obj;
		return rank.equals(other.rank) && object.equals(other.object);
	}

	@Override
	public String toString() {
		return object.getClass().getName() + " with rank " + rank;
	}

	public static <K, T> RankedObject<T> putIfHigherRank(Map<K, RankedObject<T>> map, K key, T object,
			Map<String, Object> properties) {
		RankedObject<T> rankedObject = new RankedObject<>(object, properties);
		if (!map.containsKey(key)) {
			map.put(key, rankedObject);
			if (log.isTraceEnabled())
				log.trace(
						"Added " + key + " as " + object.getClass().getName() + " with rank " + rankedObject.getRank());
			return rankedObject;
		} else {
			RankedObject<T> current = map.get(key);
			if (current.getRank() <= rankedObject.getRank()) {
				map.put(key, rankedObject);
				if (log.isTraceEnabled())
					log.trace("Replaced " + key + " by " + object.getClass().getName() + " with rank "
							+ rankedObject.getRank());
				return rankedObject;
			} else {
				return current;
			}
		}

	}

}
