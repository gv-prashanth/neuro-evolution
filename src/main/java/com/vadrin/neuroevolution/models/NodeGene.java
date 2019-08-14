package com.vadrin.neuroevolution.models;

public class NodeGene extends Gene {

	private int referenceNodeNumber;
	private NodeGeneType type;

	public int getNodeReferenceKey() {
		return referenceNodeNumber;
	}

	public NodeGeneType getType() {
		return type;
	}

	public NodeGene(int nodeReferenceKey, NodeGeneType type) {
		super();
		this.referenceNodeNumber = nodeReferenceKey;
		this.type = type;
	}

}
