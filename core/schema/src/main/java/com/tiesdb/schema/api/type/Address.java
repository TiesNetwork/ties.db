package com.tiesdb.schema.api.type;

import java.math.BigInteger;

public interface Address {
	
	String toString();
	String toChecksumedString();
	byte[] toBytes();
	BigInteger toBigNumber();

}
