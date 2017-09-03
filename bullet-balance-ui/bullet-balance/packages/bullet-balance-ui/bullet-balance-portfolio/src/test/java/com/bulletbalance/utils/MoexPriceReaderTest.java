package com.bulletbalance.utils;

import org.junit.Assert;
import org.junit.Test;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * @author berdonosova
 *         Date: 22.12.2016
 */
public class MoexPriceReaderTest {
	@Test
	public void testReader() throws ParseException {
		DecimalFormat format = MoexPriceReader.createMoexPriceFormat();
		Assert.assertEquals("Price format", 12870.34, format.parse(MoexPriceReader.prepareValue("12 870,34")).doubleValue(), 0.0005);
	}

}
