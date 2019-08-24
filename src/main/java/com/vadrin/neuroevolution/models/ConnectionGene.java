package com.vadrin.neuroevolution.models;

public class ConnectionGene extends Gene {

	private double weight;
	private boolean enabled;
	private NodeGene fromNode;
	private NodeGene toNode;
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

	public NodeGene getFromNode() {
		return fromNode;
	}

	public NodeGene getToNode() {
		return toNode;
	}

	public ConnectionGene(double weight, boolean enabled, NodeGene fromNode, NodeGene toNode,
			int referenceInnovationNumber) {
		super();
		this.weight = weight;
		this.enabled = enabled;
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.referenceInnovationNumber = referenceInnovationNumber;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
