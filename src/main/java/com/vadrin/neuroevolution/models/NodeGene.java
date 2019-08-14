package com.vadrin.neuroevolution.models;

public class NodeGene extends Gene {

	private int referenceNodeNumber;
	private NodeGeneType type;

	public NodeGeneType getType() {
		return type;
	}

	public int getReferenceNodeNumber() {
		return referenceNodeNumber;
	}

	public NodeGene(int referenceNodeKey, NodeGeneType type) {
		super();
		this.referenceNodeNumber = referenceNodeKey;
		this.type = type;
	}

}
