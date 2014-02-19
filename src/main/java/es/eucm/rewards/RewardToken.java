package es.eucm.rewards;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.codec.binary.QRCodeCompatibleBase32Encoder;

public class RewardToken implements Comparable<RewardToken> {
	
	public static final String QRCODE_ALPHANUMERIC_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
	
	private static final String REWARD_TOKEN_SEPARATORS = "$%*+-./:";
	
	private static final char DEFAULT_TOKEN_SEPARATOR = '$';
	
	private final byte[] nonce;

	private final long tstamp;
	
	private final byte[] hmacHash;

	private final char separator;
	
	private final HashAlgorithmEnum algorithm;

	public RewardToken(byte[] nonce, long tstamp, HashAlgorithmEnum hashAlgorithm, byte[] hmacHash) {
		this(nonce, tstamp, hashAlgorithm, hmacHash, DEFAULT_TOKEN_SEPARATOR);
	}

	public RewardToken(byte[] nonce, long tstamp, HashAlgorithmEnum hashAlgorithm, byte[] hmacHash, char separator) {
		if (nonce == null) {
			throw new NullPointerException("nonce must not be null");
		}
		if (hmacHash == null) {
			throw new NullPointerException("hmacHash must not be null");
		}
		if (hashAlgorithm == null) {
			throw new NullPointerException("hashAlgorithm must not be null");
		}
		if (tstamp < 0) {
			throw new IllegalArgumentException("tstamp must be >= 0: " + tstamp);
		}
		if (REWARD_TOKEN_SEPARATORS.indexOf(separator) == -1) {
			throw new IllegalArgumentException("separator must be one of this characters: "+REWARD_TOKEN_SEPARATORS);
		}
		
		this.nonce = new byte[nonce.length];
		System.arraycopy(nonce, 0, this.nonce, 0, nonce.length);
		
		this.hmacHash = new byte[hmacHash.length];
		System.arraycopy(hmacHash, 0, this.hmacHash, 0, hmacHash.length);
		
		this.tstamp = tstamp;

		this.separator = separator;
		
		this.algorithm = hashAlgorithm;
	}

	public byte[] getNonce() {
		return nonce;
	}

	public long getTstamp() {
		return tstamp;
	}

	public byte[] getHmacHash() {
		return hmacHash;
	}

	public char getSeparator() {
		return separator;
	}

	public HashAlgorithmEnum getAlgorithm() {
		return algorithm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + Arrays.hashCode(hmacHash);
		result = prime * result + Arrays.hashCode(nonce);
		result = prime * result + separator;
		result = prime * result + (int) (tstamp ^ (tstamp >>> 32));
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
		RewardToken other = (RewardToken) obj;
		if (algorithm != other.algorithm)
			return false;
		if (!Arrays.equals(hmacHash, other.hmacHash))
			return false;
		if (!Arrays.equals(nonce, other.nonce))
			return false;
		if (separator != other.separator)
			return false;
		if (tstamp != other.tstamp)
			return false;
		return true;
	}

	public int compareTo(RewardToken o) {
		int result = Long.compare(this.tstamp, o.tstamp);
		if (result == 0) {
			result = Character.compare(this.separator, o.separator);
			if (result == 0) {
				result = this.algorithm.compareTo(o.algorithm);
				if (result == 0) {
					int arrayLength = this.nonce.length - o.nonce.length;
					result = arrayLength;
					if (result == 0) {
						int i = 0;
						while (result == 0 && i < arrayLength) {
							result = this.nonce[i] = o.nonce[i];
							i++;
						}
						if (result == 0) {
							arrayLength = this.hmacHash.length - o.hmacHash.length;
							result = arrayLength;
							if (result == 0) {
								i = 0;
								while (result == 0 && i < arrayLength) {
									result = this.hmacHash[i] = o.hmacHash[i];
									i++;
								}

							}
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		byte[] tstampArray = ByteBuffer.allocate(8).putLong(tstamp).array();
		QRCodeCompatibleBase32Encoder encoder = new QRCodeCompatibleBase32Encoder(0);
		return new StringBuilder().append(encoder.encodeToString(this.nonce))
				.append(separator).append(encoder.encodeToString(tstampArray))
				.append(separator).append(this.algorithm)
				.append(separator).append(encoder.encodeToString(this.hmacHash))
				.toString();
	}

	public static RewardToken parse(String string) {
		return parse(string, DEFAULT_TOKEN_SEPARATOR);
	}

	public static RewardToken parse(String string, char separator) {
		String[] chunks = string.split(escapeSeparator(separator));
		if (chunks.length < 4) {
			throw new IllegalArgumentException("string has not a valid format: <nonce>"+separator+"<tstamp>"+separator+"<hash algorithm>"+separator+"<hmac hash>");
		}
		QRCodeCompatibleBase32Encoder decoder = new QRCodeCompatibleBase32Encoder(0);
		byte[] nonce = decoder.decode(chunks[0]);
		byte[] tstamp = decoder.decode(chunks[1]);
		HashAlgorithmEnum algorithm = HashAlgorithmEnum.valueOf(chunks[2]);
		byte[] hmacHash = decoder.decode(chunks[3]);
		
		ByteBuffer buffer = ByteBuffer.allocate(8);
	    buffer.put(tstamp);
	    buffer.flip();//need flip 
	    
		return new RewardToken(nonce, buffer.getLong(), algorithm, hmacHash, separator);
	}

	private static final String JAVA_REG_EXP_META_CHARS = ".^$|*+?()[{";

	private static String escapeSeparator(char separator) {
		StringBuilder result = new StringBuilder();
		
		if (JAVA_REG_EXP_META_CHARS.indexOf(separator) != -1) {
			result.append("\\");
		}
		result.append(separator);
		return result.toString();
	}
}
