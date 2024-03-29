package com.vadrin.neuroevolution.services;

import java.util.Random;

public class MathService {

	public static double randomNumber(double rangeMin, double rangeMax) {
		Random r = new Random();
		double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
		return randomValue;
	}

	public static double applySigmiodActivationFunction(double input) {
		return 1d / (1d + Math.exp(-4.9d * input));
	}

}
