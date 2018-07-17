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
package com.tiesdb.lib.crypto.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Arrays;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.ecc.signature.ECKey;
import com.tiesdb.lib.crypto.utils.ByteUtil;

class ECKeyTest {

	@Test
	void test() throws UnsupportedEncodingException, SignatureException {
		ECKey key = new ECKey();
		assertTrue(key.hasPrivKey(), "key should become initialized");
		
		String message = "The quick brown fox jumps over the lazy dog";
		Digest keccak = DigestManager.getDigest(DigestManager.KECCAK);
		
		byte [] hash = new byte[keccak.getDigestSize()];
		byte [] data = message.getBytes("UTF-8");
		keccak.update(data, 0, data.length);
		keccak.doFinal(hash, 0);
		
		assertTrue(Arrays.equals(hash, Hex.decode("4d741b6f1eb29cb2a9b9911c82f56fa8d73b04959d3d9d222895df6c0b28aa15")), "Keccak 256 should act good (as at https://emn178.github.io/online-tools/keccak_256.html)");
		
		ECKey.ECDSASignature sig = key.sign(hash);
		
		byte [] pub = ECKey.signatureToKeyBytes(hash, sig);
		assertTrue(Arrays.equals(pub, key.getPubKey()), "Signature should recover public key");
		
		pub = ECKey.signatureToKeyBytes(hash, sig.toByteArray());
		assertTrue(Arrays.equals(pub, key.getPubKey()), "Signature should recover from bytes to public key");
	}

	@Test
	void testShortKeys() throws UnsupportedEncodingException, SignatureException {
		byte[] keyb = new byte[32];
		for(int i=0; i<32; ++i) {
			keyb[i] = (byte)i;
		}
		
		ECKey key = ECKey.fromPrivate(keyb);
		assertTrue(key.hasPrivKey(), "key should become initialized");
		
		String message = "The quick brown fox jumps over the lazy dog.";
		Digest keccak = DigestManager.getDigest(DigestManager.KECCAK);
		
		byte [] hash = new byte[keccak.getDigestSize()];
		byte [] data = message.getBytes("UTF-8");
		keccak.update(data, 0, data.length);
		keccak.doFinal(hash, 0);
		
		assertTrue(Arrays.equals(hash, Hex.decode("578951e24efd62a3d63a86f7cd19aaa53c898fe287d2552133220370240b572d")), "Keccak 256 should act good (as at https://emn178.github.io/online-tools/keccak_256.html)");
		
		ECKey.ECDSASignature sig = key.sign(hash);
		
		byte [] pub = ECKey.signatureToKeyBytes(hash, sig);
		assertEquals(pub.length, 65, "Public key should be encoded by 65 bytes");
		assertTrue(Arrays.equals(pub, key.getPubKey()), "Signature should recover public key");
		
		//Testing checking signature with public key only
		ECKey pubkey = ECKey.fromPublicOnly(key.getPubKey());
		assertTrue(pubkey.checkSignature(hash, sig.toByteArray()));
	}
	
	@Test
	void testShortPublicKeys() throws UnsupportedEncodingException, SignatureException {
		SecureRandom sr = new SecureRandom();
		ECKey key;
		BigInteger cap = ECKey.CURVE.getN().shiftRight(8);
		do {
			byte[] keyb = new byte[32];
			sr.nextBytes(keyb);
			key = ECKey.fromPrivate(keyb);
			BigInteger x = key.getPubKeyPoint().normalize().getAffineXCoord().toBigInteger();
			if(x.compareTo(cap) < 0) {
				break;
			}
		} while(true);
		
		byte []pubkey = key.getPubKey();
		assertEquals(pubkey[1], 0, "public key should start with 0");
		assertTrue(pubkey.length == 65, "public key should be 65 bytes length");
	}
	
    @Test
    void testSignature() throws SignatureException {
    	byte[] pkey = Hex.decode("5f3c7581ee902c352bbe2d70fa665ad11f1c8ff197f28368e94dc1f314e61935");
    	ECKey key = ECKey.fromPrivate(pkey);
    	
    	//signed transaction: rlp:['','02540be400','5208','a39370058b6e7c13f765bea29ee0623195fc3c6d','0de0b6b3a7640000','','26','d522f3b1248cbb770b4bfd02bcd4c53987b98737b84fef8148099d5e2e7b38eb','252abc964aa94517e7934722151c1cc01c0dddb47cea3d481c6a490dc7178c35']
    	//payload for signing rlp:['','02540be400','5208','a39370058b6e7c13f765bea29ee0623195fc3c6d','0de0b6b3a7640000','','01','','']
    	byte[] data = Hex.decode("ec808502540be40082520894a39370058b6e7c13f765bea29ee0623195fc3c6d880de0b6b3a764000080018080");
    	
		Digest keccak = DigestManager.getDigest(DigestManager.KECCAK);
    	byte [] hash = new byte[keccak.getDigestSize()];
		keccak.update(data, 0, data.length);
		keccak.doFinal(hash, 0);    	
    	
    	assertTrue(Arrays.equals(hash, Hex.decode("603ab5c526c56bfe59173cdb65b03139538d672a1c3b717b752e6c6fda3fbedb")), "Keccak cashes should be the same");
    	
    	ECKey.ECDSASignature sig = key.sign(hash);
    	String hexr = Hex.toHexString(ByteUtil.bigIntegerToBytes(sig.r));
    	String hexs = Hex.toHexString(ByteUtil.bigIntegerToBytes(sig.s));
    	assertEquals(hexr, "d522f3b1248cbb770b4bfd02bcd4c53987b98737b84fef8148099d5e2e7b38eb", "Signature r should be the same");
    	assertEquals(hexs, "252abc964aa94517e7934722151c1cc01c0dddb47cea3d481c6a490dc7178c35", "Signature s should be the same");
    	assertEquals(sig.v, 0x26, "Signature v should be the same");
    	
    	byte[] addr = key.getAddress();
    	assertTrue(Arrays.equals(addr, Hex.decode("A39370058B6e7c13F765BEa29EE0623195fC3C6d")), "Address should be the same");
    	
    	addr = ECKey.signatureToAddressBytes(hash, sig);
    	assertTrue(Arrays.equals(addr, Hex.decode("A39370058B6e7c13F765BEa29EE0623195fC3C6d")), "Address from signature should be the same");
	}
	
	
}
