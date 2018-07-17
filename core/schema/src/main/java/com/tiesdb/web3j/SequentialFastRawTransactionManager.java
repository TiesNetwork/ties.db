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
package com.tiesdb.web3j;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.FastRawTransactionManager;

public class SequentialFastRawTransactionManager extends FastRawTransactionManager {

	public SequentialFastRawTransactionManager(Web3j web3j, Credentials credentials) {
		super(web3j, credentials);
		
	}
	
	private BigInteger decodeNonce(BigInteger value) {
		assert(value.testBit(255)); //Tweaked value should have most significant bit set
		return value.clearBit(255).shiftRight(192);
	}
	
	private BigInteger decodeValue(BigInteger value) {
		assert(value.testBit(255)); //Tweaked value should have most significant bit set
		return BigInteger.ZERO.setBit(192).subtract(BigInteger.ONE).and(value);
	}
	
	public BigInteger encodeNonceToValue(BigInteger value) throws IOException {
		BigInteger nonce = getNonce();
		return nonce.shiftLeft(192).setBit(255).or(value);
	}

    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, BigInteger value) throws IOException {

        BigInteger nonce = decodeNonce(value);
        BigInteger val = decodeValue(value);

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                gasPrice,
                gasLimit,
                to,
                val,
                data);

        return signAndSend(rawTransaction);
    }
}
