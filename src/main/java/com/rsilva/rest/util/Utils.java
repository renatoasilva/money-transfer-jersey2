package com.rsilva.rest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class Utils {

	public static BigDecimal getRoundUpAmount(BigDecimal input) {
		return scaleMoney(input.setScale(0, RoundingMode.UP).subtract(input));
	}

	public static BigDecimal scaleMoney(BigDecimal money) {
		return money.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}
}
