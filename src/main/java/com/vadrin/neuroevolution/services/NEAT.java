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
import com.vadrin.neuroevolution.models.exceptions.InvalidInputException;
import com.vadrin.neuroevolution.models.exceptions.ReferenceInnovaationNumberDoesNotExistException;

public class NEAT {

	private Set<Genome> genomes;
	private int referenceInnovationCounter;
	private int referenceNodeCounter;

	private final int poolSize; // Use 150 for all practical purposes
	private final int inputNodesSize;
	private final int outputNodesSize;

	private static final double C1 = 1.0d;
	private static final double C2 = 1.0d;
	private static final double C3 = 0.4d;
	private static final double DELTAT = 3.0d;
	private static final int GENERATIONTHRESHOLDTOKILLEVERYONEINSPECIES = 15;
	private static final int NUMBEROFCHAMPIONSTOGETWILDCARDENTRYTONEXTGENERATION = 1; // ASSUSMING GENOMES IN SPECIES IS
																						// > 5
	private static final double CHANCEFORWEIGHTMUTATION = 0.8d; // 0.8 MEANS 80%
	private static final double CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT = 0.1d; // 0.1 MEANS 10%
//	private static final double CHANCEFORWEIGHTMUTATIONWITHSMALLPERTUBED = 1
//			- CHANCEFORWEIGHTMUTATIONWITHRANDOMREPLACEWEIGHT;
	private static final double PERTUBEDVARIANCEDIFFERENCE = 0.05d;
	private static final double CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS = 0.75d; // 0.75 MEANS 75%
	private static final double CHANCEFOROFFSPRINGFROMMUTATIONALONEWITHOUTCROSSOVER = 0.25d; // 0.25 MEANS 25%
	private static final double CHANCEFORINTERSPECIESMATING = 0.001d;
	private static final double CHANCEFORADDINGNEWNODE = 0.03d;
	private static final double CHANCEFORTOGGLEENABLEDISABLE = 0.03d;
	private static final double CHANCEFORADDINGNEWCONNECTION = 0.05d;
	private static final double RANDOMWEIGHTLOWERBOUND = -2d;
	private static final double RANDOMWEIGHTUPPERBOUND = -2d;
	private static final double PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES = 0.5;// 50%
	private static final double CHANCEFORGENETOBEPICKEDUPFROMEITHEROFPARENT = 0.5d; // half

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
		// run a test with all the genomes
		calculate();// done
		// update the fitness scores for each genome
		fitBattle();// done
		// populate the species id for each genome
		speciate();// done
		// Top 50% of genomes in each species are selected.
		select();// done
		// within the species select two random parents are re populate the pool
		crossOver();// done
		// mutate the ONLY NEW ONES OR ALL???
		mutate();// done
	}

	private void speciate() {
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

		// TODO: Need to remove the below useless lines of code
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

	private void mutate() {
		// this is wrong? everyone is getting mutated.. even the best ones. Is this
		// right? -> i think its right. whats the point if a genome stays the same.
		// besides its not everyone gets mutated. it needs to be "lucky" so we are cool
		// here.
		Map<ConnectionGene, NodeGene> luckyConnectionGenesInThisGeneration = new HashMap<ConnectionGene, NodeGene>();
		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			Arrays.asList(MutationType.class.getEnumConstants()).stream()
					.forEach(mutationType -> mutate(genome, mutationType, luckyConnectionGenesInThisGeneration));
		}
	}

	private void crossOver() {
		Iterator<Genome> iterator = genomes.iterator();
		Map<String, List<Genome>> speciesByGenomesMap = new HashMap<String, List<Genome>>();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			if (randomNumber(0d, 1d) < CHANCEFOROFFSPRINGFROMMUTATIONALONEWITHOUTCROSSOVER) {
				// This genome wont be crossed over. will just be mutated later.
			} else {
				// this genome will be selected for crossover
				if (speciesByGenomesMap.containsKey(genome.getSpeciesId())) {
					speciesByGenomesMap.get(genome.getSpeciesId()).add(genome);
				} else {
					List<Genome> emptyL = new ArrayList<Genome>();
					emptyL.add(genome);
					speciesByGenomesMap.put(genome.getSpeciesId(), emptyL);
				}
			}
		}
		// post this we have the list of genomes which we need to cross over between
		// themselves. Per species.
		Iterator<String> speciesIds = speciesByGenomesMap.keySet().iterator();
		while (speciesIds.hasNext()) {
			String thisSpeciesId = speciesIds.next();
			int currentNumberOfGenomesInThisSpecies = getNumberOfGenomesIn(thisSpeciesId);
			for (int i = 0; i < currentNumberOfGenomesInThisSpecies; i++) {
				// pick any two random genomes in this species
				// and then cross over between them
				// and then put them back in the pool with same speciesid
				int firstParentIndex = (int) randomNumber(0, speciesByGenomesMap.get(thisSpeciesId).size());
				int secondParentIndex = (int) randomNumber(0, speciesByGenomesMap.get(thisSpeciesId).size());
				Genome newGenome = crossOver(speciesByGenomesMap.get(thisSpeciesId).get(firstParentIndex),
						speciesByGenomesMap.get(thisSpeciesId).get(secondParentIndex));
				newGenome.setSpeciesId(thisSpeciesId);
				genomes.add(newGenome);
			}
		}
		// just validate if the total number in the pool is matching with pool size.
		// if not, do interspecies mating untill the actual pool size is reached
		while (genomes.size() < poolSize) {
			int firstSpeciesIndex = (int) randomNumber(0, speciesByGenomesMap.size());
			int secondSpeciesIndex = (int) randomNumber(0, speciesByGenomesMap.size());
			Iterator<String> allSpeciesIds = speciesByGenomesMap.keySet().iterator();
			int someCounter = 0;
			String firstSpeciesKey = null;
			String secondSpeciesKey = null;
			while (allSpeciesIds.hasNext()) {
				String thisKey = allSpeciesIds.next();
				if (firstSpeciesIndex == someCounter) {
					firstSpeciesKey = thisKey;
				} else if (secondSpeciesIndex == someCounter) {
					secondSpeciesKey = thisKey;
				}
				someCounter++;
			}
			Genome newGenome = crossOver(speciesByGenomesMap.get(firstSpeciesKey).get(0),
					speciesByGenomesMap.get(secondSpeciesKey).get(0));
			genomes.add(newGenome);
		}
	}

	private int getNumberOfGenomesIn(String thisSpeciesId) {
		int toReturn = 0;
		Iterator<Genome> iterator = genomes.iterator();
		while (iterator.hasNext()) {
			Genome genome = iterator.next();
			if (genome.getSpeciesId().equalsIgnoreCase(thisSpeciesId)) {
				toReturn++;
			}
		}
		return toReturn;
	}

	private Genome crossOver(Genome genome1, Genome genome2) {
		ConnectionGene[] connectionGenes1 = new ConnectionGene[genome1.getConnectionGenes().size()];
		connectionGenes1 = genome1.getConnectionGenes().toArray(connectionGenes1);
		ConnectionGene[] connectionGenes2 = new ConnectionGene[genome2.getConnectionGenes().size()];
		connectionGenes2 = genome2.getConnectionGenes().toArray(connectionGenes2);

		Arrays.sort(connectionGenes1,
				(a, b) -> Integer.compare(a.getReferenceInnovationNumber(), b.getReferenceInnovationNumber()));
		Arrays.sort(connectionGenes2,
				(a, b) -> Integer.compare(a.getReferenceInnovationNumber(), b.getReferenceInnovationNumber()));

		// TODO: Need to remove the below useless lines of code
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

			Genome tempGenome = genome1;
			genome1 = genome2;
			genome2 = tempGenome;
		}

		// post this line, connectiongene1 is having smaller max innovation when
		// compared to that of connectiongene2. So is the case with genom1 & genom2
		// variables
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

		Set<ConnectionGene> connectionGenesOfNewGene = new HashSet<ConnectionGene>();

		for (int i = 0; i < ConnectionGeneMostlyEmpty1.length; i++) {
			if (ConnectionGeneMostlyEmpty1[i] != null && ConnectionGeneMostlyEmpty2[i] != null) {
				// Both present so Pick one of connectionGene
				ConnectionGene toAdd;
				if (ConnectionGeneMostlyEmpty1[i].isLucky(CHANCEFORGENETOBEPICKEDUPFROMEITHEROFPARENT)) {
					toAdd = new ConnectionGene(ConnectionGeneMostlyEmpty1[i].getWeight(),
							ConnectionGeneMostlyEmpty1[i].isEnabled(),
							ConnectionGeneMostlyEmpty1[i].getFromReferenceNodeNumber(),
							ConnectionGeneMostlyEmpty1[i].getToReferenceNodeNumber(),
							ConnectionGeneMostlyEmpty1[i].getReferenceInnovationNumber());
				} else {
					toAdd = new ConnectionGene(ConnectionGeneMostlyEmpty2[i].getWeight(),
							ConnectionGeneMostlyEmpty2[i].isEnabled(),
							ConnectionGeneMostlyEmpty2[i].getFromReferenceNodeNumber(),
							ConnectionGeneMostlyEmpty2[i].getToReferenceNodeNumber(),
							ConnectionGeneMostlyEmpty2[i].getReferenceInnovationNumber());
				}

				if (!ConnectionGeneMostlyEmpty1[i].isEnabled() && !ConnectionGeneMostlyEmpty2[i].isEnabled()
						&& toAdd.isLucky(1 - CHANCEFORGENEDISABLEDIFDISABLEDINBOTHPARENTS)) {
					toAdd.setEnabled(true);
				}
				connectionGenesOfNewGene.add(toAdd);
			}
			if ((ConnectionGeneMostlyEmpty1[i] == null && ConnectionGeneMostlyEmpty2[i] != null)
					|| (ConnectionGeneMostlyEmpty2[i] == null && ConnectionGeneMostlyEmpty1[i] != null)) {
				ConnectionGene toAdd = null;
				if (i < connectionGene1MaxInnovationNumber) {
					// disjoing genes
					if (ConnectionGeneMostlyEmpty1[i] != null) {
						toAdd = new ConnectionGene(ConnectionGeneMostlyEmpty1[i].getWeight(),
								ConnectionGeneMostlyEmpty1[i].isEnabled(),
								ConnectionGeneMostlyEmpty1[i].getFromReferenceNodeNumber(),
								ConnectionGeneMostlyEmpty1[i].getToReferenceNodeNumber(),
								ConnectionGeneMostlyEmpty1[i].getReferenceInnovationNumber());
					} else {
						toAdd = new ConnectionGene(ConnectionGeneMostlyEmpty2[i].getWeight(),
								ConnectionGeneMostlyEmpty2[i].isEnabled(),
								ConnectionGeneMostlyEmpty2[i].getFromReferenceNodeNumber(),
								ConnectionGeneMostlyEmpty2[i].getToReferenceNodeNumber(),
								ConnectionGeneMostlyEmpty2[i].getReferenceInnovationNumber());
					}
				} else {
					// excess genes. Pick only if excess is in max fit parent
					if (genome2.getFitnessScore() > genome1.getFitnessScore()) {
						toAdd = new ConnectionGene(ConnectionGeneMostlyEmpty2[i].getWeight(),
								ConnectionGeneMostlyEmpty2[i].isEnabled(),
								ConnectionGeneMostlyEmpty2[i].getFromReferenceNodeNumber(),
								ConnectionGeneMostlyEmpty2[i].getToReferenceNodeNumber(),
								ConnectionGeneMostlyEmpty2[i].getReferenceInnovationNumber());
					}
				}
				connectionGenesOfNewGene.add(toAdd);
			}
		}
		Set<NodeGene> nodeGenesOfNewGene = new HashSet<NodeGene>();
		Iterator<ConnectionGene> cgng = connectionGenesOfNewGene.iterator();
		while (cgng.hasNext()) {
			ConnectionGene thisConnG = cgng.next();
			if (!contains(nodeGenesOfNewGene, thisConnG.getFromReferenceNodeNumber()))
				nodeGenesOfNewGene.add(new NodeGene(thisConnG.getFromReferenceNodeNumber(), getNodeTypeOfThisRefNumber(
						genome1.getNodeGenes(), genome2.getNodeGenes(), thisConnG.getFromReferenceNodeNumber())));
			if (!contains(nodeGenesOfNewGene, thisConnG.getToReferenceNodeNumber()))
				nodeGenesOfNewGene.add(new NodeGene(thisConnG.getToReferenceNodeNumber(), getNodeTypeOfThisRefNumber(
						genome1.getNodeGenes(), genome2.getNodeGenes(), thisConnG.getToReferenceNodeNumber())));
		}
		return new Genome(nodeGenesOfNewGene, connectionGenesOfNewGene);
	}

	private NodeGeneType getNodeTypeOfThisRefNumber(Set<NodeGene> nodeGenes, Set<NodeGene> nodeGenes2,
			int referenceNodeNumber) {
		try {
			return nodeGenes.stream().filter(ng -> ng.getReferenceNodeNumber() == referenceNodeNumber).findFirst().get()
					.getType();
		} catch (NoSuchElementException e) {
			return nodeGenes2.stream().filter(ng -> ng.getReferenceNodeNumber() == referenceNodeNumber).findFirst()
					.get().getType();
		}
	}

	private boolean contains(Set<NodeGene> nodeGenesOfNewGene, int refNum) {
		Iterator<NodeGene> iteratorN = nodeGenesOfNewGene.iterator();
		while (iteratorN.hasNext()) {
			if (iteratorN.next().getReferenceNodeNumber() == refNum) {
				return true;
			}
		}
		return false;
	}

	private void select() {
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
			toReturn.addAll(allGenomesInthisSpecies.subList(0,
					(int) PERCENTOFCHAMPIONSTOSELECTINEACHSPECIES * allGenomesInthisSpecies.size()));
		});
		genomes = toReturn;
	}

	public void fitBattle() {
		System.out.println("ITS ASSUMED THAT YOU HAVE ASSINGNED FITNESS SCORE FOR ALL GENOMES USING SETFITNESS METHOD");
	}

	public void calculate() {
		System.out.println("ITS ASSUMED THAT YOU HAVE RUN THE CALCUATE METHOD ON ALL THE INPUT");
	}

	public Double[] calculate(Genome genome, double[] input) throws InvalidInputException {
		int temp = 0;
		Iterator<NodeGene> inputNodeGenesSorted = genome.getNodeGenes().stream()
				.filter(nodeGen -> (nodeGen.getType() == NodeGeneType.INPUT))
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber())).iterator();
		while (inputNodeGenesSorted.hasNext()) {
			NodeGene thisOne = inputNodeGenesSorted.next();
			thisOne.setOutput(input[temp]);
			temp++;
		}
		if (inputNodeGenesSorted.hasNext())
			throw new InvalidInputException();

		Iterator<NodeGene> hiddenNodeGenesSorted = genome.getNodeGenes().stream()
				.filter(nodeGen -> (nodeGen.getType() == NodeGeneType.HIDDEN))
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber())).iterator();
		while (hiddenNodeGenesSorted.hasNext()) {
			NodeGene thisNGene = hiddenNodeGenesSorted.next();
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getConnectionGenes().stream()
					.filter(connGene -> (connGene.getToReferenceNodeNumber() == thisNGene.getReferenceNodeNumber()))
					.iterator();
			double sumOfInputToThisHiddenNode = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) + Over all connections
				sumOfInputToThisHiddenNode += tempConnGene.getWeight() * genome.getNodeGenes().stream()
						.filter(n -> n.getReferenceNodeNumber() == tempConnGene.getFromReferenceNodeNumber())
						.findFirst().get().getOutput();
			}
			double finalOutput = applySigmiodActivationFunction(sumOfInputToThisHiddenNode);
			thisNGene.setOutput(finalOutput);
		}

		Iterator<NodeGene> outputNodeGenesSorted = genome.getNodeGenes().stream()
				.filter(nodeGen -> (nodeGen.getType() == NodeGeneType.OUTPUT))
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber())).iterator();
		while (outputNodeGenesSorted.hasNext()) {
			NodeGene thisNGene = hiddenNodeGenesSorted.next();
			Iterator<ConnectionGene> relavantConnGenesIterator = genome.getConnectionGenes().stream()
					.filter(connGene -> (connGene.getToReferenceNodeNumber() == thisNGene.getReferenceNodeNumber()))
					.iterator();
			double sumOfInputToThisHiddenNode = 0;
			while (relavantConnGenesIterator.hasNext()) {
				ConnectionGene tempConnGene = relavantConnGenesIterator.next();
				// totalInput = (prevNodeOutput * connectionWeight) + Over all connections
				sumOfInputToThisHiddenNode += tempConnGene.getWeight() * genome.getNodeGenes().stream()
						.filter(n -> n.getReferenceNodeNumber() == tempConnGene.getFromReferenceNodeNumber())
						.findFirst().get().getOutput();
			}
			double finalOutput = applySigmiodActivationFunction(sumOfInputToThisHiddenNode);
			thisNGene.setOutput(finalOutput);
		}

		Iterator<NodeGene> toReturnIterator = genome.getNodeGenes().stream()
				.filter(nodeGen -> (nodeGen.getType() == NodeGeneType.OUTPUT))
				.sorted((a, b) -> Integer.compare(a.getReferenceNodeNumber(), b.getReferenceNodeNumber())).iterator();
		List<Double> toReturn = new ArrayList<Double>();
		while (toReturnIterator.hasNext()) {
			toReturn.add(toReturnIterator.next().getOutput());
		}
		return toReturn.toArray(new Double[toReturn.size()]);
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

	private double applySigmiodActivationFunction(double input) {
		return 1d / (1d + Math.exp(-input));
	}

	public Set<Genome> getGenomes() {
		return genomes;
	}

}
