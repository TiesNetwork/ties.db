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
package com.tiesdb.protocol.v0r0.api.message;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class DataEntryField {

	private String name;
	private FieldValue value;
	private byte[] fieldHash; // hash(name, value.type, value.data)

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FieldValue getValue() {
		return value;
	}

	public void setValue(FieldValue value) {
		this.value = value;
	}

	public byte[] getFieldHash() {
		return fieldHash;
	}

	public void setFieldHash(byte[] fieldHash) {
		this.fieldHash = fieldHash;
	}

	@Override
	public String toString() {
		return "DataEntryField [name=" + name + ", value=" + value + ", fieldHash="
				+ (fieldHash == null ? "null" : DatatypeConverter.printHexBinary(fieldHash)) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(fieldHash);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		DataEntryField other = (DataEntryField) obj;
		if (!Arrays.equals(fieldHash, other.fieldHash))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
