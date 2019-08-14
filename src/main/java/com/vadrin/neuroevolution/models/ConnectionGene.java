package com.vadrin.neuroevolution.models;

public class ConnectionGene extends Gene {

	private double weight;
	private boolean enabled;
	private int fromReferenceNodeNumber;
	private int toReferenceNodeNumber;
	private int referenceInnovationNumber;

	public double getWeight() {
		return weight;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public int getReferenceInnovationNumber() {
		return referenceInnovationNumber;
	}

	public int getFromReferenceNodeNumber() {
		return fromReferenceNodeNumber;
	}

	public int getToReferenceNodeNumber() {
		return toReferenceNodeNumber;
	}

	public ConnectionGene(double weight, boolean enabled, int fromReferenceNodeNumber, int toReferenceNodeNumber,
			int referenceInnovationNumber) {
		super();
		this.weight = weight;
		this.enabled = enabled;
		this.fromReferenceNodeNumber = fromReferenceNodeNumber;
		this.toReferenceNodeNumber = toReferenceNodeNumber;
		this.referenceInnovationNumber = referenceInnovationNumber;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
