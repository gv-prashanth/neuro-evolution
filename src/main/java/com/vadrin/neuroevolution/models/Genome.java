package com.vadrin.neuroevolution.models;

import java.util.Set;

public class Genome {

	private Set<NodeGene> nodeGenes;
	private Set<ConnectionGene> connectionGenes;
	private double fitnessScore;

	public Set<NodeGene> getNodeGenes() {
		return nodeGenes;
	}

	public Set<ConnectionGene> getConnectionGenes() {
		return connectionGenes;
	}

	public Genome(Set<NodeGene> nodeGenes, Set<ConnectionGene> connectionGenes) {
		super();
		this.nodeGenes = nodeGenes;
		this.connectionGenes = connectionGenes;
	}

	public double getFitnessScore() {
		return fitnessScore;
	}

	public void setFitnessScore(double fitnessScore) {
		this.fitnessScore = fitnessScore;
	}

}
