package es.eucm.rewards;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Iterator;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class RewardsTokenGenerator implements Iterable<RewardToken> {
	
	/**
	 * Default number of random generated bytes.
	 */
	public static final int DEFAULT_NONCE_SIZE = 10;

	public static String generateKey(int size) {
		SecureRandom rnd = new SecureRandom();
		byte[] key = new byte[size];
		rnd.nextBytes(key);
		return Base64.encodeBase64String(key);
	}
	
	public static byte[] parseKey(String key) {
		return Base64.decodeBase64(key);
	}
	
	private SecureRandom rnd;
	
	private byte[] secret;

	private HashAlgorithmEnum hashAlgorithm;


	public RewardsTokenGenerator(String secret) {
		this(secret.getBytes(), HashAlgorithmEnum.SHA1);
	}
	
	public RewardsTokenGenerator(String secret, HashAlgorithmEnum hashAlgorithm) {
		this(secret.getBytes(), hashAlgorithm);
	}
	
	public RewardsTokenGenerator(byte[] secret) {
		this(secret, HashAlgorithmEnum.SHA1);
	}
	
	public RewardsTokenGenerator(byte[] secret, HashAlgorithmEnum hashAlgorithm) {
		if (secret == null) {
			throw new NullPointerException("secret must not be null");
		}
		if (hashAlgorithm == null) {
			throw new NullPointerException("hashAlgorithm must not be null");
		}
		this.rnd = new SecureRandom();
		this.secret = secret;
		this.hashAlgorithm = hashAlgorithm;
	}

	private byte[] hmacMd5(byte[] value) {
			byte[] keyBytes = this.secret;
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacMD5");

			// Get an hmac_md5 Mac instance and initialize with the signing key
			Mac mac;
			byte[] rawHmac = new byte[0];
			try {
				mac = Mac.getInstance("HmacMD5");
				mac.init(signingKey);
				// Compute the hmac on input data bytes
				rawHmac = mac.doFinal(value);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("Error generating HMAC-MD5", e);
			} catch (InvalidKeyException e) {
				throw new RuntimeException("Error generating HMAC-MD5", e);
			}

			return rawHmac;
	}
	
	private byte[] hmacSha1(byte[] value) {
			byte[] keyBytes = this.secret;
			SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA1");

			// Get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac;
			byte[] rawHmac = new byte[0];
			try {
				mac = Mac.getInstance("HmacSHA1");
				mac.init(signingKey);
				// Compute the hmac on input data bytes
				rawHmac = mac.doFinal(value);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("Error generating HMAC-SHA1", e);
			} catch (InvalidKeyException e) {
				throw new RuntimeException("Error generating HMAC-SHA1", e);
			}

			return rawHmac;
	}
	
	public boolean verifyToken(RewardToken token) {
		byte[] hash = sign(token.getNonce(), token.getTstamp(), token.getAlgorithm());
		return Arrays.equals(token.getHmacHash(), hash);
	}
	
	private byte[] sign(byte[] nonce, long tstamp, HashAlgorithmEnum hashAlgorithm) {
		byte[] algorithm;
		try {
			algorithm = hashAlgorithm.toString().getBytes("US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Cannot generate token", e);
		}
		
		int messageSize = nonce.length;
		messageSize += 8; // timestamp size
		messageSize += algorithm.length;


		// Create a buffer large enough to store the random bytes + timestamp
		byte[] buffer = new byte[messageSize];
		System.arraycopy(nonce, 0, buffer, 0, 0);
		
		messageSize = nonce.length;
		
		// Add timestamp to the buffer
		byte[] tstampArray = ByteBuffer.allocate(8).putLong(tstamp).array();
		System.arraycopy(tstampArray, 0, buffer, messageSize, 8);
		messageSize +=8;

		System.arraycopy(algorithm, 0, buffer, messageSize, algorithm.length);
		
		byte[] hash;
		switch(hashAlgorithm) {
			case MD5 :
				hash = hmacMd5(buffer);
			break;
			default :
				hash = hmacSha1(buffer);
			break;
		}

		return hash;
	}

	public Iterator<RewardToken> iterator() {
		return new GeneratorIterator(DEFAULT_NONCE_SIZE);
	}
	
	private class GeneratorIterator implements Iterator<RewardToken> {
		
		private byte[] nonce;
		
		public GeneratorIterator(int size) {
			this.nonce = new byte[size];			
		}
		
		public boolean hasNext() {
			return true;
		}

		public RewardToken next() {
			rnd.nextBytes(this.nonce);
			long tstamp = System.currentTimeMillis();
			byte[] hash = sign(this.nonce, tstamp, RewardsTokenGenerator.this.hashAlgorithm);
			return new RewardToken(this.nonce, tstamp, RewardsTokenGenerator.this.hashAlgorithm, hash);
		}

		public void remove() {
			throw new UnsupportedOperationException("Operation not permitted");
		}
		
	}
}
