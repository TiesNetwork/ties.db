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
		return Numeric.toBytesPadded(toBigNumber(), org.web3j.abi.datatypes.Address.LENGTH>>8);
	}

	@Override
	public BigInteger toBigNumber() {
		return toUint160().getValue();
	}

}
