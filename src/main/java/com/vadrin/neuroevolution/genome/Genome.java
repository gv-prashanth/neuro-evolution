package com.vadrin.neuroevolution.genome;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.vadrin.neuroevolution.commons.NodeGeneType;

public class Genome {

	private Set<NodeGene> nodeGenes;
	private Set<ConnectionGene> connectionGenes;
	private double fitnessScore;
	private int referenceSpeciesNumber;
	private String id;

	public Genome(Set<NodeGene> nodeGenes, Set<ConnectionGene> connectionGenes) {
		super();
		this.nodeGenes = nodeGenes;
		this.connectionGenes = connectionGenes;
		this.fitnessScore = 0;
		this.referenceSpeciesNumber = 0;
		this.id = UUID.randomUUID().toString();
	}

	public double getFitnessScore() {
		return fitnessScore;
	}

	protected void setFitnessScore(double fitnessScore) {
		this.fitnessScore = fitnessScore;
	}

	public int getReferenceSpeciesNumber() {
		return referenceSpeciesNumber;
	}

	public void setReferenceSpeciesNumber(int referenceSpeciesNumber) {
		this.referenceSpeciesNumber = referenceSpeciesNumber;
	}

	public String getId() {
		return id;
	}

	public List<NodeGene> getSortedNodeGenes(NodeGeneType type) {
		return this.nodeGenes.stream().filter(nodeGen -> (nodeGen.getType() == type))
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber()))
				.collect(Collectors.toList());
	}

	public List<NodeGene> getSortedNodeGenes() {
		return this.nodeGenes.stream()
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber()))
				.collect(Collectors.toList());
	}

	public List<ConnectionGene> getSortedConnectionGenes() {
		return this.connectionGenes.stream()
				.sorted((a, b) -> Integer.compare(a.getReferenceInnovationNumber(), b.getReferenceInnovationNumber()))
				.collect(Collectors.toList());
	}

	protected void addConnectionGene(ConnectionGene toAdd) {
		this.connectionGenes.add(toAdd);
	}

	protected void addNodeGene(NodeGene toAdd) {
		this.nodeGenes.add(toAdd);
	}
}
