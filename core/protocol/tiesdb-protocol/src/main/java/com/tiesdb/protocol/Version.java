package com.tiesdb.protocol;

import java.util.Comparator;

import static com.tiesdb.protocol.Version.VersionCompratorThreshold.*;

public final class Version {

	public static enum VersionCompratorThreshold {
		MAJOR, MINOR, FULL
	}

	public static class VersionComparator implements Comparator<Version> {

		private final VersionCompratorThreshold comparatorThreshold;

		public VersionComparator() {
			this(VersionCompratorThreshold.FULL);
		}

		public VersionComparator(VersionCompratorThreshold comparatorThreshold) {
			if (comparatorThreshold == null) {
				throw new IllegalArgumentException("comparatorThreshold should not be null");
			}
			this.comparatorThreshold = comparatorThreshold;
		}

		@Override
		public int compare(Version v1, Version v2) {
			int c = 0;
			return v1 == null
				? (v2 == null ? 0 : 1)
				: v2 == null
					? -1
					: (c = Integer.compare(v1.major, v2.major)) != 0 || comparatorThreshold.equals(MAJOR)
						? c
						: (c = Integer.compare(v1.minor, v2.minor)) != 0 || comparatorThreshold.equals(MINOR)
							? c
							: Integer.compare(v1.maint, v2.maint);
		}
	}

	private static final int MAX_UNSIGNED_VALUE = 1 + Short.MAX_VALUE * 2;

	private final int major;
	private final int minor;
	private final int maint;

	public Version(int major) {
		this(major, 0, 0);
	}

	public Version(int major, int minor) {
		this(major, minor, 0);
	}

	public Version(int major, int minor, int maint) {
		this.major = safeShort(major);
		this.minor = safeShort(minor);
		this.maint = safeShort(maint);
	}

	private static int safeShort(int v) {
		if (v < 0 || v > MAX_UNSIGNED_VALUE) {
			throw new IllegalArgumentException("Unsigned short should be 0 - " + MAX_UNSIGNED_VALUE);
		}
		return v;
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
		return major + "." + minor + (maint != 0 ? "." + maint : "");
	}
}
