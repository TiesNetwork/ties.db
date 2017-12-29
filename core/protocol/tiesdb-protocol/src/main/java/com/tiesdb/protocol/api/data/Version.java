/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.protocol.api.data;

import java.util.Comparator;

public final class Version {

	public static enum VersionComprator implements Comparator<Version> {
		MAJOR, MINOR, FULL;

		@Override
		public int compare(Version v1, Version v2) {
			int c = 0;
			return v1 == null
				? (v2 == null ? 0 : 1)
				: v2 == null
					? -1
					: (c = Integer.compare(v1.major, v2.major)) != 0 || equals(MAJOR)
						? c
						: (c = Integer.compare(v1.minor, v2.minor)) != 0 || equals(MINOR)
							? c /**/
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

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public int getMaint() {
		return maint;
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
