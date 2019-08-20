package com.vadrin.neuroevolution.genome;

import com.vadrin.neuroevolution.commons.NodeGeneType;

public class NodeGene extends Gene {

	private int referenceNodeNumber;
	private NodeGeneType type;
	private double output;

	public NodeGeneType getType() {
		return type;
	}

	public int getReferenceNodeNumber() {
		return referenceNodeNumber;
	}

	protected NodeGene(int referenceNodeKey, NodeGeneType type) {
		super();
		this.referenceNodeNumber = referenceNodeKey;
		this.type = type;
	}

	public double getOutput() {
		return output;
	}

	public void setOutput(double output) {
		this.output = output;
	}

}
