package com.vadrin.neuroevolution.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.MutationType;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;
import com.vadrin.neuroevolution.models.exceptions.ConnectionGeneAlreadyPresentException;
import com.vadrin.neuroevolution.models.exceptions.NodeGeneAlreadyPresentException;

public class NEAT {

	private Set<Genome> genomes;
	private int referenceInnovationNumberCouner;
	private int referenceNodeNumberCounter;
	private int numberOfInputNodes;
	private int numberOfOutputNodes;
	private int poolSize;

	private void mutate(Genome genome, MutationType mutationType) {
		switch (mutationType) {
		case ADDCONNECTIONGENE:
			break;
		case ADDNODEGENE:
			break;
		case ALTERWEIGHTOFCONNECTIONGENE:
			break;
		case ENABLEDISABLECONNECTIONGENE:
			break;
		}
	}

	public Set<Set<Genome>> speciate(Set<Genome> allGenomes) {
		return null;
	}

	private void calculateAndSetFitness(Genome genome) {

	}

	private void createRandomStartingPool() {
		for (int i = 0; i < poolSize; i++) {
			Genome toAdd = new Genome();
			genomes.add(toAdd);
		}
	}

	public void calculateAndSetFitness(Set<Genome> allGenomes) {
		allGenomes.stream().forEach((genome) -> calculateAndSetFitness(genome));
	}

	public void mutate(Set<Genome> allGenomes) {
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ADDCONNECTIONGENE));
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ADDNODEGENE));
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ALTERWEIGHTOFCONNECTIONGENE));
		allGenomes.stream().forEach((genome) -> mutate(genome, MutationType.ENABLEDISABLECONNECTIONGENE));
	}

	public NodeGene createNewNodeGene(int referenceNodeNumber, NodeGeneType nodeGeneType)
			throws NodeGeneAlreadyPresentException {

		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome thisGenome = iterator.next();
			try {
				thisGenome.getNodeGenes().stream()
						.filter((nodeGene) -> nodeGene.getNodeReferenceKey() == referenceNodeNumber).findFirst().get();
				throw new NodeGeneAlreadyPresentException();
			} catch (NoSuchElementException e) {

			}
		}

		referenceNodeNumberCounter++;
		return new NodeGene(referenceNodeNumberCounter, nodeGeneType);
	}

	public ConnectionGene createNewConnectionGene(int fromReferenceNodeNumber, int toReferenceNodeNumber) {

		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome thisGenome = iterator.next();
			try {
				thisGenome.getConnectionGenes().stream()
						.filter((connectedGene) -> connectedGene.getFromNodeReferenceKey() == fromReferenceNodeNumber
								&& connectedGene.getToNodeReferenceKey() == toReferenceNodeNumber)
						.findFirst().get();
				throw new ConnectionGeneAlreadyPresentException();
			} catch (NoSuchElementException e) {

			}
		}
		referenceInnovationNumberCouner++;
		return new ConnectionGene(weight, enabled, fromReferenceNodeNumber, toReferenceNodeNumber,
				referenceInnovationNumberCouner);
	}

	public NEAT(int numberOfInputNodes, int numberOfOutputNodes, int poolSize) {
		super();
		this.genomes = new HashSet<Genome>();
		this.referenceNodeNumberCounter = 0;
		this.referenceInnovationNumberCouner = 0;
		this.numberOfInputNodes = numberOfInputNodes;
		this.numberOfOutputNodes = numberOfOutputNodes;
		this.poolSize = poolSize;
		createRandomStartingPool();
	}

	public Set<Genome> getGenomes() {
		return genomes;
	}

	public int getReferenceInnovationNumberCouner() {
		return referenceInnovationNumberCouner;
	}

	public int getReferenceNodeNumberCounter() {
		return referenceNodeNumberCounter;
	}

	public int getNumberOfInputNodes() {
		return numberOfInputNodes;
	}

	public int getNumberOfOutputNodes() {
		return numberOfOutputNodes;
	}

	public int getPoolSize() {
		return poolSize;
	}

}
