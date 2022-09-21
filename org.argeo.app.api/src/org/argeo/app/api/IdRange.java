package org.argeo.app.api;

/** A range of numerical IDs (typically numerical uid or gid). */
public class IdRange {

	private final long min;
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
		System.out.println(exp);

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
