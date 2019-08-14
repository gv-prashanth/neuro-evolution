package com.vadrin.neuroevolution.models;

public abstract class Gene {

	public boolean isLucky(double chance) {
		return (Math.random() < chance);
	}
}
