package com.vadrin.neuroevolution.models;

public class ConnectionGeneTemplate {
	private NodeGene fromNode;
	private NodeGene toNode;
	private int innovationNumber;

	public NodeGene getFromNode() {
		return fromNode;
	}

	public NodeGene getToNode() {
		return toNode;
	}

	public int getInnovationNumber() {
		return innovationNumber;
	}

	public ConnectionGeneTemplate(NodeGene fromNode, NodeGene toNode, int innovationNumber) {
		super();
		this.fromNode = fromNode;
		this.toNode = toNode;
		this.innovationNumber = innovationNumber;
	}

}
