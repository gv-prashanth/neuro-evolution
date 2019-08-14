package com.vadrin.neuroevolution.models;

import java.util.Set;

public class Genome {

	private Set<NodeGene> nodeGenes;
	private Set<ConnectionGene> connectionGenes;
	private double fitnessScore;
	private String speciesId;

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
		this.fitnessScore = 0;
	}

	public double getFitnessScore() {
		return fitnessScore;
	}

	public void setFitnessScore(double fitnessScore) {
		this.fitnessScore = fitnessScore;
	}

	public String getSpeciesId() {
		return speciesId;
	}

	public void setSpeciesId(String speciesId) {
		this.speciesId = speciesId;
	}

}
