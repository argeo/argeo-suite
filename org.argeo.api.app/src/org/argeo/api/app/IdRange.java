package org.argeo.api.app;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/** A range of numerical IDs (typically numerical uid or gid). */
public class IdRange {
	// see https://systemd.io/UIDS-GIDS/#special-distribution-uid-ranges
	final static long MIN_INCLUDED = Long.parseUnsignedLong("66000");
	final static long MAX_EXCLUDED = Long.parseUnsignedLong("4294967294");

	// We use long as a de facto unsigned int
	
	/** included */
	private final long min;
	/** included */
	private final long max;

	public IdRange(long min, long max) {
		this.min = min;
		this.max = max;
	}

	public IdRange(long minPow10) {
		this(minPow10, maxFromMinPow10(minPow10));
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	@Override
	public int hashCode() {
		return (int) min;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IdRange idRange) {
			return min == idRange.min && max == idRange.max;
		} else
			return false;
	}

	@Override
	public String toString() {
		return "[" + Long.toUnsignedString(min) + "," + Long.toUnsignedString(max) + "]";
	}

	/*
	 * RANGE GENERATION
	 */
	public static synchronized Set<IdRange> randomRanges10000(int count, Set<IdRange> forbiddenRanges) {
		Set<IdRange> res = new HashSet<>();

		for (int i = 0; i < count; i++) {
			IdRange newRange = null;
			do {
				newRange = randomRange10000();
			} while (overlap(newRange, res) || overlap(newRange, forbiddenRanges));
			res.add(newRange);
		}
		return res;
	}

	public static synchronized IdRange randomRange10000() {
		// TODO make it more generic
		long minPred = 7l;
		long maxPred = 429496l;

		long rand = ThreadLocalRandom.current().nextLong(minPred, maxPred);
		long min = rand * 10000l;
		return new IdRange(min);
	}

	public static boolean overlap(IdRange idRange, Set<IdRange> idRanges) {
		for (IdRange other : idRanges) {
			if (overlap(idRange, other))
				return true;
		}
		return false;
	}

	public static boolean overlap(IdRange idRange, IdRange other) {
		// see
		// https://stackoverflow.com/questions/3269434/whats-the-most-efficient-way-to-test-if-two-ranges-overlap
		return idRange.min <= other.max && other.min <= idRange.max;
	}

	/*
	 * UTILITIES
	 */

	private static long maxFromMinPow10(long minPow10) {
		if ((minPow10 % 100) != 0) {
			throw new IllegalArgumentException(minPow10 + " must at least ends with two zeroes");
		}
		int exp = 2;
		exp: for (int i = exp + 1; i < 10; i++) {
			if ((minPow10 % pow10(i)) != 0)
				break exp;
			exp++;
		}
//		System.out.println(exp);

		long max = minPow10 + pow10(exp) - 1;
		return max;
	}

	/** Power of 10. */
	private static long pow10(int exp) {
		if (exp == 0)
			return 1;
		else
			return 10 * pow10(exp - 1);
	}

	public static void main(String... args) {
		System.out.println(maxFromMinPow10(100));
		System.out.println(maxFromMinPow10(78500));
		System.out.println(maxFromMinPow10(716850000));

//		System.out.println(pow10(6));
//		System.out.println(maxFromMinPow10(12));
//		System.out.println(maxFromMinPow10(124));
//		System.out.println(maxFromMinPow10(99814565));
	}
}
