package com.vadrin.neuroevolution.models;

import java.util.UUID;

public abstract class Gene {

	private String id;

	public boolean isLucky(double chance) {
		return (Math.random() < chance);
	}

	public Gene() {
		super();
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

}
