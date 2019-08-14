package com.vadrin.neuroevolution.models;

public class ConnectionGene extends Gene {

	private double weight;
	private boolean enabled;
	private int fromNodeReferenceKey;
	private int toNodeReferenceKey;
	private int referenceInnovationNumber;

	public double getWeight() {
		return weight;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getFromNodeReferenceKey() {
		return fromNodeReferenceKey;
	}

	public int getToNodeReferenceKey() {
		return toNodeReferenceKey;
	}

	public int getReferenceInnovationNumber() {
		return referenceInnovationNumber;
	}

	public ConnectionGene(double weight, boolean enabled, int fromNodeReferenceKey, int toNodeReferenceKey,
			int referenceInnovationNumber) {
		super();
		this.weight = weight;
		this.enabled = enabled;
		this.fromNodeReferenceKey = fromNodeReferenceKey;
		this.toNodeReferenceKey = toNodeReferenceKey;
		this.referenceInnovationNumber = referenceInnovationNumber;
	}

}
