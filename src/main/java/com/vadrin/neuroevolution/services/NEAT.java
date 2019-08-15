package com.vadrin.neuroevolution.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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

	private final int poolSize; // Use 150 for all practical purposes
	private final int inputNodesSize;
	private final int outputNodesSize;

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
	private static final double CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS = 0.75; // 0.75 MEANS 75%
	private static final double CHANCEFOROFFSPRINGFROMMUTATIONALONEWITHOUTCROSSOVER = 0.25; // 0.25 MEANS 25%
	private static final double CHANCEFORINTERSPECIESMATING = 0.001;
	private static final double CHANCEFORADDINGNEWNODE = 0.03;
	private static final double CHANCEFORTOGGLEENABLEDISABLE = 0.03;
	private static final double CHANCEFORADDINGNEWCONNECTION = 0.05;
	private static final double RANDOMWEIGHTLOWERBOUND = -2;
	private static final double RANDOMWEIGHTUPPERBOUND = -2;
	private static final int NUMBEROFCHAMPIONSTOSELECTINEACHSPECIES = 5;
	
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
		speciate();// done
		select();// done
		crossOver();
		mutate();// done
	}

	public void speciate() {
		Iterator<Genome> iterator = genomes.iterator();
		Map<Genome, String> speciesIdToGenomeMap = new HashMap<Genome, String>();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			try {
				genome.setSpeciesId(speciesIdToGenomeMap.get(speciesIdToGenomeMap.keySet().stream()
						.filter(genomeFromMap -> areSameSpecies(genomeFromMap, genome)).findFirst().get()));
			} catch (NoSuchElementException e) {
				genome.setSpeciesId(UUID.randomUUID().toString());
				speciesIdToGenomeMap.put(genome, genome.getSpeciesId());
			}
		}
	}

	private boolean areSameSpecies(Genome genome1, Genome genome2) {
		ConnectionGene[] connectionGenes1 = new ConnectionGene[genome1.getConnectionGenes().size()];
		connectionGenes1 = genome1.getConnectionGenes().toArray(connectionGenes1);
		ConnectionGene[] connectionGenes2 = new ConnectionGene[genome2.getConnectionGenes().size()];
		connectionGenes2 = genome2.getConnectionGenes().toArray(connectionGenes2);

		Arrays.sort(connectionGenes1,
				(a, b) -> Integer.compare(a.getReferenceInnovationNumber(), b.getReferenceInnovationNumber()));
		Arrays.sort(connectionGenes2,
				(a, b) -> Integer.compare(a.getReferenceInnovationNumber(), b.getReferenceInnovationNumber()));

		if (connectionGenes1.length > 2 && connectionGenes1[0].getReferenceInnovationNumber() > connectionGenes1[1]
				.getReferenceInnovationNumber()) {
			System.out.println("BIG TROUBLE!!!!!!!!!!!!!!!");
		}

		if (connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()) {
			ConnectionGene[] temp = connectionGenes1;
			connectionGenes1 = connectionGenes2;
			connectionGenes2 = temp;
		}

		// post this line, connectiongene1 is having smaller max innovation when
		// compared to that of connectiongene2
		int connectionGene1MaxInnovationNumber = connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber();

		ConnectionGene[] ConnectionGeneMostlyEmpty1 = new ConnectionGene[connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()
								? connectionGenes1[connectionGenes1.length - 1].getReferenceInnovationNumber()
								: connectionGenes2[connectionGenes2.length - 1].getReferenceInnovationNumber()];
		ConnectionGene[] ConnectionGeneMostlyEmpty2 = new ConnectionGene[connectionGenes1[connectionGenes1.length - 1]
				.getReferenceInnovationNumber() > connectionGenes2[connectionGenes2.length - 1]
						.getReferenceInnovationNumber()
								? connectionGenes1[connectionGenes1.length - 1].getReferenceInnovationNumber()
								: connectionGenes2[connectionGenes2.length - 1].getReferenceInnovationNumber()];
		Arrays.asList(connectionGenes1).stream()
				.forEach(thisConnectionGene1Entry -> ConnectionGeneMostlyEmpty1[thisConnectionGene1Entry
						.getReferenceInnovationNumber()] = thisConnectionGene1Entry);
		Arrays.asList(connectionGenes2).stream()
				.forEach(thisConnectionGene2Entry -> ConnectionGeneMostlyEmpty2[thisConnectionGene2Entry
						.getReferenceInnovationNumber()] = thisConnectionGene2Entry);

		double E = 0;
		double D = 0;
		double W = 0;
		double w = 0;
		double N = connectionGenes2.length < 20 ? 1 : connectionGenes2.length;

		for (int i = 0; i < ConnectionGeneMostlyEmpty1.length; i++) {
			if (ConnectionGeneMostlyEmpty1[i] != null && ConnectionGeneMostlyEmpty2[i] != null) {
				w++;
				W = W + (ConnectionGeneMostlyEmpty1[i].getWeight() - ConnectionGeneMostlyEmpty2[i].getWeight());
			}
			if ((ConnectionGeneMostlyEmpty1[i] == null && ConnectionGeneMostlyEmpty2[i] != null)
					|| (ConnectionGeneMostlyEmpty2[i] == null && ConnectionGeneMostlyEmpty1[i] != null)) {
				if (i < connectionGene1MaxInnovationNumber) {
					D++;
				} else {
					E++;
				}
			}
		}

		// Average weight difference
		W = W / w;

		double deltaScore = ((C1 * E) / N) + ((C2 * D) / N) + C3 * W;
		return deltaScore < DELTAT;
	}

	public void mutate() {
		Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration = new HashMap<ConnectionGene, NodeGene>();
		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			Arrays.asList(MutationType.class.getEnumConstants()).stream()
					.forEach(mutationType -> mutate(genome, mutationType, luckyConnectionGenesInThisGeneration));
		}
	}

	public void crossOver() {

	}

	private Genome crossOver(Genome genomeOne, Genome genomeTwo) {
		return null;
	}

	public void select() {
		// Select the top X in each species
		// To start lets make the list of each species
		Iterator<Genome> iterator = genomes.iterator();
		Map<String, List<Genome>> soFarTop = new HashMap<String, List<Genome>>();
		while (iterator.hasNext()) {
			Genome thisGenome = iterator.next();
			String speciesId = thisGenome.getSpeciesId();
			if (soFarTop.containsKey(speciesId)) {
				// Species already has a map so add
				soFarTop.get(speciesId).add(thisGenome);
			} else {
				List<Genome> single = new ArrayList<Genome>();
				single.add(thisGenome);
				soFarTop.put(speciesId, single);
			}
		}
		// sort within each species
		soFarTop.keySet().forEach(thisSpeciesId -> {
			soFarTop.get(thisSpeciesId).sort((a, b) -> Double.compare(a.getFitnessScore(), b.getFitnessScore()));
		});
		// Now what? -- Pick the top X
		// and DELETE the others
		// If yes how many to delete? and what to do after deleting?
		
		Set<Genome> toReturn = new HashSet<Genome>();
		soFarTop.keySet().forEach(thisSpeciesId -> {
			List<Genome> allGenomesInthisSpecies = soFarTop.get(thisSpeciesId);
			toReturn.addAll(allGenomesInthisSpecies.subList(0, NUMBEROFCHAMPIONSTOSELECTINEACHSPECIES));
		});
		genomes = toReturn;
	}

	public void fitBattle() {

	}

	private void mutate(Genome genome, MutationType mutationType,
			Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration) {
		switch (mutationType) {
		case ADDCONNECTIONGENE:
			mutationAddConnectionGene(genome);
			break;
		case ADDNODEGENE:
			mutationAddNodeGene(genome, luckyConnectionGenesInThisGeneration);
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
		genome.getConnectionGenes().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCEFORTOGGLEENABLEDISABLE)) {
				connectionGene.setEnabled(!connectionGene.isEnabled());
			}
		});
	}

	private void mutationAlterWeightOfConnectionGene(Genome genome) {
		genome.getConnectionGenes().forEach(connectionGene -> {
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

	private void mutationAddNodeGene(Genome genome,
			Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration) {
		genome.getConnectionGenes().forEach(connectionGene -> {
			if (connectionGene.isLucky(CHANCEFORADDINGNEWNODE)) {
				NodeGene newNodeGene;
				if (luckyConnectionGenesInThisGeneration.keySet().stream()
						.anyMatch(oneOfLuckyConnectionGene -> oneOfLuckyConnectionGene
								.getFromReferenceNodeNumber() == connectionGene.getFromReferenceNodeNumber()
								&& oneOfLuckyConnectionGene.getToReferenceNodeNumber() == connectionGene
										.getToReferenceNodeNumber())) {
					newNodeGene = new NodeGene(
							luckyConnectionGenesInThisGeneration.get(connectionGene).getReferenceNodeNumber(),
							luckyConnectionGenesInThisGeneration.get(connectionGene).getType());
				} else {
					newNodeGene = constructNewNodeGene();
					luckyConnectionGenesInThisGeneration.put(connectionGene, newNodeGene);
				}
				genome.getNodeGenes().add(newNodeGene);
				// This connection will get a new node in between now.
				connectionGene.setEnabled(false);
				ConnectionGene firstHalf = constructNewConnectionGene(1.0, true,
						connectionGene.getFromReferenceNodeNumber(), newNodeGene.getReferenceNodeNumber());
				ConnectionGene secondHalf = constructNewConnectionGene(connectionGene.getWeight(), true,
						newNodeGene.getReferenceNodeNumber(), connectionGene.getToReferenceNodeNumber());
				genome.getConnectionGenes().add(firstHalf);
				genome.getConnectionGenes().add(secondHalf);
			}
		});
	}

	private void mutationAddConnectionGene(Genome genome) {
		Set<NodeGene> luckyPairs = new HashSet<NodeGene>();
		genome.getNodeGenes().forEach(nodeGene -> {
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
					ConnectionGene toAdd = constructNewConnectionGene(
							randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), true,
							from.getReferenceNodeNumber(), to.getReferenceNodeNumber());
					genome.getConnectionGenes().add(toAdd);
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
						connectionGene -> (connectionGene.getFromReferenceNodeNumber() == fromReferenceNodeNumber
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
