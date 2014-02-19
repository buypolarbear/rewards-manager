package es.eucm.rewards;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RewardsTokenGeneratorTest {

	private RewardsTokenGenerator cut;
	
	@Before
	public void setup() {
		this.cut = new RewardsTokenGenerator("test");
	}
	
	@Test
	public void generateTokenRewards() {
		String token = cut.iterator().next();
		System.out.println(token);
		assertEquals(49, token.length());
	}
}
