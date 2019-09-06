package com.vadrin.neuroevolution.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import com.vadrin.neuroevolution.services.MathService;
import com.vadrin.neuroevolution.services.MutationService;

public class Pool {

	private final int poolCapacity;
	private Set<Genome> genomes;
	private int referenceNodeCounter;
	private int referenceInnovationCounter;
	private int referenceGenerationCounter;
	private Set<InnovationInformation> innovationInformation;

	private static final int GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES = 15;
	private static final int NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES = 1;
	private static final int MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED = 5;

	public Pool(int poolCapacity, int inputNodesSize, int outputNodesSize) {
		super();
		this.poolCapacity = poolCapacity;
		this.genomes = new HashSet<Genome>();
		this.referenceNodeCounter = 0;
		this.referenceInnovationCounter = 0;
		this.referenceGenerationCounter = 0;
		this.innovationInformation = new HashSet<InnovationInformation>();
		startNewGeneration();
		// TODO: Somehow i need to add the bias node here... it wont be coming as part
		// of inputs array but still ill need to acomodate. Read the comments on the
		// mutate method regarding the bias nodes.
		Genome firstRandomGenome = constructGenome(inputNodesSize, outputNodesSize);
		for (int i = 1; i < poolCapacity; i++) {
			constructGenome(firstRandomGenome);
		}
	}

	//FromSampleConnectionGenes
	public Genome constructGenome(Set<ConnectionGene> sampleConnectionGenes) {
		Set<NodeGene> actualNodeGenes = new HashSet<NodeGene>();
		Set<ConnectionGene> actualConnectionGenes = new HashSet<ConnectionGene>();

		Iterator<ConnectionGene> cgng = sampleConnectionGenes.stream().iterator();
		while (cgng.hasNext()) {
			ConnectionGene thisSampleConnectionGene = cgng.next();
			int sampleRefNumberOfFrom = thisSampleConnectionGene.getFromNode().getReferenceNodeNumber();
			NodeGeneType sampleTypeOfFrom = thisSampleConnectionGene.getFromNode().getType();
			int sampleRefNumberOfTo = thisSampleConnectionGene.getToNode().getReferenceNodeNumber();
			NodeGeneType sampleTypeOfTo = thisSampleConnectionGene.getToNode().getType();

			NodeGene constructedNodeGeneOfFrom = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfFrom)) {
				constructedNodeGeneOfFrom = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfFrom).findFirst().get();
			} else {
				constructedNodeGeneOfFrom = constructNodeGene(sampleRefNumberOfFrom,
						sampleTypeOfFrom);
				actualNodeGenes.add(constructedNodeGeneOfFrom);
			}

			NodeGene constructedNodeGeneOfTo = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo)) {
				constructedNodeGeneOfTo = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo).findFirst().get();
			} else {
				constructedNodeGeneOfTo = constructNodeGene(sampleRefNumberOfTo, sampleTypeOfTo);
				actualNodeGenes.add(constructedNodeGeneOfTo);
			}
		}
		sampleConnectionGenes.forEach(sampleConn -> {
			NodeGene fromNodeGene = actualNodeGenes.stream()
					.filter(n -> n.getReferenceNodeNumber() == sampleConn.getFromNode().getReferenceNodeNumber())
					.findFirst().get();
			NodeGene toNodeGene = actualNodeGenes.stream()
					.filter(n -> n.getReferenceNodeNumber() == sampleConn.getToNode().getReferenceNodeNumber())
					.findFirst().get();
			actualConnectionGenes.add(constructConnectionGene(
					sampleConn.getReferenceInnovationNumber(), fromNodeGene, toNodeGene));
		});
		Genome toReturn = new Genome(actualNodeGenes, actualConnectionGenes, referenceGenerationCounter);
		genomes.add(toReturn);
		return toReturn;
	}

	public void killGenome(Genome genome) {
		genomes.remove(genome);
	}

	//WithReferenceNodeNumber
	public NodeGene constructNodeGene(Genome genome, int referenceNodeNumber,
			NodeGeneType type) {
		return constructNodeGene(referenceNodeNumber, type);
	}

	public void startNewGeneration() {
		genomes.forEach(g -> g.addFitnessLog(referenceGenerationCounter));
		referenceGenerationCounter++;
	}

	//Random
	private Genome constructGenome(int inputNodesSize, int outputNodesSize) {
		Set<NodeGene> inputNodeGenes = new HashSet<NodeGene>();
		Set<NodeGene> outputNodeGenes = new HashSet<NodeGene>();
		for (int j = 0; j < inputNodesSize; j++) {
			inputNodeGenes.add(constructNodeGene(NodeGeneType.INPUT));
		}
		for (int j = 0; j < outputNodesSize; j++) {
			outputNodeGenes.add(constructNodeGene(NodeGeneType.OUTPUT));
		}
		// Instatiate Empty Connection Genes
		Set<ConnectionGene> connectionGenes = new HashSet<ConnectionGene>();
		Iterator<NodeGene> itr = inputNodeGenes.stream().iterator();
		while (itr.hasNext()) {
			NodeGene in = itr.next();
			Iterator<NodeGene> oitr = outputNodeGenes.stream().iterator();
			while (oitr.hasNext()) {
				NodeGene out = oitr.next();
				connectionGenes.add(constructConnectionGene(in, out));
			}
			;
		}
		inputNodeGenes.addAll(outputNodeGenes);
		Genome genome = new Genome(inputNodeGenes, connectionGenes, referenceGenerationCounter);
		genomes.add(genome);
		return genome;
	}

	//Copy
	private Genome constructGenome(Genome oriGenome) {
		Genome copyGenome = constructGenome(
				oriGenome.getConnectionGenesSorted().stream().collect(Collectors.toSet()));
		genomes.add(copyGenome);
		return copyGenome;
	}

	//TODO: This needs to be removed. it wont always give valid answers
	public int getInnovationNumberOnlyAsPerCurrentGenomesInThePoolAndNotPastGenomes(int fromReferenceNodeNumber, int toReferenceNodeNumber) {
		Iterator<Genome> allGenomes = this.genomes.iterator();
		while (allGenomes.hasNext()) {
			Genome thisGenome = allGenomes.next();
			Iterator<ConnectionGene> allConnections = thisGenome.getConnectionGenesSorted().iterator();
			while (allConnections.hasNext()) {
				ConnectionGene thisConnection = allConnections.next();
				if (thisConnection.getFromNode().getReferenceNodeNumber() == fromReferenceNodeNumber && thisConnection.getToNode()
						.getReferenceNodeNumber() == toReferenceNodeNumber) {
					return thisConnection.getReferenceInnovationNumber();
				}
			}
		}
		throw new NoSuchElementException();
	}

	//Random
	public NodeGene constructNodeGene(NodeGeneType type) {
		referenceNodeCounter++;
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		return toReturn;
	}

	//WithReferenceNodeNumber
	public NodeGene constructNodeGene(int referenceNodeNumber, NodeGeneType type) {
		NodeGene toReturn = new NodeGene(referenceNodeNumber, type);
		return toReturn;
	}

	//WithNewInnovationNumber
	public ConnectionGene constructConnectionGene(NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGene(fromNodeGene, toNodeGene, MathService.randomNumber(
				MutationService.X_RANDOM_WEIGHT_LOWER_BOUND, MutationService.X_RANDOM_WEIGHT_UPPER_BOUND));
	}

	//Sometimes as part of mutate add node, the from will be more than to reference number, which is fine.
	//WithNewInnovationNumber
	public ConnectionGene constructConnectionGene(NodeGene fromNodeGene, NodeGene toNodeGene,
			double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene,
				referenceInnovationCounter);
		return toReturn;
	}

	//Sometimes as part of mutate add node, the from will be more than to reference number, which is fine.
	//WithExistingInnovationNumber
	public ConnectionGene constructConnectionGene(int referenceInnovationNumber,
			double weight, NodeGene fromNodeGene, NodeGene toNodeGene) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene, referenceInnovationNumber);
		return toReturn;
	}

	//WithExistingInnovationNumber
	public ConnectionGene constructConnectionGene(int referenceInnovationNumber,
			NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGene(referenceInnovationNumber, MathService
				.randomNumber(MutationService.X_RANDOM_WEIGHT_LOWER_BOUND, MutationService.X_RANDOM_WEIGHT_UPPER_BOUND),
				fromNodeGene, toNodeGene);
	}

	public int getPoolCapacity() {
		return poolCapacity;
	}

	public int getReferenceGenerationCounter() {
		return referenceGenerationCounter;
	}

	public Genome getReferenceGenomeOfSpeciesId(String speciesId) {
		return genomes.stream().filter(g -> g.getReferenceSpeciesNumber()!=null && g.getReferenceSpeciesNumber().equalsIgnoreCase(speciesId)).findFirst()
				.get();
	}

	public Set<String> getSpeciesIds() {
		Set<String> toReturn = new HashSet<String>();
		genomes.forEach(g -> {
			if (g.getReferenceSpeciesNumber() != null)
				toReturn.add(g.getReferenceSpeciesNumber());
		});
		return toReturn;
	}

	public Genome getRandomGenomeOfThisSpecies(String thisSpeciesId) {
		int randomPos = (int) MathService.randomNumber(0, getNumberOfGenomesInSpecies(thisSpeciesId) - 1);
		return genomes.stream()
				.filter(genome -> genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)).skip(randomPos)
				.findFirst().get();
	}

	public int getNumberOfGenomesInSpecies(String thisSpeciesId) {
		return (int) genomes.stream()
				.filter(genome -> genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)).count();
	}

	public void extinctThisSpeciesAlsoKillOfAnyRemainingGenomes(String thisSpeciesId) {
		Set<Genome> genomesToKill = new HashSet<Genome>();
		genomes.stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.forEach(g -> genomesToKill.add(g));
		genomesToKill.forEach(g -> killGenome(g));
	}

	public Genome getMaxFitGenomeOfThisSpecies(String thisSpeciesId) {
		return genomes.stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1).findFirst().get();
	}

	public Set<Genome> getAllGenomesOfThisSpecies(String speciesId) {
		return genomes.stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(speciesId))
				.collect(Collectors.toSet());
	}

	public boolean isSpeciesStagnated(String thisSpeciesId) {
		Iterator<Integer> genomesI = getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog().keySet().stream()
				.sorted((a, b) -> Integer.compare(b, a))
				.limit(GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES).iterator();
		double descPrevVal = genomesI.hasNext()
				? getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog().get(genomesI.next())
				: 0d;
		if (descPrevVal == 0d)
			return false;
		int counter = 1;
		while (genomesI.hasNext()) {
			double thisNum = getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog().get(genomesI.next());
			if (descPrevVal <= thisNum) {
				counter++;
			}
		}
		if (counter >= GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES)
			return true;
		return false;
	}

	public Set<String> championsWhoShouldntBeHarmed() {
		Set<String> toReturn = new HashSet<String>();

		// Pick top one in the overall pool
		genomes.stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1)
				.forEachOrdered(g -> toReturn.add(g.getId()));

		// Pick top in each species
		getSpeciesIds().stream().forEach(s -> {
			if (getNumberOfGenomesInSpecies(
					s) > MINIMUM_NUMBER_OF_GENOMES_IN_A_SPECIES_SO_THAT_ITS_CHAMPION_IS_LEFT_UNHARMED) {
				genomes.stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(s))
						.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
						.limit(NUMBER_OF_CHAMPIONS_TO_BE_LEFT_UNHARMED_IN_EACH_SPECIES)
						.forEachOrdered(g -> toReturn.add(g.getId()));
			}
		});

		return toReturn;
	}

	public Genome getGenome(String gid) {
		return genomes.stream().filter(g -> g.getId().equalsIgnoreCase(gid)).findFirst().get();
	}

	public Set<InnovationInformation> getInnovationInformation() {
		return innovationInformation;
	}

	public void addInnovationInformation(Integer referenceInnovationNumber, Integer createdReferenceNodeNumber, Integer createdFromReferenceInnovationNumber, Integer createdToReferenceInnovationNumber) {
		innovationInformation.add(new InnovationInformation(referenceInnovationNumber, createdReferenceNodeNumber, createdFromReferenceInnovationNumber, createdToReferenceInnovationNumber));
	}
	
	public List<Genome> getGenomes() {
		return genomes.stream()
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.collect(Collectors.toList());
	}

	public String getNodesMap() {
		Map<Integer, Integer> nodesMap = new HashMap<Integer, Integer>();
		genomes.forEach(g -> {
			nodesMap.put(g.getNodeGenesSorted().size(),
					nodesMap.containsKey(g.getNodeGenesSorted().size())
							? nodesMap.get(g.getNodeGenesSorted().size()) + 1
							: 1);
		});
		return nodesMap.toString();
	}
}
