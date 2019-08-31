package com.vadrin.neuroevolution.services;

import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class MathService {

	public double randomNumber(double rangeMin, double rangeMax) {
		Random r = new Random();
		double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
		return randomValue;
	}

	public double applySigmiodActivationFunction(double input) {
		return 1d / (1d + Math.exp(-4.9d*input));
	}

}
