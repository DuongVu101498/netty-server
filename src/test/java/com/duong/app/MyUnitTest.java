package com.duong.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MyUnitTest {

	@Test
	void test() {
		assertEquals("Hello world !", HttpSnoopServerHandler.getHello());
	}

}
