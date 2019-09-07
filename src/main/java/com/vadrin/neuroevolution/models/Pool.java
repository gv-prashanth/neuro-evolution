package com.vadrin.neuroevolution.models;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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

	public Pool(int poolCapacity, int inputNodesSize, int outputNodesSize) {
		super();
		this.poolCapacity = poolCapacity;
		this.genomes = new HashSet<Genome>();
		this.referenceNodeCounter = 0;
		this.referenceInnovationCounter = 0;
		this.referenceGenerationCounter = 0;
		this.innovationInformation = new HashSet<InnovationInformation>();
		increnemtReferenceGenerationCounter();
		Genome firstRandomGenome = constructGenome(inputNodesSize, outputNodesSize);
		for (int i = 1; i < poolCapacity; i++) {
			constructGenome(firstRandomGenome);
		}
	}

	// FromSampleConnectionGenes
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
				constructedNodeGeneOfFrom = constructNodeGene(sampleRefNumberOfFrom, sampleTypeOfFrom);
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
			actualConnectionGenes
					.add(constructConnectionGene(sampleConn.getReferenceInnovationNumber(), fromNodeGene, toNodeGene));
		});
		Genome toReturn = new Genome(actualNodeGenes, actualConnectionGenes, referenceGenerationCounter);
		genomes.add(toReturn);
		return toReturn;
	}

	// Random
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

	// Copy
	private Genome constructGenome(Genome oriGenome) {
		Genome copyGenome = constructGenome(oriGenome.getConnectionGenes().stream().collect(Collectors.toSet()));
		genomes.add(copyGenome);
		return copyGenome;
	}

	public void killGenome(Genome genome) {
		genomes.remove(genome);
	}

	// WithReferenceNodeNumber
	public NodeGene constructNodeGene(Genome genome, int referenceNodeNumber, NodeGeneType type) {
		return constructNodeGene(referenceNodeNumber, type);
	}

	// Random
	public NodeGene constructNodeGene(NodeGeneType type) {
		referenceNodeCounter++;
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		return toReturn;
	}

	// WithReferenceNodeNumber
	public NodeGene constructNodeGene(int referenceNodeNumber, NodeGeneType type) {
		NodeGene toReturn = new NodeGene(referenceNodeNumber, type);
		return toReturn;
	}

	// WithNewInnovationNumber
	public ConnectionGene constructConnectionGene(NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGene(fromNodeGene, toNodeGene, MathService.randomNumber(
				MutationService.X_RANDOM_WEIGHT_LOWER_BOUND, MutationService.X_RANDOM_WEIGHT_UPPER_BOUND));
	}

	// Sometimes as part of mutate add node, the from will be more than to reference
	// number, which is fine.
	// WithNewInnovationNumber
	public ConnectionGene constructConnectionGene(NodeGene fromNodeGene, NodeGene toNodeGene, double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene,
				referenceInnovationCounter);
		return toReturn;
	}

	// Sometimes as part of mutate add node, the from will be more than to reference
	// number, which is fine.
	// WithExistingInnovationNumber
	public ConnectionGene constructConnectionGene(int referenceInnovationNumber, double weight, NodeGene fromNodeGene,
			NodeGene toNodeGene) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene, referenceInnovationNumber);
		return toReturn;
	}

	// WithExistingInnovationNumber
	public ConnectionGene constructConnectionGene(int referenceInnovationNumber, NodeGene fromNodeGene,
			NodeGene toNodeGene) {
		return constructConnectionGene(referenceInnovationNumber, MathService
				.randomNumber(MutationService.X_RANDOM_WEIGHT_LOWER_BOUND, MutationService.X_RANDOM_WEIGHT_UPPER_BOUND),
				fromNodeGene, toNodeGene);
	}

	public void increnemtReferenceGenerationCounter() {
		genomes.forEach(g -> g.addFitnessLog(referenceGenerationCounter));
		referenceGenerationCounter++;
	}

	public int getPoolCapacity() {
		return poolCapacity;
	}

	public int getReferenceGenerationCounter() {
		return referenceGenerationCounter;
	}

	public Set<String> getSpecies() {
		Set<String> toReturn = new HashSet<String>();
		genomes.forEach(g -> {
			if (g.getReferenceSpeciesNumber() != null)
				toReturn.add(g.getReferenceSpeciesNumber());
		});
		return toReturn;
	}

	public Set<InnovationInformation> getInnovationInformation() {
		return innovationInformation;
	}

	public void addInnovationInformation(Integer referenceInnovationNumber, Integer createdReferenceNodeNumber,
			Integer createdFromReferenceInnovationNumber, Integer createdToReferenceInnovationNumber) {
		innovationInformation.add(new InnovationInformation(referenceInnovationNumber, createdReferenceNodeNumber,
				createdFromReferenceInnovationNumber, createdToReferenceInnovationNumber));
	}

	public List<Genome> getGenomes() {
		return genomes.stream().sorted((a, b) -> Double.compare(b.getFitnessScore(), a.getFitnessScore()))
				.collect(Collectors.toList());
	}

	public Set<Genome> getGenomes(String thisSpeciesId) {
		return genomes.stream()
				.filter(genome -> genome.getReferenceSpeciesNumber() != null
						&& genome.getReferenceSpeciesNumber().equalsIgnoreCase(thisSpeciesId))
				.collect(Collectors.toSet());
	}

}
