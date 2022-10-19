package org.argeo.app.api;

import java.util.Map;

import org.argeo.api.cms.CmsLog;

/**
 * A container for an object whose relevance can be ranked. Typically used in an
 * OSGi context with the service.ranking property.
 */
public class RankedObject<T> {
	private final static CmsLog log = CmsLog.getLog(RankedObject.class);

	private final static String SERVICE_RANKING = "service.ranking";
//	private final static String SERVICE_ID = "service.id";

	private T object;
	private Map<String, Object> properties;
	private final int rank;

	public RankedObject(T object, Map<String, Object> properties) {
		this(object, properties, extractRanking(properties));
	}

	public RankedObject(T object, Map<String, Object> properties, int rank) {
		super();
		this.object = object;
		this.properties = properties;
		this.rank = rank;
	}

	private static int extractRanking(Map<String, Object> properties) {
		if (properties == null)
			return 0;
		if (properties.containsKey(SERVICE_RANKING))
			return (Integer) properties.get(SERVICE_RANKING);
//		else if (properties.containsKey(SERVICE_ID))
//			return (Long) properties.get(SERVICE_ID);
		else
			return 0;
	}

	public T get() {
		return object;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public int getRank() {
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
		return rank == other.rank && object.equals(other.object);
	}

	@Override
	public String toString() {
		return object.getClass().getName() + " with rank " + rank;
	}

	public static <K, T> boolean hasHigherRank(Map<K, RankedObject<T>> map, K key, Map<String, Object> properties) {
		if (!map.containsKey(key))
			return true;
		RankedObject<T> rankedObject = new RankedObject<>(null, properties);
		RankedObject<T> current = map.get(key);
		return current.getRank() < rankedObject.getRank();
	}

	/**
	 * @return the {@link RankedObject}, or <code>null</code> if the current one was
	 *         kept
	 */
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
			if (current.getRank() < rankedObject.getRank()) {
				map.put(key, rankedObject);
				if (log.isDebugEnabled())
					log.debug("Replaced " + key + " by " + object.getClass().getName() + " with rank "
							+ rankedObject.getRank());
				return rankedObject;
			} else if (current.getRank() == rankedObject.getRank()) {
				log.error("Already " + key + " by " + current.get().getClass().getName() + " with rank "
						+ rankedObject.getRank() + ", ignoring " + rankedObject.get().getClass().getName());
				return null;
			} else {
				return null;
			}
		}

	}

}
