package com.sentrysoftware.hardware.agent.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductInfoServiceTest {

	@Autowired
	private ProductInfoService productInfoService;

	@Test
	void testLogProductInformation() {
		try (MockedStatic<ProductInfoService> application = mockStatic(ProductInfoService.class)) {
			application.when(() -> ProductInfoService.isLogInfoEnabled()).thenReturn(true);
			assertDoesNotThrow(() -> productInfoService.logProductInformation());
		}
	}

	@Test
	void testIsLogInfoEnabled() {
		assertDoesNotThrow(() -> ProductInfoService.isLogInfoEnabled());
	}

}
