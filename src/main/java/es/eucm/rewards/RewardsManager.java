package es.eucm.rewards;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;

import com.itextpdf.text.DocumentException;

public class RewardsManager {

	public static final String SECRET_PROPERTY = "secret";
	
	public static final int DEFAULT_SECRET_SIZE = 60;
	
	public static final String DEFAULT_CONFIG_FILENAME = "generator.properties";
	
	public static final String DEFAULT_GENERATED_FILENAME = "generator.csv";

	public static String generateKey(int size) {
		SecureRandom rnd = new SecureRandom();
		byte[] key = new byte[size];
		rnd.nextBytes(key);
		return Base64.encodeBase64String(key);
	}
	
	public static byte[] parseKey(String key) {
		return Base64.decodeBase64(key);
	}
	
	private RewardsTokenGenerator generator;

	private Set<RewardToken> tokens;
	
	public RewardsManager(boolean generateKeyIfNotExists) throws IOException {
		Properties properties = new Properties();
		File file = new File(DEFAULT_CONFIG_FILENAME);
		if (file.exists()) {
			InputStream input = new FileInputStream(file);
			properties.load(input);
			input.close();
		}

		String secret = properties.getProperty(SECRET_PROPERTY);
		if (secret == null) {
			if ( generateKeyIfNotExists ) {
				secret = RewardsManager.generateKey(DEFAULT_SECRET_SIZE);
				properties.put(SECRET_PROPERTY, secret);
				OutputStream out = new FileOutputStream(file);
				properties.store(out, "Generator properties");
				out.close();
			} else {
				throw new IllegalStateException("secret not found");
			}
		}

		this.generator = new RewardsTokenGenerator(secret);
		this.tokens = new HashSet<>();

		load();
	}

	private void load() throws FileNotFoundException, IOException {
		File file = new File(DEFAULT_GENERATED_FILENAME);
		if (file.exists()) {
			load(new FileInputStream(file));
		}
	}

	public List<RewardToken> generate(int numPages) throws FileNotFoundException, IOException,
			DocumentException {
		List<RewardToken> newTokens = generateCodes(numPages);
		return newTokens;
	}
	
	private List<RewardToken> generateCodes(int numPages) {
		Iterator<RewardToken> it = generator.iterator();
		List<RewardToken> newTokens = new ArrayList<>();
		int quantity = 14 * 2 * numPages;
		int i = 0;
		while (i < quantity) {
			RewardToken token = it.next();
			if (tokens.add(token)) {
				newTokens.add(token);
				i++;
			}
		}
		return newTokens;
	}
	
	public void load(InputStream in) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = reader.readLine();
		while (line != null) {
			tokens.add(RewardToken.parse(line));
			line = reader.readLine();
		}
	}

	public void save() throws IOException {
		File file = new File(DEFAULT_GENERATED_FILENAME);
		OutputStream out = new FileOutputStream(file);
		save(out);
		out.close();
	}
	

	public void save(OutputStream out) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		for(RewardToken token : tokens) {
			writer.write(token.toString());
			writer.newLine();
		}
		writer.flush();
	}

	public boolean verify(String string) {
		RewardToken token = RewardToken.parse(string);
		return tokens.contains(token) && generator.verifyToken(token);
	}
}
