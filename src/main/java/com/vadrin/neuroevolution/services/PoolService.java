package com.vadrin.neuroevolution.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;

public class PoolService {

	private Map<String, Genome> genomesPool = new HashMap<String, Genome>();
	private int referenceNodeCounter = 0;
	private Map<String, NodeGene> nodeGenesPool = new HashMap<String, NodeGene>();
	private int referenceInnovationCounter = 0;
	private Set<ConnectionGene> connectionGenesPool = new HashSet<ConnectionGene>();
	// TODO: Need to get rid of this below variable. Should get from NEAT class right?
	private int POOLCAPACITY;
	private int GENERATION;
	
	private static final int GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES = 15;

	public PoolService(int poolSize, int inputNodesSize, int outputNodesSize) {
		super();
		startNewGeneration();
		constructRandomGenomePool(poolSize, inputNodesSize, outputNodesSize);
	}

	public Genome constructGenomeFromSampleConnectionGenes(Set<ConnectionGene> sampleConnectionGenes) {
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
				constructedNodeGeneOfFrom = constructNodeGeneWithReferenceNodeNumber(sampleRefNumberOfFrom,
						sampleTypeOfFrom);
				actualNodeGenes.add(constructedNodeGeneOfFrom);
			}

			NodeGene constructedNodeGeneOfTo = null;
			if (actualNodeGenes.stream().anyMatch(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo)) {
				constructedNodeGeneOfTo = actualNodeGenes.stream()
						.filter(n -> n.getReferenceNodeNumber() == sampleRefNumberOfTo).findFirst().get();
			} else {
				constructedNodeGeneOfTo = constructNodeGeneWithReferenceNodeNumber(sampleRefNumberOfTo, sampleTypeOfTo);
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
			actualConnectionGenes.add(constructConnectionGeneWithExistingInnovationNumber(
					sampleConn.getReferenceInnovationNumber(), fromNodeGene, toNodeGene));
		});
		Genome toReturn = new Genome(actualNodeGenes, actualConnectionGenes, GENERATION);
		genomesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	public Collection<Genome> getGenomes() {
		return genomesPool.values();
	}

	public Genome getGenome(String id) {
		return genomesPool.get(id);
	}

	public void killGenome(String genomeId) {
		genomesPool.remove(genomeId);
	}

	public NodeGene constructNodeGeneWithReferenceNodeNumber(Genome genome, int referenceNodeNumber,
			NodeGeneType type) {
		return constructNodeGeneWithReferenceNodeNumber(referenceNodeNumber, type);
	}

	public NodeGene constructRandomNodeGene(Genome genome, NodeGeneType type) {
		return constructRandomNodeGene(type);
	}

	public void constructRandomGenomePool(int poolSize, int inputNodesSize, int outputNodesSize) {
		// TODO: Somehow i need to add the bias node here... it wont be coming as part
		// of inputs array but still ill need to acomodate. Read the comments on the
		// mutate method regarding the bias nodes.
		Genome firstRandomGenome = constructRandomGenome(inputNodesSize, outputNodesSize);
		for (int i = 1; i < poolSize; i++) {
			constructCopyGenome(firstRandomGenome);
		}
		this.POOLCAPACITY = poolSize;
	}

	public void startNewGeneration() {
		getGenomes().forEach(g -> g.addFitnessLog(GENERATION));
		GENERATION++;
	}

	private Genome constructRandomGenome(int inputNodesSize, int outputNodesSize) {
		Set<NodeGene> inputNodeGenes = new HashSet<NodeGene>();
		Set<NodeGene> outputNodeGenes = new HashSet<NodeGene>();
		for (int j = 0; j < inputNodesSize; j++) {
			inputNodeGenes.add(constructRandomNodeGene(NodeGeneType.INPUT));
		}
		for (int j = 0; j < outputNodesSize; j++) {
			outputNodeGenes.add(constructRandomNodeGene(NodeGeneType.OUTPUT));
		}
		// Instatiate Empty Connection Genes
		Set<ConnectionGene> connectionGenes = new HashSet<ConnectionGene>();
		Iterator<NodeGene> itr = inputNodeGenes.stream().iterator();
		while (itr.hasNext()) {
			NodeGene in = itr.next();
			Iterator<NodeGene> oitr = outputNodeGenes.stream().iterator();
			while (oitr.hasNext()) {
				NodeGene out = oitr.next();
				connectionGenes.add(constructConnectionGeneWithNewInnovationNumber(in, out));
			}
			;
		}
		inputNodeGenes.addAll(outputNodeGenes);
		Genome genome = new Genome(inputNodeGenes, connectionGenes, GENERATION);
		genomesPool.put(genome.getId(), genome);
		return genome;
	}

	private Genome constructCopyGenome(Genome oriGenome) {
		Genome copyGenome = constructGenomeFromSampleConnectionGenes(
				oriGenome.getConnectionGenesSorted().stream().collect(Collectors.toSet()));
		genomesPool.put(copyGenome.getId(), copyGenome);
		return copyGenome;
	}

	public Genome getGenomeHavingConnection(String connectionId) {
		return genomesPool.values().stream().filter(
				g -> g.getConnectionGenesSorted().stream().anyMatch(c -> c.getId().equalsIgnoreCase(connectionId)))
				.findFirst().get();
	}

	public Genome getGenomeHavingNode(String nodeId) {
		return genomesPool.values().stream()
				.filter(g -> g.getNodeGenesSorted().stream().anyMatch(n -> n.getId().equalsIgnoreCase(nodeId)))
				.findFirst().get();
	}

	public int getInnovationNumber(int fromReferenceNodeNumber, int toReferenceNodeNumber) {
		Iterator<Genome> allGenomes = this.getGenomes().iterator();
		while (allGenomes.hasNext()) {
			Genome thisGenome = allGenomes.next();
			Iterator<ConnectionGene> allConnections = thisGenome.getConnectionGenesSorted().iterator();
			while (allConnections.hasNext()) {
				ConnectionGene thisConnection = allConnections.next();
				if (thisConnection.getFromNode().getReferenceNodeNumber() == thisConnection.getToNode()
						.getReferenceNodeNumber()) {
					return thisConnection.getReferenceInnovationNumber();
				}
			}
		}
		throw new NoSuchElementException();
	}

	public NodeGene constructRandomNodeGene(NodeGeneType type) {
		referenceNodeCounter++;
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	public NodeGene constructNodeGeneWithReferenceNodeNumber(int referenceNodeNumber, NodeGeneType type) {
		NodeGene toReturn = new NodeGene(referenceNodeNumber, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	public ConnectionGene constructConnectionGeneWithNewInnovationNumber(NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGeneWithNewInnovationNumber(fromNodeGene, toNodeGene, MathService.randomNumber(
				MutationService.X_RANDOM_WEIGHT_LOWER_BOUND, MutationService.X_RANDOM_WEIGHT_UPPER_BOUND));
	}

	public ConnectionGene constructConnectionGeneWithNewInnovationNumber(NodeGene fromNodeGene, NodeGene toNodeGene,
			double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene,
				referenceInnovationCounter);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	public ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			double weight, NodeGene fromNodeGene, NodeGene toNodeGene) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene, referenceInnovationNumber);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	public ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGeneWithExistingInnovationNumber(referenceInnovationNumber, MathService
				.randomNumber(MutationService.X_RANDOM_WEIGHT_LOWER_BOUND, MutationService.X_RANDOM_WEIGHT_UPPER_BOUND),
				fromNodeGene, toNodeGene);
	}

	public int getPOOLCAPACITY() {
		return POOLCAPACITY;
	}

	public int getGENERATION() {
		return GENERATION;
	}

	public Genome getReferenceGenomeOfSpeciesId(String speciesId) {
		return getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(speciesId))
				.findFirst().get();
	}

	public Set<String> getSpeciesIds() {
		Set<String> toReturn = new HashSet<String>();
		getGenomes().forEach(g -> {
			if (g.getReferenceSpeciesNumber() != null)
				toReturn.add(g.getReferenceSpeciesNumber());
		});
		return toReturn;
	}

	public Genome getRandomGenomeOfThisSpecies(String thisSpeciesId) {
		int randomPos = (int) MathService.randomNumber(0, getNumberOfGenomesInSpecies(thisSpeciesId) - 1);
		return getGenomes().stream()
				.filter(genome -> genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)).skip(randomPos)
				.findFirst().get();
	}

	public int getNumberOfGenomesInSpecies(String thisSpeciesId) {
		return (int) getGenomes().stream()
				.filter(genome -> genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId)).count();
	}

	public void extinctThisSpeciesAlsoKillOfAnyRemainingGenomes(String thisSpeciesId) {
		Set<Genome> genomesToKill = new HashSet<Genome>();
		getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.forEach(g -> genomesToKill.add(g));
		genomesToKill.forEach(g -> killGenome(g.getId()));
	}

	public Genome getMaxFitGenomeOfThisSpecies(String thisSpeciesId) {
		return getGenomes().stream()
				.filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore())).limit(1).findFirst().get();
	}

	public Set<Genome> getAllGenomesOfThisSpecies(String speciesId) {
		return getGenomes().stream().filter(g -> g.getReferenceSpeciesNumber().equalsIgnoreCase(speciesId))
				.collect(Collectors.toSet());
	}
	
	
	
	boolean isSpeciesStagnated(String thisSpeciesId) {
		Iterator<Integer> genomesI = getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog()
				.keySet().stream().sorted((a, b) -> Integer.compare(b, a))
				.limit(GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES).iterator();
		double prevVal = genomesI.hasNext()
				? getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog().get(genomesI.next())
				: 0d;
		if (prevVal == 0d)
			return false;
		int counter = 1;
		while (genomesI.hasNext()) {
			double thisNum = getMaxFitGenomeOfThisSpecies(thisSpeciesId).getFitnessLog()
					.get(genomesI.next());
			if (prevVal <= thisNum) {
				counter++;
			}
		}
		if (counter >= GENERATIONS_AFTER_WHICH_TO_CUTOFF_THE_SPECIES_INCASE_FITNESS_STAGNATES)
			return true;
		return false;
	}
}
