package es.eucm.rewards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

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
		Iterator<RewardToken> it = cut.iterator();
		for(int i = 0; i < 50; i++) {
			RewardToken expected = it.next();
			RewardToken actual = RewardToken.parse(expected.toString(), expected.getSeparator());
			assertEquals(expected, actual);
			assertTrue(cut.verifyToken(actual));
		}

	}
}
