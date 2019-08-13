package com.vadrin.neuroevolution.models;

public class ConnectionGene extends Gene {

	private double weight;
	private boolean enabled;
	private ConnectionGeneTemplate connectionGeneTemplate;

	public ConnectionGene(double weight, boolean enabled, ConnectionGeneTemplate connectionGeneTemplate) {
		super();
		this.weight = weight;
		this.enabled = enabled;
		this.connectionGeneTemplate = connectionGeneTemplate;
	}

	public double getWeight() {
		return weight;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public ConnectionGeneTemplate getConnectionGeneTemplate() {
		return connectionGeneTemplate;
	}

}
