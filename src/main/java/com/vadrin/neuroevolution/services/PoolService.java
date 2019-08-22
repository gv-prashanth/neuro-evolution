package com.vadrin.neuroevolution.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.vadrin.neuroevolution.models.ConnectionGene;
import com.vadrin.neuroevolution.models.Genome;
import com.vadrin.neuroevolution.models.NodeGene;
import com.vadrin.neuroevolution.models.NodeGeneType;

@Service
public class PoolService {

	private Map<String, Genome> genomesPool = new HashMap<String, Genome>();

	private int referenceNodeCounter = 0;
	private Map<String, NodeGene> nodeGenesPool = new HashMap<String, NodeGene>();

	private int referenceInnovationCounter = 0;
	private Set<ConnectionGene> connectionGenesPool = new HashSet<ConnectionGene>();

	private static final double RANDOMWEIGHTLOWERBOUND = -20d;
	private static final double RANDOMWEIGHTUPPERBOUND = 20d;

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
					.findAny().get();
			NodeGene toNodeGene = actualNodeGenes.stream()
					.filter(n -> n.getReferenceNodeNumber() == sampleConn.getToNode().getReferenceNodeNumber())
					.findAny().get();
			actualConnectionGenes.add(constructConnectionGeneWithExistingInnovationNumber(
					sampleConn.getReferenceInnovationNumber(), fromNodeGene, toNodeGene));
		});
		Genome toReturn = new Genome(actualNodeGenes, actualConnectionGenes);
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
		Genome genome = new Genome(inputNodeGenes, connectionGenes);
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
		return genomesPool.values().stream()
				.filter(g -> g.getConnectionGenesSorted().stream().anyMatch(c -> c.getId().equalsIgnoreCase(connectionId)))
				.findFirst().get();
	}

	public Genome getGenomeHavingNode(String nodeId) {
		return genomesPool.values().stream()
				.filter(g -> g.getNodeGenesSorted().stream().anyMatch(n -> n.getId().equalsIgnoreCase(nodeId))).findFirst()
				.get();
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

	protected NodeGene constructRandomNodeGene(NodeGeneType type) {
		referenceNodeCounter++;
		NodeGene toReturn = new NodeGene(referenceNodeCounter, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	protected NodeGene constructNodeGeneWithReferenceNodeNumber(int referenceNodeNumber, NodeGeneType type) {
		NodeGene toReturn = new NodeGene(referenceNodeNumber, type);
		nodeGenesPool.put(toReturn.getId(), toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithNewInnovationNumber(NodeGene fromNodeGene,
			NodeGene toNodeGene) {
		return constructConnectionGeneWithNewInnovationNumber(fromNodeGene, toNodeGene,
				MathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND));
	}

	protected ConnectionGene constructConnectionGeneWithNewInnovationNumber(NodeGene fromNodeGene, NodeGene toNodeGene,
			double weight) {
		referenceInnovationCounter++;
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene,
				referenceInnovationCounter);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			double weight, NodeGene fromNodeGene, NodeGene toNodeGene) {
		ConnectionGene toReturn = new ConnectionGene(weight, true, fromNodeGene, toNodeGene, referenceInnovationNumber);
		connectionGenesPool.add(toReturn);
		return toReturn;
	}

	protected ConnectionGene constructConnectionGeneWithExistingInnovationNumber(int referenceInnovationNumber,
			NodeGene fromNodeGene, NodeGene toNodeGene) {
		return constructConnectionGeneWithExistingInnovationNumber(referenceInnovationNumber,
				MathService.randomNumber(RANDOMWEIGHTLOWERBOUND, RANDOMWEIGHTUPPERBOUND), fromNodeGene, toNodeGene);
	}

}
