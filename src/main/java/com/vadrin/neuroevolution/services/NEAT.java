package com.vadrin.neuroevolution.services;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.MutationType;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;
import com.vadrin.neuroevolution.models.exceptions.ReferenceInnovaationNumberDoesNotExistException;

public class NEAT {

	private Set<Genome> genomes;
	private int referenceInnovationCounter;
	private int referenceNodeCounter;

	private final int poolSize;
	private final int inputNodesSize;
	private final int outputNodesSize;

	private static final int POPSIZE = 150;// needs to be depricated and used from constructed
	private static final double C1 = 1.0;
	private static final double C2 = 1.0;
	private static final double C3 = 0.4;
	private static final double DELTAT = 3.0;
	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;
	private static final int NUMBEROFCHAMPIONSTOGETWILDCARDENTRYTONEXTGENERATION = 1; // ASSUSMING GENOMES IN SPECIES IS
																						// > 5
	private static final double CHANCEFORWEIGHTMUTATION = 0.8; // 0.8 MEANS 80%
	private static final double CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT = 0.1; // 0.1 MEANS 10%
	private static final double CHANCEFORWEIGHTMUTATIONWITHSMALLPERTUBED = 1
			- CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT;
	private static final double PERTUBEDVARIANCEDIFFERENCE = 0.05;
	private static final double CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS = 0.75; // 0.8 MEANS 80%
	private static final double CHANCEFOROFFSPRINGFROMMUTATIONALONEWITHOUTCROSSOVER = 0.25; // 0.8 MEANS 80%
	private static final double CHANCEFORINTERSPECIESMATING = 0.001;
	private static final double CHANCEFORADDINGNEWNODE = 0.03;
	private static final double CHANCEFORTOGGLEENABLEDISABLE = 0.03;
	private static final double CHANCEFORADDINGNEWCONNECTION = 0.05;
	private static final double RANDOMWEIGHTLOWERBOUND = -2;
	private static final double RANDOMWEIGHTUPPERBOUND = -2;

	public NEAT(int poolSize, int inputNodesSize, int outputNodesSize) {
		super();
		this.referenceInnovationCounter = 1;
		this.referenceNodeCounter = 1;
		this.genomes = new HashSet<Genome>();
		this.poolSize = poolSize;
		this.inputNodesSize = inputNodesSize;
		this.outputNodesSize = outputNodesSize;
		loadAllGenomesWithInputOutputNodeGenesAndEmptyConnectionGenes();
	}
	
	public void process() {
		fitBattle();
		speciate();
		select();
		crossOver();
		mutate();
	}

	public void speciate() {
		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			genome.setSpeciesId("TODBABY");
		}
	}

	public void mutate() {
		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			Arrays.asList(MutationType.class.getEnumConstants()).stream()
					.forEach((mutationType) -> mutate(genome, mutationType));
		}
	}
	
	public void crossOver() {
		
	}

	private Genome crossOver(Genome genomeOne, Genome genomeTwo) {
		return genomeTwo;
	}
	
	public void select() {
		
	}
	
	public void fitBattle() {
		
	}

	private void mutate(Genome genome, MutationType mutationType) {
		switch (mutationType) {
		case ADDCONNECTIONGENE:
			mutationAddConnectionGene(genome);
			break;
		case ADDNODEGENE:
			mutationAddNodeGene(genome);
			break;
		case ALTERWEIGHTOFCONNECTIONGENE:
			mutationAlterWeightOfConnectionGene(genome);
			break;
		case ENABLEDISABLECONNECTIONGENE:
			mutationEnableDisableConnectionGene(genome);
			break;
		}
	}

	private void mutationEnableDisableConnectionGene(Genome genome) {
		genome.getConnectionGenes().forEach((connectionGene) -> {
			if (connectionGene.isLucky(CHANCEFORTOGGLEENABLEDISABLE)) {
				connectionGene.setEnabled(!connectionGene.isEnabled());
			}
		});
	}

	private void mutationAlterWeightOfConnectionGene(Genome genome) {
		genome.getConnectionGenes().forEach((connectionGene) -> {
			if (connectionGene.isLucky(CHANCEFORWEIGHTMUTATION)) {
				if (connectionGene.isLucky(CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT)) {
					connectionGene.setWeight(randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
				} else {
					connectionGene.setWeight(connectionGene.getWeight()
							* randomNumber(1 - PERTUBEDVARIANCEDIFFERENCE, 1 + PERTUBEDVARIANCEDIFFERENCE));
				}
			}
		});
	}

	private void mutationAddNodeGene(Genome genome) {
		genome.getConnectionGenes().forEach((connectionGene) -> {
			if (connectionGene.isLucky(CHANCEFORADDINGNEWNODE)) {
				// This connection will get a new node in between now.
				NodeGene newNodeGene = constructNewNodeGene();
				connectionGene.setEnabled(false);
				constructNewConnectionGene(1.0, true, connectionGene.getFromReferenceNodeNumber(),
						newNodeGene.getReferenceNodeNumber());
				constructNewConnectionGene(connectionGene.getWeight(), true, newNodeGene.getReferenceNodeNumber(),
						connectionGene.getToReferenceNodeNumber());
			}
		});
	}

	private void mutationAddConnectionGene(Genome genome) {
		Set<NodeGene> luckyPairs = new HashSet<NodeGene>();
		genome.getNodeGenes().forEach((nodeGene) -> {
			if (nodeGene.isLucky(CHANCEFORADDINGNEWCONNECTION)) {
				luckyPairs.add(nodeGene);
			}
		});
		Iterator<NodeGene> luckyPairsIterator = luckyPairs.iterator();
		while (luckyPairsIterator.hasNext()) {
			NodeGene from = luckyPairsIterator.next();
			if (luckyPairsIterator.hasNext()) {
				NodeGene to = luckyPairsIterator.next();
				// To make sure we dont join input to input or output to output
				if ((from.getType() != to.getType())
						|| (from.getType() == to.getType() && from.getType() == NodeGeneType.HIDDEN)) {
					constructNewConnectionGene(randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), true,
							from.getReferenceNodeNumber(), to.getReferenceNodeNumber());
				}
			}
		}
	}

	private void loadAllGenomesWithInputOutputNodeGenesAndEmptyConnectionGenes() {
		for (int i = 0; i < poolSize; i++) {
			Set<NodeGene> nodeGenes = new HashSet<NodeGene>();
			for (int j = 1; j <= inputNodesSize; j++) {
				NodeGene nodeGene = new NodeGene(j, NodeGeneType.INPUT);
				nodeGenes.add(nodeGene);
			}
			for (int j = 1; j <= outputNodesSize; j++) {
				NodeGene nodeGene = new NodeGene(inputNodesSize + j, NodeGeneType.OUTPUT);
				nodeGenes.add(nodeGene);
			}
			// Instatiate Empty Connection Genes
			Set<ConnectionGene> connectionGenes = new HashSet<ConnectionGene>();
			Genome genome = new Genome(nodeGenes, connectionGenes);
			genomes.add(genome);
		}
		referenceNodeCounter = inputNodesSize + outputNodesSize;
	}

	// There seems to be a flaw here. What if genome1 decided to mutate a connection
	// and construct a new node between nodeA & nodeB say nodeX is created. And
	// after a while if genome2 also decides to mutate the same connection and
	// cosntruct a new node between nodeA & nodeB, then we have a flaw that rather
	// than reusing nodeX we are constructing a new nodeY which i reality should be
	// using nodeX only. How can we solve this problem?
	private NodeGene constructNewNodeGene() {
		referenceNodeCounter++;
		NodeGene nodeGene = new NodeGene(referenceNodeCounter, NodeGeneType.HIDDEN);
		return nodeGene;
	}

	private ConnectionGene constructNewConnectionGene(double weight, boolean enabled, int fromReferenceNodeNumber,
			int toReferenceNodeNumber) {
		if (fromReferenceNodeNumber > toReferenceNodeNumber) {
			System.out.println("WRONG FROM AND TO ---> AUTO FIXING!");
			int temp = fromReferenceNodeNumber;
			fromReferenceNodeNumber = toReferenceNodeNumber;
			toReferenceNodeNumber = temp;
		}
		try {
			int innovationNumberForThisConnectionGene = requestForConnectionGeneReferenceInnovationNumber(
					fromReferenceNodeNumber, toReferenceNodeNumber);
			return new ConnectionGene(weight, enabled, fromReferenceNodeNumber, toReferenceNodeNumber,
					innovationNumberForThisConnectionGene);
		} catch (ReferenceInnovaationNumberDoesNotExistException e) {
			referenceInnovationCounter++;
			return new ConnectionGene(weight, enabled, fromReferenceNodeNumber, toReferenceNodeNumber,
					referenceInnovationCounter);
		}
	}

	private int requestForConnectionGeneReferenceInnovationNumber(int fromReferenceNodeNumber,
			int toReferenceNodeNumber) throws ReferenceInnovaationNumberDoesNotExistException {
		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			try {
				Genome genome = iterator.next();
				return genome.getConnectionGenes().stream().filter(
						(connectionGene) -> (connectionGene.getFromReferenceNodeNumber() == fromReferenceNodeNumber
								&& connectionGene.getToReferenceNodeNumber() == toReferenceNodeNumber))
						.findAny().get().getReferenceInnovationNumber();
			} catch (NoSuchElementException e) {

			}
		}
		throw new ReferenceInnovaationNumberDoesNotExistException();
	}

	private double randomNumber(double rangeMin, double rangeMax) {
		Random r = new Random();
		double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
		return randomValue;
	}

}
