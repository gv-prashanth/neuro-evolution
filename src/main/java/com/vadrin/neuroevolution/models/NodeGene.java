package com.vadrin.neuroevolution.models;

public class NodeGene extends Gene {

	private NodeGeneTemplate nodeGeneTemplate;

	public NodeGene(NodeGeneTemplate nodeGeneTemplate) {
		super();
		this.nodeGeneTemplate = nodeGeneTemplate;
	}

	public NodeGeneTemplate getNodeGeneTemplate() {
		return nodeGeneTemplate;
	}

}
