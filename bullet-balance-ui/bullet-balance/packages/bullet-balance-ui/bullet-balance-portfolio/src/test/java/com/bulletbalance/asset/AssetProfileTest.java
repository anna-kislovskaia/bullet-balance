package com.bulletbalance.asset;

import org.junit.Assert;
import org.junit.Test;

public class AssetProfileTest {
	@Test
	public void testAssetProfile() {
		// SUN prices 1997-2007
		double[] prices = new double[]{19.94, 42.81, 154.88, 111.48, 49.20, 12.44, 17.88, 21.56, 16.76, 21.68, 18.13};
		AssetProfile profile = AssetProfile.create("test", prices);
		Assert.assertEquals("Count", prices.length - 1, profile.getCount());
		Assert.assertEquals("Geometric mean return", -0.009, profile.getMeanReturn(), 0.0005);
		Assert.assertEquals("Min price", 12.44, profile.getMinPrice(), 0.0005);
		Assert.assertEquals("Max price", 154.88, profile.getMaxPrice(), 0.0005);
	}
}
