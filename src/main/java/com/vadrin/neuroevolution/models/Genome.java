package com.vadrin.neuroevolution.models;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

	public void setFitnessScore(double fitnessScore) {
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

	public ConnectionGene getConnectionGene(String id) {
		return this.connectionGenes.stream().filter(c -> c.getId().equalsIgnoreCase(id)).findFirst().get();
	}

	public NodeGene getNodeGene(String id) {
		return nodeGenes.stream().filter(n -> n.getId().equalsIgnoreCase(id)).findFirst().get();
	}

	public List<NodeGene> getNodeGenesSorted(NodeGeneType type) {
		return this.nodeGenes.stream().filter(nodeGen -> (nodeGen.getType() == type))
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber()))
				.collect(Collectors.toList());
	}

	public List<NodeGene> getNodeGenesSorted() {
		return this.nodeGenes.stream()
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber()))
				.collect(Collectors.toList());
	}

	public List<ConnectionGene> getConnectionGenesSorted() {
		return this.connectionGenes.stream()
				.sorted((a, b) -> Integer.compare(a.getReferenceInnovationNumber(), b.getReferenceInnovationNumber()))
				.collect(Collectors.toList());
	}

	public boolean isConnectionPresentBetweenNodes(String fromNodeId, String toNodeId) {
		return connectionGenes.stream().anyMatch(c -> c.getFromNode().getId().equalsIgnoreCase(fromNodeId)
				&& c.getFromNode().getId().equalsIgnoreCase(toNodeId));
	}

	public void addConnection(ConnectionGene toAdd) {
		this.connectionGenes.add(toAdd);
	}

	public void addNode(NodeGene newNodeGene) {
		this.nodeGenes.add(newNodeGene);
	}

}
