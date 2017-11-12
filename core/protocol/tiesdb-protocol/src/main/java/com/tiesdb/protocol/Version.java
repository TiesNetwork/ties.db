package com.tiesdb.protocol;

import java.util.Comparator;

public final class Version {

	public static final Comparator<Version> VERSION_COMPARATOR = new Comparator<Version>() {

		@Override
		public int compare(Version v1, Version v2) {
			int c = 0;
			return v1 == null ? (v2 == null ? 0 : 1)
					: v2 == null ? -1
							: (c = compare(v1.major, v2.major)) != 0 ? c
									: (c = compare(v1.minor, v2.minor)) != 0 ? c : compare(v1.maint, v2.maint);
		}

		private int compare(short o1, short o2) {
			return Integer.compare(o1 + Short.MIN_VALUE, o2 + Short.MIN_VALUE);
		}

	};
	private static final int MAX_UNSIGNED_VALUE = 1 + Short.MAX_VALUE * 2;

	private final short major;
	private final short minor;
	private final short maint;
	private final String versionString;

	public Version(int major) {
		this(major, 0, 0);
	}

	public Version(int major, int minor) {
		this(major, minor, 0);
	}

	public Version(int major, int minor, int maint) {
		this(safeShort(major), safeShort(minor), safeShort(maint));
	}

	private static short safeShort(int v) {
		if (v < 0 || v > MAX_UNSIGNED_VALUE) {
			throw new IllegalArgumentException("Unsigned short should be 0 - " + MAX_UNSIGNED_VALUE);
		}
		return (short) v;
	}

	protected Version(short major, short minor, short maint) {
		super();
		this.major = major;
		this.minor = minor;
		this.maint = maint;
		this.versionString = Short.toUnsignedInt(major) + "." + Short.toUnsignedInt(minor)
				+ (maint != 0 ? "." + Short.toUnsignedInt(maint) : "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + maint;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Version other = (Version) obj;
		if (major != other.major)
			return false;
		if (minor != other.minor)
			return false;
		if (maint != other.maint)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return versionString;
	}
}
