package com.tiesdb.schema.impl;

import java.math.BigInteger;

import org.web3j.utils.Numeric;

import com.tiesdb.schema.api.type.Address;

public class AddressImpl extends org.web3j.abi.datatypes.Address implements Address {
	public AddressImpl(String val) {
		super(val);
	}

	AddressImpl(byte[] val) {
		this(new BigInteger(val));
	}

	AddressImpl(BigInteger val) {
		super(val);
	}
	
	@Override
	public String toChecksumedString() {
		return toString();
	}

	@Override
	public byte[] toBytes() {
		return Numeric.toBytesPadded(toBigNumber(), super.LENGTH>>8);
	}

	@Override
	public BigInteger toBigNumber() {
		return toUint160().getValue();
	}

}
