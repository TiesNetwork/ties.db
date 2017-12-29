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
 * with Ties.DB project.If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package com.tiesdb.lib.crypto.ecc.signature;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import com.tiesdb.lib.crypto.digest.DigestManager;
import com.tiesdb.lib.crypto.digest.api.Digest;
import com.tiesdb.lib.crypto.utils.ByteUtil;

/**
 * <p>
 * Represents an elliptic curve public and (optionally) private key, usable for
 * digital signatures but not encryption. Creating a new ECKey with the empty
 * constructor will generate a new random keypair. Other static methods can be
 * used when you already have the public or private parts. If you create a key
 * with only the public part, you can check signatures but not create them.
 * </p>
 *
 * <p>
 * The ECDSA algorithm supports <i>key recovery</i> in which a signature plus a
 * couple of discriminator bits can be reversed to find the public key used to
 * calculate it. This can be convenient when you have a message and a signature
 * and want to find out who signed it, rather than requiring the user to provide
 * the expected identity.
 * </p>
 *
 * This code is borrowed from the ethereumj project and altered to fit Ties.DB
 * and depend on bouncycastle.<br>
 * See <a href=
 * "https://github.com/ethereum/ethereumj/blob/develop/ethereumj-core/src/main/java/org/ethereum/crypto/ECKey.java">
 * ethereumj on GitHub</a>.
 */
public class ECKey {
	/**
	 * The parameters of the secp256k1 curve that Ethereum uses.
	 */
	public static final ECDomainParameters CURVE;
	public static final ECParameterSpec CURVE_SPEC;
	public static final String ALGORITHM = "EC";
	public static final String CURVE_NAME = "secp256k1";
	private static final ECGenParameterSpec SECP256K1_CURVE = new ECGenParameterSpec(CURVE_NAME);
	public static final String SIGNATURE_ALGORITHM = "NONEwithECDSA";

	/**
	 * Equal to CURVE.getN().shiftRight(1), used for canonicalising the S value of a
	 * signature. ECDSA signatures are mutable in the sense that for a given (R, S)
	 * pair, then both (R, S) and (R, N - S mod N) are valid signatures. Canonical
	 * signatures are those where 1 <= S <= N/2
	 *
	 * See
	 * https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures
	 */
	public static final BigInteger HALF_CURVE_ORDER;
	private static final SecureRandom secureRandom;
	// the Java Cryptographic Architecture provider to use for Signature
	// this is set along with the PrivateKey privKey and must be compatible
	// this provider will be used when selecting a Signature instance
	// https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html
	private static final Provider provider;

	private static Provider getProvider() {
		Provider provider = Security.getProvider("BC");
		if (provider == null) {
			Security.addProvider(new BouncyCastleProvider());
			provider = Security.getProvider("BC");
		}
		check(provider != null, "Could not create crypto provider!");
		return provider;
	}

	static {
		// All clients must agree on the curve to use by agreement. Ethereum uses
		// secp256k1.
		X9ECParameters params = SECNamedCurves.getByName("secp256k1");
		CURVE = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH());
		CURVE_SPEC = new ECParameterSpec(params.getCurve(), params.getG(), params.getN(), params.getH());
		HALF_CURVE_ORDER = params.getN().shiftRight(1);
		secureRandom = new SecureRandom();
		provider = getProvider();
	}

	// The two parts of the key. If "priv" is set, "pub" can always be calculated.
	// If "pub" is set but not "priv", we
	// can only verify signatures not make them.
	private final PrivateKey privKey;
	protected final ECPoint pub;

	// Transient because it's calculated on demand.
	transient private byte[] pubKeyHash;

	/**
	 * Generates an entirely new keypair.
	 *
	 * BouncyCastle will be used as the Java Security Provider
	 */
	public ECKey() {
		this(secureRandom);
	}

	/*
	 * Convert a Java JCE ECPublicKey into a BouncyCastle ECPoint
	 */
	private static ECPoint extractPublicKey(final ECPublicKey ecPublicKey) {
		final java.security.spec.ECPoint publicPointW = ecPublicKey.getW();
		final BigInteger xCoord = publicPointW.getAffineX();
		final BigInteger yCoord = publicPointW.getAffineY();

		return CURVE.getCurve().createPoint(xCoord, yCoord);
	}

	/**
	 * Generates an entirely new keypair with the given {@link SecureRandom} object.
	 *
	 * BouncyCastle will be used as the Java Security Provider
	 *
	 * @param secureRandom
	 *            -
	 */
	public ECKey(SecureRandom secureRandom) {
		try {
			final KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(ALGORITHM, provider);
			keyPairGen.initialize(SECP256K1_CURVE, secureRandom);
			final KeyPair keyPair = keyPairGen.generateKeyPair();

			this.privKey = keyPair.getPrivate();

			final PublicKey pubKey = keyPair.getPublic();
			if (pubKey instanceof BCECPublicKey) {
				pub = ((BCECPublicKey) pubKey).getQ();
			} else if (pubKey instanceof ECPublicKey) {
				pub = extractPublicKey((ECPublicKey) pubKey);
			} else {
				throw new AssertionError("Expected Provider " + provider.getName()
						+ " to produce a subtype of ECPublicKey, found " + pubKey.getClass());
			}
		} catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException ex) {
			throw new AssertionError("Assumed JRE supports EC key pair generation", ex);
		}
	}

	/*
	 * Test if a generic private key is an EC private key
	 *
	 * it is not sufficient to check that privKey is a subtype of ECPrivateKey as
	 * the SunPKCS11 Provider will return a generic PrivateKey instance a fallback
	 * that covers this case is to check the key algorithm
	 */
	private static boolean isECPrivateKey(PrivateKey privKey) {
		return privKey instanceof ECPrivateKey || privKey.getAlgorithm().equals(ALGORITHM);
	}

	/**
	 * Pair a private key with a public EC point.
	 *
	 * All private key operations will use the provider.
	 */
	public ECKey(PrivateKey privKey, ECPoint pub) {
		if (privKey == null || isECPrivateKey(privKey)) {
			this.privKey = privKey;
		} else {
			throw new IllegalArgumentException("Expected EC private key, given a private key object with class "
					+ privKey.getClass().toString() + " and algorithm " + privKey.getAlgorithm());
		}

		if (pub == null) {
			throw new IllegalArgumentException("Public key may not be null");
		} else {
			this.pub = pub;
		}
	}

	/*
	 * Convert a BigInteger into a PrivateKey object
	 */
	private static PrivateKey privateKeyFromBigInteger(BigInteger priv) {
		if (priv == null) {
			return null;
		} else {
			try {
				return KeyFactory.getInstance(ALGORITHM, provider)
						.generatePrivate(new ECPrivateKeySpec(priv, CURVE_SPEC));
			} catch (InvalidKeySpecException ex) {
				throw new AssertionError("Assumed correct key spec statically");
			} catch (NoSuchAlgorithmException e) {
				throw new AssertionError("Assumed JRE supports EC key pair generation", e);
			}
		}
	}

	/**
	 * Pair a private key integer with a public EC point Creates an ECKey that
	 * simply trusts the caller to ensure that point is really the result of
	 * multiplying the generator point by the private key. This is used to speed
	 * things up when you know you have the right values already. The compression
	 * state of pub will be preserved.
	 *
	 * BouncyCastle will be used as the Java Security Provider
	 * 
	 * @param priv
	 *            -
	 * @param pub
	 *            -
	 *
	 * @return -
	 */
	public ECKey(BigInteger priv, ECPoint pub) {
		this(privateKeyFromBigInteger(priv), pub);
	}

	/**
	 * Creates an ECKey given the private key only.
	 *
	 * @param privKey
	 *            -
	 *
	 *
	 * @return -
	 */
	public static ECKey fromPrivate(BigInteger privKey) {
		return new ECKey(privKey, CURVE.getG().multiply(privKey));
	}

	/**
	 * Creates an ECKey given the private key only.
	 *
	 * @param privKeyBytes
	 *            -
	 *
	 * @return -
	 */
	public static ECKey fromPrivate(byte[] privKeyBytes) {
		return fromPrivate(new BigInteger(1, privKeyBytes));
	}

	/**
	 * Creates an ECKey that simply trusts the caller to ensure that point is really
	 * the result of multiplying the generator point by the private key. This is
	 * used to speed things up when you know you have the right values already. The
	 * compression state of pub will be preserved.
	 *
	 * @param priv
	 *            -
	 * @param pub
	 *            -
	 *
	 * @return -
	 */
	public static ECKey fromPrivateAndPrecalculatedPublic(BigInteger priv, ECPoint pub) {
		return new ECKey(priv, pub);
	}

	/**
	 * Creates an ECKey that cannot be used for signing, only verifying signatures,
	 * from the given point. The compression state of pub will be preserved.
	 *
	 * @param pub
	 *            -
	 * @return -
	 */
	public static ECKey fromPublicOnly(ECPoint pub) {
		return new ECKey((PrivateKey) null, pub);
	}

	/**
	 * Creates an ECKey that cannot be used for signing, only verifying signatures,
	 * from the given encoded point. The compression state of pub will be preserved.
	 *
	 * @param pub
	 *            -
	 * @return -
	 */
	public static ECKey fromPublicOnly(byte[] pub) {
		return new ECKey((PrivateKey) null, CURVE.getCurve().decodePoint(pub));
	}

	/**
	 * Returns true if this key doesn't have access to private key bytes. This may
	 * be because it was never given any private key bytes to begin with (a watching
	 * key).
	 *
	 * @return -
	 */
	public boolean isPubKeyOnly() {
		return privKey == null;
	}

	/**
	 * Returns true if this key has access to private key bytes. Does the opposite
	 * of {@link #isPubKeyOnly()}.
	 *
	 * @return -
	 */
	public boolean hasPrivKey() {
		return privKey != null;
	}

	/**
	 * Returns public key bytes from the given private key. To convert a byte array
	 * into a BigInteger, use <tt>
	 * new BigInteger(1, bytes);</tt>
	 *
	 * @param privKey
	 *            -
	 * @param compressed
	 *            -
	 * @return -
	 */
	public static byte[] publicKeyFromPrivate(BigInteger privKey, boolean compressed) {
		ECPoint point = CURVE.getG().multiply(privKey);
		return point.getEncoded(compressed);
	}

	/**
	 * Compute an address from an encoded public key.
	 *
	 * @param pubBytes
	 *            an encoded (uncompressed) public key
	 * @return 20-byte address
	 */
	public static byte[] computeAddress(byte[] pubBytes) {
		check(pubBytes.length >= 64, "Public key should be either 64 or 65 bytes");
		Digest hash = DigestManager.getDigest(DigestManager.KECCAK);
		byte[] out = new byte[hash.getDigestSize()];
		hash.update(pubBytes, pubBytes.length - 64, 64);
		hash.doFinal(out, 0);
		return Arrays.copyOfRange(out, out.length - 20, out.length);
	}

	/**
	 * Compute an address from a public point.
	 *
	 * @param pubPoint
	 *            a public point
	 * @return 20-byte address
	 */
	public static byte[] computeAddress(ECPoint pubPoint) {
		return computeAddress(pubPoint.getEncoded(/* uncompressed */ false));
	}

	/**
	 * Gets the address form of the public key.
	 *
	 * @return 20-byte address
	 */
	public byte[] getAddress() {
		if (pubKeyHash == null) {
			check(pub != null, "Public key should be initialized");
			pubKeyHash = computeAddress(this.pub);
		}
		return pubKeyHash;
	}

	/**
	 * Gets the encoded public key value.
	 *
	 * @return 65-byte encoded public key
	 */
	public byte[] getPubKey() {
		return pub.getEncoded(/* compressed */ false);
	}

	/**
	 * Gets the public key in the form of an elliptic curve point object from Bouncy
	 * Castle.
	 *
	 * @return -
	 */
	public ECPoint getPubKeyPoint() {
		return pub;
	}

	/**
	 * Gets the private key in the form of an integer field element. The public key
	 * is derived by performing EC point addition this number of times (i.e. point
	 * multiplying).
	 *
	 *
	 * @return -
	 *
	 * @throws java.lang.AssertionError
	 *             if the private key bytes are not available.
	 */
	public BigInteger getPrivKey() {
		if (privKey == null) {
			throw new AssertionError("Private key is uninitialized");
		} else if (privKey instanceof BCECPrivateKey) {
			return ((BCECPrivateKey) privKey).getD();
		} else {
			throw new AssertionError("Private key is unknown");
		}
	}

	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("pub:").append(Hex.toHexString(pub.getEncoded(false)));
		return b.toString();
	}

	/**
	 * Produce a string rendering of the ECKey INCLUDING the private key. Unless you
	 * absolutely need the private key it is better for security reasons to just use
	 * toString().
	 *
	 *
	 * @return -
	 */
	public String toStringWithPrivate() {
		StringBuilder b = new StringBuilder();
		b.append(toString());
		if (privKey != null && privKey instanceof BCECPrivateKey) {
			b.append(" priv:").append(Hex.toHexString(((BCECPrivateKey) privKey).getD().toByteArray()));
		}
		return b.toString();
	}

	/**
	 * Groups the two components that make up a signature, and provides a way to
	 * encode to Base64 form, which is how ECDSA signatures are represented when
	 * embedded in other data structures in the Ethereum protocol. The raw
	 * components can be useful for doing further EC maths on them.
	 */
	public static class ECDSASignature {
		public static final byte vBase = 0x25; //Value to add to v (old value is 27, new value is 0x25 (EIP#155))
		
		/**
		 * The two components of the signature.
		 */
		public final BigInteger r, s;
		public byte v;

		/**
		 * Constructs a signature with the given components. Does NOT automatically
		 * canonicalise the signature.
		 *
		 * @param r
		 *            -
		 * @param s
		 *            -
		 */
		public ECDSASignature(BigInteger r, BigInteger s) {
			this.r = r;
			this.s = s;
		}

		/**
		 * t
		 * 
		 * @param r
		 * @param s
		 * @return -
		 */
		private static ECDSASignature fromComponents(byte[] r, byte[] s) {
			return new ECDSASignature(new BigInteger(1, r), new BigInteger(1, s));
		}

		/**
		 *
		 * @param r
		 *            -
		 * @param s
		 *            -
		 * @param v
		 *            -
		 * @return -
		 */
		public static ECDSASignature fromComponents(byte[] r, byte[] s, byte v) {
			ECDSASignature signature = fromComponents(r, s);
			signature.v = v;
			return signature;
		}

		public static ECDSASignature fromBytes(byte[] signatureEncoded) throws SignatureException {
			// Parse the signature bytes into r/s and the selector value.
			if (signatureEncoded.length < 65)
				throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
			
			return fromComponents(Arrays.copyOfRange(signatureEncoded, 0, 32),
					Arrays.copyOfRange(signatureEncoded, 32, 64), signatureEncoded[64]);
		}

		public boolean validateComponents() {
			return validateComponents(r, s, v);
		}

		public static boolean validateComponents(BigInteger r, BigInteger s, byte v) {

			if (v != vBase && v != vBase+1)
				return false;

			if (r.compareTo(BigInteger.ONE) < 0)
				return false;
			if (s.compareTo(BigInteger.ONE) < 0)
				return false;

			if (r.compareTo(CURVE.getN()) >= 0)
				return false;
			if (s.compareTo(CURVE.getN()) >= 0)
				return false;

			return true;
		}

		public static ECDSASignature decodeFromDER(byte[] bytes) {
			ASN1InputStream decoder = null;
			try {
				decoder = new ASN1InputStream(bytes);
				DLSequence seq = (DLSequence) decoder.readObject();
				if (seq == null)
					throw new RuntimeException("Reached past end of ASN.1 stream.");
				ASN1Integer r, s;
				try {
					r = (ASN1Integer) seq.getObjectAt(0);
					s = (ASN1Integer) seq.getObjectAt(1);
				} catch (ClassCastException e) {
					throw new IllegalArgumentException(e);
				}
				// OpenSSL deviates from the DER spec by interpreting these values as unsigned,
				// though they should not be
				// Thus, we always use the positive versions. See:
				// http://r6.ca/blog/20111119T211504Z.html
				return new ECDSASignature(r.getPositiveValue(), s.getPositiveValue());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (decoder != null)
					try {
						decoder.close();
					} catch (IOException x) {
					}
			}
		}

		/**
		 * Will automatically adjust the S component to be less than or equal to half
		 * the curve order, if necessary. This is required because for every signature
		 * (r,s) the signature (r, -s (mod N)) is a valid signature of the same message.
		 * However, we dislike the ability to modify the bits of a Ethereum transaction
		 * after it's been signed, as that violates various assumed invariants. Thus in
		 * future only one of those forms will be considered legal and the other will be
		 * banned.
		 *
		 * @return -
		 */
		public ECDSASignature toCanonicalised() {
			if (s.compareTo(HALF_CURVE_ORDER) > 0) {
				// The order of the curve is the number of valid points that exist on that
				// curve. If S is in the upper
				// half of the number of valid points, then bring it back to the lower half.
				// Otherwise, imagine that
				// N = 10
				// s = 8, so (-8 % 10 == 2) thus both (r, 8) and (r, 2) are valid solutions.
				// 10 - 8 == 2, giving us always the latter solution, which is canonical.
				return new ECDSASignature(r, CURVE.getN().subtract(s));
			} else {
				return this;
			}
		}

		public byte[] toByteArray() {
			byte[] bytes = new byte[32 + 32 + 1];
			ByteUtil.bigIntegerToBytes(this.r, bytes, 0, 32);
			ByteUtil.bigIntegerToBytes(this.s, bytes, 32, 32);
			bytes[64] = this.v;

			return bytes;
		}

		public String toHex() {
			return Hex.toHexString(toByteArray());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			ECDSASignature signature = (ECDSASignature) o;

			if (!r.equals(signature.r))
				return false;
			if (!s.equals(signature.s))
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			int result = r.hashCode();
			result = 31 * result + s.hashCode();
			return result;
		}
	}

	/**
	 * Signs the given hash and returns the R and S components as BigIntegers and
	 * put them in ECDSASignature
	 *
	 * @param input
	 *            to sign
	 * @return ECDSASignature signature that contains the R and S components
	 */
	public ECDSASignature doSign(byte[] input) {
		check(input.length == 32, "Expected 32 byte input to ECDSA signature, not " + input.length);

		// No decryption of private key required.
		if (privKey == null)
			throw new AssertionError("Private key is not set");
		if (privKey instanceof BCECPrivateKey) {
			ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
			ECPrivateKeyParameters privKeyParams = new ECPrivateKeyParameters(((BCECPrivateKey) privKey).getD(), CURVE);
			signer.init(true, privKeyParams);
			BigInteger[] components = signer.generateSignature(input);
			return new ECDSASignature(components[0], components[1]).toCanonicalised();
		} else {
			try {
				final Signature ecSig = Signature.getInstance(SIGNATURE_ALGORITHM, provider);
				ecSig.initSign(privKey);
				ecSig.update(input);
				final byte[] derSignature = ecSig.sign();
				return ECDSASignature.decodeFromDER(derSignature).toCanonicalised();
			} catch (SignatureException | InvalidKeyException | NoSuchAlgorithmException ex) {
				throw new RuntimeException("ECKey signing error", ex);
			}
		}
	}

	/**
	 * Takes the keccak hash (32 bytes) of data and returns the ECDSA signature
	 *
	 * @param messageHash
	 *            -
	 * @return -
	 * @throws IllegalStateException
	 *             if this ECKey does not have the private part.
	 */
	public ECDSASignature sign(byte[] messageHash) {
		ECDSASignature sig = doSign(messageHash);
		// Now we have to work backwards to figure out the recId needed to recover the
		// signature.
		int recId = -1;
		byte[] thisKey = this.pub.getEncoded(/* compressed */ false);
		for (int i = 0; i < 4; i++) {
			byte[] k = ECKey.recoverPubBytesFromSignature(i, sig, messageHash);
			if (k != null && Arrays.equals(k, thisKey)) {
				recId = i;
				break;
			}
		}
		if (recId == -1)
			throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
		sig.v = (byte) (recId + ECDSASignature.vBase);
		return sig;
	}

	/**
	 * Given a piece of text and a message signature encoded in base64, returns an
	 * ECKey containing the public key that was used to sign it. This can then be
	 * compared to the expected public key to determine if the signature was
	 * correct.
	 *
	 * @param messageHash
	 *            a piece of human readable text that was signed
	 * @param signatureEncoded
	 *            The Ethereum-format message signature in base64
	 *
	 * @return -
	 * @throws SignatureException
	 *             If the public key could not be recovered or if there was a
	 *             signature format error.
	 */
	public static byte[] signatureToKeyBytes(byte[] messageHash, byte[] signatureEncoded) throws SignatureException {
		return signatureToKeyBytes(messageHash,	ECDSASignature.fromBytes(signatureEncoded));
	}

	public static byte[] signatureToKeyBytes(byte[] messageHash, ECDSASignature sig) throws SignatureException {
		check(messageHash.length == 32, "messageHash argument has length " + messageHash.length);
		int header = sig.v;
		// The header byte: 0x25 = first key with even y, 0x26 = first key with odd y,
		// 0x27 = second key with even y, 0x28 = second key with odd y - these keys should have been removed with normalization
		if (header < ECDSASignature.vBase || header > ECDSASignature.vBase + 3)
			throw new SignatureException("Header byte out of range: " + header);

		int recId = header - ECDSASignature.vBase;
		byte[] key = ECKey.recoverPubBytesFromSignature(recId, sig, messageHash);
		if (key == null)
			throw new SignatureException("Could not recover public key from signature");
		return key;
	}
	
	public boolean checkSignature(byte[] messageHash, byte[] signatureEncoded) throws SignatureException {
		return checkSignature(messageHash, ECDSASignature.fromBytes(signatureEncoded));
	}
	
	public boolean checkSignature(byte[] messageHash, ECDSASignature sig) throws SignatureException {
		byte []pubkey = signatureToKeyBytes(messageHash, sig);
		return Arrays.equals(pubkey, getPubKey());
	}

	public static byte[] signatureToAddressBytes(byte[] messageHash, byte[] signatureEncoded) throws SignatureException {
		return signatureToAddressBytes(messageHash,	ECDSASignature.fromBytes(signatureEncoded));
	}

	public static byte[] signatureToAddressBytes(byte[] messageHash, ECDSASignature sig) throws SignatureException {
		byte[] pubkey = signatureToKeyBytes(messageHash, sig);
		byte[] address = computeAddress(pubkey);
		return address;
	}
	
	/**
	 * Decompress a compressed public key (x co-ord and low-bit of y-coord).
	 *
	 * @param xBN
	 *            -
	 * @param yBit
	 *            -
	 * @return -
	 */
	private static ECPoint decompressKey(BigInteger xBN, boolean yBit) {
		X9IntegerConverter x9 = new X9IntegerConverter();
		byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(CURVE.getCurve()));
		compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
		return CURVE.getCurve().decodePoint(compEnc);
	}

	/**
	 * <p>
	 * Given the components of a signature and a selector value, recover and return
	 * the public key that generated the signature according to the algorithm in
	 * SEC1v2 section 4.1.6.
	 * </p>
	 *
	 * <p>
	 * The recId is an index from 0 to 3 which indicates which of the 4 possible
	 * keys is the correct one. Because the key recovery operation yields multiple
	 * potential keys, the correct key must either be stored alongside the
	 * signature, or you must be willing to try each recId in turn until you find
	 * one that outputs the key you are expecting.
	 * </p>
	 *
	 * <p>
	 * If this method returns null it means recovery was not possible and recId
	 * should be iterated.
	 * </p>
	 *
	 * <p>
	 * Given the above two points, a correct usage of this method is inside a for
	 * loop from 0 to 3, and if the output is null OR a key that is not the one you
	 * expect, you try again with the next recId.
	 * </p>
	 *
	 * @param recId
	 *            Which possible key to recover.
	 * @param sig
	 *            the R and S components of the signature, wrapped.
	 * @param messageHash
	 *            Hash of the data that was signed.
	 * @return 65-byte encoded public key
	 */
	private static byte[] recoverPubBytesFromSignature(int recId, ECDSASignature sig, byte[] messageHash) {
		check(recId >= 0, "recId must be positive");
		check(sig.r.signum() >= 0, "r must be positive");
		check(sig.s.signum() >= 0, "s must be positive");
		check(messageHash != null, "messageHash must not be null");
		// 1.0 For j from 0 to h (h == recId here and the loop is outside this function)
		// 1.1 Let x = r + jn
		BigInteger n = CURVE.getN(); // Curve order.
		BigInteger i = BigInteger.valueOf((long) recId / 2);
		BigInteger x = sig.r.add(i.multiply(n));
		// 1.2. Convert the integer x to an octet string X of length mlen using the
		// conversion routine
		// specified in Section 2.3.7, where mlen = вЊ€(log2 p)/8вЊ‰ or mlen =
		// вЊ€m/8вЊ‰.
		// 1.3. Convert the octet string (16 set binary digits)||X to an elliptic curve
		// point R using the
		// conversion routine specified in Section 2.3.4. If this conversion routine
		// outputs вЂњinvalidвЂќ, then
		// do another iteration of Step 1.
		//
		// More concisely, what these points mean is to use X as a compressed public
		// key.
		ECCurve.Fp curve = (ECCurve.Fp) CURVE.getCurve();
		BigInteger prime = curve.getQ(); // Bouncy Castle is not consistent about the letter it uses for the prime.
		if (x.compareTo(prime) >= 0) {
			// Cannot have point co-ordinates larger than this as everything takes place
			// modulo Q.
			return null;
		}
		// Compressed keys require you to know an extra bit of data about the y-coord as
		// there are two possibilities.
		// So it's encoded in the recId.
		ECPoint R = decompressKey(x, (recId & 1) == 1);
		// 1.4. If nR != point at infinity, then do another iteration of Step 1 (callers
		// responsibility).
		if (!R.multiply(n).isInfinity())
			return null;
		// 1.5. Compute e from M using Steps 2 and 3 of ECDSA signature verification.
		BigInteger e = new BigInteger(1, messageHash);
		// 1.6. For k from 1 to 2 do the following. (loop is outside this function via
		// iterating recId)
		// 1.6.1. Compute a candidate public key as:
		// Q = mi(r) * (sR - eG)
		//
		// Where mi(x) is the modular multiplicative inverse. We transform this into the
		// following:
		// Q = (mi(r) * s ** R) + (mi(r) * -e ** G)
		// Where -e is the modular additive inverse of e, that is z such that z + e = 0
		// (mod n). In the above equation
		// ** is point multiplication and + is point addition (the EC group operator).
		//
		// We can find the additive inverse by subtracting e from zero then taking the
		// mod. For example the additive
		// inverse of 3 modulo 11 is 8 because 3 + 8 mod 11 = 0, and -3 mod 11 = 8.
		BigInteger eInv = BigInteger.ZERO.subtract(e).mod(n);
		BigInteger rInv = sig.r.modInverse(n);
		BigInteger srInv = rInv.multiply(sig.s).mod(n);
		BigInteger eInvrInv = rInv.multiply(eInv).mod(n);
		ECPoint.Fp q = (ECPoint.Fp) ECAlgorithms.sumOfTwoMultiplies(CURVE.getG(), eInvrInv, R, srInv);
		return q.getEncoded(/* compressed */ false);
	}

	private static void check(boolean test, String message) {
		if (!test)
			throw new IllegalArgumentException(message);
	}

}
