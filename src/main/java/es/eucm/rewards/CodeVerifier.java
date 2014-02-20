package es.eucm.rewards;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;


public class CodeVerifier {
	
	public static final String DEFAULT_CODES_FILENAME = "codes.txt";
	
	public static void main(String[] args) throws Exception {
		RewardsManager rewards = new RewardsManager(false);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(DEFAULT_CODES_FILENAME)));
		String line = reader.readLine();
		while(line != null) {
			if (!rewards.verify(line)) {
				System.err.println("NOT VERIFIED: "+line);
			}
			line = reader.readLine();
		}
		reader.close();
	}

}
