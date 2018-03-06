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

public class EthereumAddress implements BlockchainAddress {

	private static final String BLOCKCHAIN_TYPE = "Ethereum";
	private final byte[] bytes;

	@Override
	public String getBlockchainType() {
		return BLOCKCHAIN_TYPE;
	}

	public EthereumAddress(byte[] bytes) {
		if (!validateAdress(bytes)) {
			throw new IllegalArgumentException("Illegal ethereum address: " + DatatypeConverter.printHexBinary(bytes));
		}
		this.bytes = Arrays.copyOf(bytes, bytes.length);
	}

	// TODO Write more robust address check
	private boolean validateAdress(byte[] bytes) {
		if (null != bytes && bytes.length == 20) {
			return true;
		}
		return false;
	}

	public byte[] getBytes() {
		return Arrays.copyOf(bytes, bytes.length);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(getBytes());
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
		EthereumAddress other = (EthereumAddress) obj;
		if (!Arrays.equals(getBytes(), other.getBytes()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EthereumAddress [" + DatatypeConverter.printHexBinary(bytes) + "]";
	}

}
