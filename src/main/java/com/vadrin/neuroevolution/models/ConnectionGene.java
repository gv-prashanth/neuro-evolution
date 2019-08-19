package com.vadrin.neuroevolution.models;

public class ConnectionGene extends Gene {

	private double weight;
	private boolean enabled;
	private String fromNodeGeneId;
	private String toNodeGeneId;
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

	public String getFromNodeGeneId() {
		return fromNodeGeneId;
	}

	public String getToNodeGeneId() {
		return toNodeGeneId;
	}

	public ConnectionGene(double weight, boolean enabled, String fromNodeGeneId, String toNodeGeneId,
			int referenceInnovationNumber) {
		super();
		this.weight = weight;
		this.enabled = enabled;
		this.fromNodeGeneId = fromNodeGeneId;
		this.toNodeGeneId = toNodeGeneId;
		this.referenceInnovationNumber = referenceInnovationNumber;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
