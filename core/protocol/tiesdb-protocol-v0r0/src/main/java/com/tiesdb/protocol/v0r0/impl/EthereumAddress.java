package com.tiesdb.protocol.v0r0.impl;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import com.tiesdb.protocol.v0r0.api.message.part.BlockchainAddress;

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
