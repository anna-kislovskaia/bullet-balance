package com.bulletbalance.portfolio;

import com.bulletbalance.asset.AssetProfile;
import com.bulletbalance.utils.MathUtils;
import com.bulletbalance.utils.MoexPriceReader;
import com.bulletbalance.utils.PortfolioUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.CharArrayReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PortfolioTest {

	@Test
	public void testExactAssetCorrelation() {
		PortfolioBuilder<String> builder = new PortfolioBuilder<>();
		builder.add("VSMO1", MoexPriceReader.readLastPrices(new CharArrayReader(VSMO.toCharArray())));
		builder.add("VSMO2", MoexPriceReader.readLastPrices(new CharArrayReader(VSMO.toCharArray())));
		Portfolio<String> portfolio = builder.build();
		Assert.assertEquals("Correlation", 1, portfolio.getCorrelationCoefficient("VSMO1", "VSMO2"), 0.0005);
	}

	@Test
	public void testSingleAssetPortfolio() {
		PortfolioBuilder<String> builder = new PortfolioBuilder<>();
		builder.add("GAZP", MoexPriceReader.readLastPrices(new CharArrayReader(GAZP.toCharArray())));
		Portfolio<String> portfolio = builder.build();

		BigDecimal[] allocation = new BigDecimal[]{BigDecimal.ONE};
		double portfolioReturn = portfolio.calculateAllocationReturn(allocation);
		double portfolioStdDev = portfolio.calculateAllocationStandardDeviation(allocation);

		AssetProfile<String> profile = portfolio.getProfile("GAZP");
		Assert.assertEquals("Single portfolio return", profile.getAverageReturn(), portfolioReturn, 0.0005);
		Assert.assertEquals("Single portfolio std", profile.getStandardDeviation(), portfolioStdDev, 0.0005);
	}

	@Test
	public void testTwoAssetPortfolio() {
		PortfolioBuilder<String> builder = new PortfolioBuilder<>();
		builder.add("GAZP", MoexPriceReader.readLastPrices(new CharArrayReader(GAZP.toCharArray())));
		builder.add("LSR", MoexPriceReader.readLastPrices(new CharArrayReader(LSR.toCharArray())));
		Portfolio<String> portfolio = builder.build();

		double weight = 0.5;
		BigDecimal[] allocation = new BigDecimal[]{new BigDecimal(weight), new BigDecimal(weight)};
		double portfolioReturn = portfolio.calculateAllocationReturn(allocation);
		double portfolioStdDev = portfolio.calculateAllocationStandardDeviation(allocation);

		AssetProfile<String> profile1 = portfolio.getProfile("GAZP");
		AssetProfile<String> profile2 = portfolio.getProfile("LSR");
		Assert.assertEquals("Two assets portfolio return", (profile1.getAverageReturn() + profile2.getAverageReturn()) / 2, portfolioReturn, 0.0005);

		double weightSq = Math.pow(weight, 2);
		double correlationCoef = portfolio.getCorrelationCoefficient("GAZP", "LSR");
		double variance = weightSq * profile1.getVariance()
				+ weightSq * profile2.getVariance()
				+ 2 * weight * weight * profile1.getStandardDeviation() * profile2.getStandardDeviation() * correlationCoef;
		double stdDev = MathUtils.round(Math.sqrt(variance));
		Assert.assertEquals("Two assets portfolio std", stdDev, portfolioStdDev, 0.0005);
	}

	@Test
	public void testCorrelationMatrix() {
		Portfolio<String> portfolio = createPortfolio();
		double[][] matrix = portfolio.getCorrelationMatrix();
		List<String> titles = portfolio.getAssetKeys();
		int matrixSize = matrix.length;
		Assert.assertEquals("Title count", matrixSize, titles.size());
		// print for debug purposes
		for (int r = 0, nr = titles.size() + 1; r < nr; r++) {
			for (int c = 0, n = titles.size() + 1; c < n; c++) {
				if (r == 0) {
					if (c == 0) {
						System.out.print("\t\t");
					} else {
						System.out.print(titles.get(c - 1) + "\t\t");
					}
				}
				if (r > 0) {
					int matrixRow = r - 1;
					if (c == 0) {
						System.out.print(titles.get(matrixRow) + "\t\t");
					} else {
						System.out.print(matrix[matrixRow][c - 1] + "\t\t");
					}
				}
			}
			System.out.println("");
		}

		// check matrix consistency
		for (int row = 0; row < matrixSize; row++) {
			double[] matrixRow = matrix[row];
			Assert.assertEquals("Column count must be equal to row count", matrixSize, matrixRow.length);
			for (int col = 0; col < matrixSize; col++) {
				if (row == col) {
					Assert.assertEquals("Correlation to itself is 1", 1, matrixRow[col], 0.0005);
				} else {
					double mirrorValue = matrix[col][row];
					Assert.assertEquals("Correlation of the same two assets", mirrorValue, matrixRow[col], 0.0005);
				}
			}
		}
	}

	@Test
	public void testRatesConversion() {
		double annualRate = 0.05;
		double dailyRate = PortfolioUtils.convertAnnualRateToDaily(annualRate);
		double restoredAnnualRate = PortfolioUtils.convertDailyRateToAnnual(dailyRate);
		Assert.assertEquals("Annual rate", annualRate, restoredAnnualRate, 0.0005);
	}

	@Test
	public void testDateConversion() {
		Assert.assertEquals("Local date", LocalDate.of(2020, 4, 5), PortfolioUtils.intToLocalDate(20200405));
		Assert.assertEquals("Local date", LocalDate.of(2015, 11, 19), PortfolioUtils.intToLocalDate(20151119));
	}

	private Portfolio<String> createPortfolio() {
		PortfolioBuilder<String> builder = new PortfolioBuilder<>();
		builder.add("VSMO", MoexPriceReader.readLastPrices(new CharArrayReader(VSMO.toCharArray())));
		builder.add("LSR", MoexPriceReader.readLastPrices(new CharArrayReader(LSR.toCharArray())));
		builder.add("GAZP", MoexPriceReader.readLastPrices(new CharArrayReader(GAZP.toCharArray())));
		builder.add("MTS", MoexPriceReader.readLastPrices(new CharArrayReader(MTS.toCharArray())));
		builder.add("Fosagro", MoexPriceReader.readLastPrices(new CharArrayReader(PHOR.toCharArray())));
		return builder.build();
	}


	// ================= MOEX data =========================


	private static String VSMO =
					"2016-11-01\tVSMO\t44\t2 112 370\t12 880\t12 860\t12 860\t12 930\t12 870\n" +
					"2016-11-02\tVSMO\t61\t1 773 800\t12 950\t12 910\t12 860\t13 030\t12 870\n" +
					"2016-11-03\tVSMO\t36\t924 400\t12 840\t12 820\t12 820\t12 890\t12 860\n" +
					"2016-11-07\tVSMO\t51\t1 400 160\t12 850\t12 850\t12 820\t12 910\t12 880\n" +
					"2016-11-08\tVSMO\t68\t1 952 290\t12 840\t12 880\t12 800\t12 910\t12 910\n" +
					"2016-11-09\tVSMO\t65\t1 471 890\t12 910\t12 800\t12 800\t12 980\t12 940\n" +
					"2016-11-10\tVSMO\t124\t3 777 770\t12 890\t12 910\t12 800\t12 990\t12 960\n" +
					"2016-11-11\tVSMO\t71\t1 570 950\t12 980\t12 920\t12 910\t13 040\t12 960\n" +
					"2016-11-14\tVSMO\t57\t1 892 490\t13 050\t12 930\t12 930\t13 150\t13 120\n" +
					"2016-11-15\tVSMO\t87\t2 214 650\t13 260\t13 170\t13 120\t13 380\t13 380\n" +
					"2016-11-16\tVSMO\t59\t1 758 180\t13 420\t13 390\t13 390\t13 470\t13 430\n" +
					"2016-11-17\tVSMO\t18\t737 390\t13 410\t13 400\t13 350\t13 430\t13 430\n" +
					"2016-11-18\tVSMO\t34\t855 590\t13 370\t13 410\t13 310\t13 430\t13 430\n" +
					"2016-11-21\tVSMO\t45\t2 602 900\t13 490\t13 430\t13 430\t13 500\t13 500\n" +
					"2016-11-22\tVSMO\t49\t1 235 750\t13 580\t13 480\t13 470\t13 630\t13 590\n" +
					"2016-11-23\tVSMO\t43\t1 012 570\t13 680\t13 570\t13 570\t13 740\t13 730\n" +
					"2016-11-24\tVSMO\t73\t1 553 230\t13 750\t13 730\t13 690\t13 820\t13 800\n" +
					"2016-11-25\tVSMO\t189\t4 640 030\t13 530\t13 730\t13 050\t13 820\t13 650\n" +
					"2016-11-28\tVSMO\t72\t1 616 030\t13 700\t13 700\t13 600\t13 770\t13 690\n" +
					"2016-11-29\tVSMO\t95\t2 855 450\t13 860\t13 720\t13 720\t13 950\t13 870";

	private static final String GAZP =
					"2016-11-01\tGAZP\t55 399\t6 943 744 566\t140,48\t139,12\t139,01\t142,44\t141,98\n" +
					"2016-11-02\tGAZP\t60 018\t6 122 499 695\t141,88\t141,49\t140,03\t143,64\t140,81\n" +
					"2016-11-03\tGAZP\t31 942\t3 485 709 964\t140,12\t140,5\t139,3\t141,3\t139,9\n" +
					"2016-11-07\tGAZP\t35 842\t3 283 526 909\t139,34\t140,5\t138,5\t140,96\t138,5\n" +
					"2016-11-08\tGAZP\t35 903\t2 916 544 955\t139,9\t138,86\t138,81\t140,98\t139,32\n" +
					"2016-11-09\tGAZP\t94 500\t11 343 564 179\t142,82\t138\t137,55\t145,62\t145,62\n" +
					"2016-11-10\tGAZP\t103 501\t15 388 700 308\t149,09\t146,45\t145,75\t151,3\t147,4\n" +
					"2016-11-11\tGAZP\t49 248\t6 721 771 942\t147,98\t147,4\t145,79\t149,5\t147,88\n" +
					"2016-11-14\tGAZP\t40 479\t4 661 568 705\t147,27\t148,6\t145,96\t149,67\t146,26\n" +
					"2016-11-15\tGAZP\t29 864\t3 488 436 602\t146,71\t146,5\t145,6\t148\t145,9\n" +
					"2016-11-16\tGAZP\t48 761\t6 018 316 510\t147,09\t147,07\t145,14\t148,8\t145,5\n" +
					"2016-11-17\tGAZP\t34 568\t4 151 412 695\t146,4\t145,82\t145,32\t147,78\t147,7\n" +
					"2016-11-18\tGAZP\t21 273\t2 159 377 689\t147,42\t147,51\t146,74\t148,29\t146,96\n" +
					"2016-11-21\tGAZP\t31 170\t3 896 421 482\t148,94\t147,6\t147,6\t149,85\t149,6\n" +
					"2016-11-22\tGAZP\t29 135\t3 986 691 592\t149,65\t150,2\t149,06\t150,83\t150\n" +
					"2016-11-23\tGAZP\t24 536\t2 891 139 492\t149,58\t150,24\t148,56\t150,38\t149,49\n" +
					"2016-11-24\tGAZP\t30 089\t3 951 318 727\t150,36\t149,53\t149,35\t152,25\t151,11\n" +
					"2016-11-25\tGAZP\t23 110\t2 698 302 674\t150,45\t150,94\t149,5\t151,6\t151,31\n" +
					"2016-11-28\tGAZP\t27 899\t3 643 476 891\t149,24\t151,18\t148,39\t151,48\t148,94\n" +
					"2016-11-29\tGAZP\t29 304\t3 141 157 935\t148,72\t148,83\t147,34\t149,49\t147,75\n";

	private static final String MTS =
					"2016-11-01\tMTSS\t8 728\t746 758 586\t223,95\t223,9\t222,35\t225\t223,7\n" +
					"2016-11-02\tMTSS\t8 244\t495 775 922\t221,9\t223\t220,2\t224,5\t220,6\n" +
					"2016-11-03\tMTSS\t5 881\t536 196 769\t220,1\t220,7\t218,6\t222\t220,5\n" +
					"2016-11-07\tMTSS\t4 968\t253 835 601\t219,65\t220\t217,75\t221,65\t220,8\n" +
					"2016-11-08\tMTSS\t5 032\t270 912 541\t219,75\t219,7\t218,25\t222\t219,2\n" +
					"2016-11-09\tMTSS\t8 513\t403 830 075\t219,65\t215,2\t215,2\t222\t220,7\n" +
					"2016-11-10\tMTSS\t12 671\t1 123 360 278\t225,25\t222,5\t221,15\t228,65\t224,3\n" +
					"2016-11-11\tMTSS\t6 244\t335 517 747\t221,95\t223,6\t220,5\t224,55\t221,9\n" +
					"2016-11-14\tMTSS\t4 585\t217 222 397\t221,75\t221,9\t220,5\t223,5\t221,55\n" +
					"2016-11-15\tMTSS\t5 571\t260 893 504\t219,65\t221,5\t218,6\t221,65\t220,25\n" +
					"2016-11-16\tMTSS\t5 081\t316 740 761\t220,85\t220,95\t218,7\t222,45\t221,6\n" +
					"2016-11-17\tMTSS\t4 876\t284 190 220\t221,35\t221,95\t219,9\t223,1\t220,8\n" +
					"2016-11-18\tMTSS\t5 354\t414 064 069\t220,3\t221,1\t219\t221,9\t220,5\n" +
					"2016-11-21\tMTSS\t5 905\t503 636 779\t222,45\t220,15\t220,15\t224,45\t224,45\n" +
					"2016-11-22\tMTSS\t9 227\t773 891 758\t225,5\t224,7\t222,6\t227,75\t223,5\n" +
					"2016-11-23\tMTSS\t8 233\t632 647 778\t225\t223,85\t223,45\t226,85\t225,7\n" +
					"2016-11-24\tMTSS\t8 290\t608 799 566\t227,85\t225,95\t225,95\t228,9\t228,2\n" +
					"2016-11-25\tMTSS\t4 794\t476 297 091\t228,55\t228\t226\t230,4\t227,5\n" +
					"2016-11-28\tMTSS\t6 039\t195 037 218\t228,7\t228,45\t226,7\t229,4\t229,3\n" +
					"2016-11-29\tMTSS\t4 897\t384 097 184\t230,4\t229,1\t228,15\t231,9\t229,55";

	private static final String LSR =
			"2016-11-01\tLSRG\t833\t67 728 648\t908,5\t877\t877\t925,5\t906,5\n" +
					"2016-11-02\tLSRG\t234\t3 839 607\t901,5\t905\t890\t919,5\t900\n" +
					"2016-11-03\tLSRG\t100\t664 837\t897\t900,5\t893,5\t903,5\t894\n" +
					"2016-11-07\tLSRG\t194\t4 313 655\t888\t898\t880,5\t898\t883\n" +
					"2016-11-08\tLSRG\t361\t7 269 225\t894,5\t883\t883\t905,5\t883,5\n" +
					"2016-11-09\tLSRG\t340\t15 927 506\t900,5\t880\t880\t914\t903,5\n" +
					"2016-11-10\tLSRG\t476\t16 000 756\t904,5\t906\t884,5\t913,5\t905\n" +
					"2016-11-11\tLSRG\t1 442\t50 908 496\t915,5\t910\t908\t921,5\t914,5\n" +
					"2016-11-14\tLSRG\t265\t3 568 534\t899,5\t916\t891,5\t921\t895\n" +
					"2016-11-15\tLSRG\t225\t4 707 565\t900\t895,5\t885\t906,5\t898\n" +
					"2016-11-16\tLSRG\t205\t2 267 372\t893,5\t899,5\t889\t903\t892,5\n" +
					"2016-11-17\tLSRG\t160\t2 183 264\t895,5\t894,5\t888,5\t901\t894,5\n" +
					"2016-11-18\tLSRG\t347\t7 072 728\t888,5\t895\t881,5\t900\t883,5\n" +
					"2016-11-21\tLSRG\t440\t10 494 103\t881,5\t887,5\t864\t892\t885,5\n" +
					"2016-11-22\tLSRG\t595\t17 760 153\t873\t888,5\t867\t891\t874\n" +
					"2016-11-23\tLSRG\t328\t5 401 057\t884\t880\t875\t889,5\t888,5\n" +
					"2016-11-24\tLSRG\t249\t3 234 584\t885\t890\t878\t898,5\t883,5\n" +
					"2016-11-25\tLSRG\t208\t14 731 900\t888\t881,5\t881\t892\t892\n" +
					"2016-11-28\tLSRG\t373\t5 776 134\t899,5\t894,5\t892,5\t902,5\t897\n" +
					"2016-11-29\tLSRG\t529\t36 108 497\t905\t900\t898,5\t915\t915\n";

	private static final String PHOR =
			"2016-11-01\tPHOR\t5 102\t38 023 726\t2 309\t2 369\t2 290\t2 369\t2 313\n" +
					"2016-11-02\tPHOR\t720\t15 789 539\t2 311\t2 317\t2 295\t2 334\t2 295\n" +
					"2016-11-03\tPHOR\t804\t17 409 891\t2 265\t2 304\t2 240\t2 326\t2 250\n" +
					"2016-11-07\tPHOR\t550\t9 652 726\t2 240\t2 255\t2 224\t2 262\t2 259\n" +
					"2016-11-08\tPHOR\t784\t26 523 639\t2 322\t2 246\t2 246\t2 356\t2 331\n" +
					"2016-11-09\tPHOR\t421\t5 951 727\t2 332\t2 330\t2 307\t2 353\t2 340\n" +
					"2016-11-10\tPHOR\t504\t19 775 447\t2 360\t2 340\t2 332\t2 377\t2 370\n" +
					"2016-11-11\tPHOR\t999\t23 632 535\t2 406\t2 380\t2 340\t2 450\t2 416\n" +
					"2016-11-14\tPHOR\t447\t16 934 020\t2 435\t2 409\t2 409\t2 449\t2 437\n" +
					"2016-11-15\tPHOR\t519\t13 169 481\t2 465\t2 420\t2 420\t2 490\t2 469\n" +
					"2016-11-16\tPHOR\t407\t12 421 785\t2 471\t2 480\t2 451\t2 487\t2 454\n" +
					"2016-11-17\tPHOR\t726\t27 044 593\t2 496\t2 454\t2 454\t2 507\t2 495\n" +
					"2016-11-18\tPHOR\t724\t16 212 416\t2 512\t2 503\t2 485\t2 535\t2 492\n" +
					"2016-11-21\tPHOR\t808\t20 200 628\t2 532\t2 487\t2 486\t2 550\t2 538\n" +
					"2016-11-22\tPHOR\t1 069\t27 915 894\t2 534\t2 547\t2 505\t2 561\t2 540\n" +
					"2016-11-23\tPHOR\t882\t22 972 275\t2 577\t2 531\t2 530\t2 600\t2 589\n" +
					"2016-11-24\tPHOR\t429\t10 594 693\t2 578\t2 582\t2 560\t2 601\t2 571\n" +
					"2016-11-25\tPHOR\t570\t14 639 315\t2 558\t2 571\t2 522\t2 590\t2 567\n" +
					"2016-11-28\tPHOR\t313\t8 072 925\t2 571\t2 542\t2 542\t2 592\t2 579\n" +
					"2016-11-29\tPHOR\t500\t12 475 488\t2 548\t2 581\t2 527\t2 586\t2 542\n";
}
