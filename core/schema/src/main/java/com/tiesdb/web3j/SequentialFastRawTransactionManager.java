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
