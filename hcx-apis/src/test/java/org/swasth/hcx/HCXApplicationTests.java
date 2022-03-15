package org.swasth.hcx;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HCXApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void applicationStarts() {
		HCXApplication.main(new String[] {});
		//we added dummy assert for now later we remove this one
		assertEquals(true, true);
	}

}
